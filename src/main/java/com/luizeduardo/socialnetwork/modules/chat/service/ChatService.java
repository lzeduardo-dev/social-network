package com.luizeduardo.socialnetwork.modules.chat.service;

import com.luizeduardo.socialnetwork.modules.chat.dto.ConversationResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.MessageResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.ReadReceiptDTO;
import com.luizeduardo.socialnetwork.modules.chat.model.Conversation;
import com.luizeduardo.socialnetwork.modules.chat.model.Message;
import com.luizeduardo.socialnetwork.modules.chat.repository.ConversationRepository;
import com.luizeduardo.socialnetwork.modules.chat.repository.MessageRepository;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.UserRepository;
import com.luizeduardo.socialnetwork.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final int MAX_MESSAGE_LENGTH = 2000;

    // Filas privadas que o cliente assina: /user/queue/messages e /user/queue/read
    private static final String MESSAGES_QUEUE = "/queue/messages";
    private static final String READ_QUEUE = "/queue/read";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final S3StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate;

    // ---------- Conversas ----------

    // Retorna a conversa com o usuario alvo, criando-a na primeira vez
    @Transactional
    public ConversationResponseDTO getOrCreateConversation(User currentUser, String targetUsername) {
        User target = userRepository.findByUsername(targetUsername.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário nao encontrado"));

        if (target.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voce nao pode conversar consigo mesmo");
        }

        // Ordena o par para a restricao unica (user1_id, user2_id) valer nos dois sentidos
        boolean currentFirst = currentUser.getId().compareTo(target.getId()) < 0;
        User user1 = currentFirst ? currentUser : target;
        User user2 = currentFirst ? target : currentUser;

        Conversation conversation = conversationRepository.findByUser1_IdAndUser2_Id(user1.getId(), user2.getId())
                .orElseGet(() -> conversationRepository.saveAndFlush(Conversation.builder()
                        .user1(user1)
                        .user2(user2)
                        .lastMessageAt(Instant.now())
                        .build()));

        return mapToConversationDTO(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponseDTO> getConversations(User currentUser, Pageable pageable) {
        return conversationRepository.findByParticipant(currentUser.getId(), pageable)
                .map(conversation -> mapToConversationDTO(conversation, currentUser));
    }

    // ---------- Mensagens ----------

    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getMessages(User currentUser, UUID conversationId, Pageable pageable) {
        findConversationForParticipant(conversationId, currentUser);

        return messageRepository.findByConversation_IdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::mapToMessageDTO);
    }

    // Salva a mensagem e, apos o commit, entrega em tempo real para os dois participantes
    @Transactional
    public MessageResponseDTO sendMessage(User sender, UUID conversationId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem nao pode ser vazia");
        }
        String text = content.trim();
        if (text.length() > MAX_MESSAGE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A mensagem pode ter no maximo " + MAX_MESSAGE_LENGTH + " caracteres");
        }

        Conversation conversation = findConversationForParticipant(conversationId, sender);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(text)
                .build();
        // Flush imediato para o @CreationTimestamp preencher o createdAt antes do mapeamento
        messageRepository.saveAndFlush(message);

        conversation.setLastMessageAt(message.getCreatedAt());

        MessageResponseDTO dto = mapToMessageDTO(message);
        String recipientEmail = conversation.getOtherParticipant(sender.getId()).getEmail();
        String senderEmail = sender.getEmail();

        // O remetente tambem recebe, para sincronizar outras abas/dispositivos abertos
        afterCommit(() -> {
            messagingTemplate.convertAndSendToUser(recipientEmail, MESSAGES_QUEUE, dto);
            messagingTemplate.convertAndSendToUser(senderEmail, MESSAGES_QUEUE, dto);
        });

        return dto;
    }

    // Marca como lidas as mensagens recebidas e avisa o outro participante
    @Transactional
    public void markAsRead(User reader, UUID conversationId) {
        Conversation conversation = findConversationForParticipant(conversationId, reader);

        Instant now = Instant.now();
        int updated = messageRepository.markAsRead(conversationId, reader.getId(), now);
        if (updated == 0) {
            return;
        }

        ReadReceiptDTO receipt = new ReadReceiptDTO(conversationId, reader.getUsername(), now);
        String otherEmail = conversation.getOtherParticipant(reader.getId()).getEmail();

        afterCommit(() -> messagingTemplate.convertAndSendToUser(otherEmail, READ_QUEUE, receipt));
    }

    // ---------- Auxiliares ----------

    // Responde 404 tambem para quem nao participa, para nao revelar que a conversa existe
    private Conversation findConversationForParticipant(UUID conversationId, User user) {
        return conversationRepository.findWithParticipantsById(conversationId)
                .filter(conversation -> conversation.hasParticipant(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa nao encontrada"));
    }

    private void afterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private ConversationResponseDTO mapToConversationDTO(Conversation conversation, User currentUser) {
        User other = conversation.getOtherParticipant(currentUser.getId());

        return new ConversationResponseDTO(
                conversation.getId(),
                other.getUsername(),
                other.getName(),
                other.getAvatarKey() != null ? storageService.getUrl(other.getAvatarKey()) : null,
                messageRepository.findFirstByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                        .map(this::mapToMessageDTO)
                        .orElse(null),
                messageRepository.countByConversation_IdAndSender_IdNotAndReadAtIsNull(conversation.getId(), currentUser.getId()),
                conversation.getLastMessageAt()
        );
    }

    private MessageResponseDTO mapToMessageDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }
}
