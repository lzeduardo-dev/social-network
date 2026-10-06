package com.luizeduardo.socialnetwork.modules.chat.controller;

import com.luizeduardo.socialnetwork.modules.chat.dto.ConversationResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.MessageRequestDTO;
import com.luizeduardo.socialnetwork.modules.chat.dto.MessageResponseDTO;
import com.luizeduardo.socialnetwork.modules.chat.service.ChatService;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat", description = "Conversas e mensagens diretas. O tempo real (STOMP) esta descrito no topo da documentacao.")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // GET /api/chats -> conversas do usuario logado, mais recentes primeiro
    @GetMapping
    @Operation(summary = "Lista as conversas do usuario logado",
            description = "Ordenadas pela ultima mensagem; inclui a ultima mensagem e a contagem de nao lidas.")
    public Page<ConversationResponseDTO> getConversations(@PageableDefault(size = 20) Pageable pageable) {
        return chatService.getConversations(userService.getAuthenticatedUser(), pageable);
    }

    // POST /api/chats/{username} -> abre (ou cria) a conversa com o usuario
    @PostMapping("/{username}")
    @Operation(summary = "Abre ou cria a conversa com um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversa existente ou recem-criada"),
            @ApiResponse(responseCode = "400", description = "Tentativa de conversar consigo mesmo"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    public ConversationResponseDTO openConversation(@PathVariable String username) {
        return chatService.getOrCreateConversation(userService.getAuthenticatedUser(), username);
    }

    // GET /api/chats/{conversationId}/messages -> historico paginado
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Historico de mensagens", description = "Mais recentes primeiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de mensagens"),
            @ApiResponse(responseCode = "404", description = "Conversa nao encontrada ou usuario nao participa dela")
    })
    public Page<MessageResponseDTO> getMessages(
            @PathVariable UUID conversationId,
            @PageableDefault(size = 30) Pageable pageable) {
        return chatService.getMessages(userService.getAuthenticatedUser(), conversationId, pageable);
    }

    // Alternativa REST ao envio via WebSocket; a entrega em tempo real acontece igual
    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia uma mensagem",
            description = "Alternativa REST ao envio por STOMP; os participantes recebem em /user/queue/messages. Maximo de 2000 caracteres.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensagem enviada"),
            @ApiResponse(responseCode = "400", description = "Mensagem vazia ou longa demais"),
            @ApiResponse(responseCode = "404", description = "Conversa nao encontrada ou usuario nao participa dela")
    })
    public MessageResponseDTO sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody MessageRequestDTO request) {
        return chatService.sendMessage(userService.getAuthenticatedUser(), conversationId, request.content());
    }

    @PostMapping("/{conversationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marca a conversa como lida",
            description = "O outro participante recebe a confirmacao em /user/queue/read.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mensagens marcadas como lidas"),
            @ApiResponse(responseCode = "404", description = "Conversa nao encontrada ou usuario nao participa dela")
    })
    public void markAsRead(@PathVariable UUID conversationId) {
        chatService.markAsRead(userService.getAuthenticatedUser(), conversationId);
    }
}
