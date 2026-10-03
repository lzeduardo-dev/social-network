package com.luizeduardo.socialnetwork.modules.chat.controller;

import com.luizeduardo.socialnetwork.modules.chat.dto.ConversationResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.MessageRequestDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.MessageResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.service.ChatService;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // GET /api/chats -> conversas do usuario logado, mais recentes primeiro
    @GetMapping
    public Page<ConversationResponseDTO> getConversations(@PageableDefault(size = 20) Pageable pageable) {
        return chatService.getConversations(userService.getAuthenticatedUser(), pageable);
    }

    // POST /api/chats/{username} -> abre (ou cria) a conversa com o usuario
    @PostMapping("/{username}")
    public ConversationResponseDTO openConversation(@PathVariable String username) {
        return chatService.getOrCreateConversation(userService.getAuthenticatedUser(), username);
    }

    // GET /api/chats/{conversationId}/messages -> historico paginado
    @GetMapping("/{conversationId}/messages")
    public Page<MessageResponseDTO> getMessages(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 30) Pageable pageable) {
        return chatService.getMessages(userService.getAuthenticatedUser(), conversationId, pageable);
    }

    // Alternativa REST ao envio via WebSocket; a entrega em tempo real acontece igual
    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDTO sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody MessageRequestDTO request) {
        return chatService.sendMessage(userService.getAuthenticatedUser(), conversationId, request.content());
    }

    @PostMapping("/{conversationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable UUID conversationId) {
        chatService.markAsRead(userService.getAuthenticatedUser(), conversationId);
    }
}
