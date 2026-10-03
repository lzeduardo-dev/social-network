package com.luizeduardo.socialnetwork.modules.chat.controller;

import com.luizeduardo.socialnetwork.modules.chat.dto.MessageRequestDTO;
import com.luizeduardo.socialnetwork.modules.chat.service.ChatService;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints STOMP do chat. O Principal vem do WebSocketJwtInterceptor (getName() = email).
 * As mensagens sao entregues pelo ChatService em /user/queue/messages.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserService userService;

    // SEND /app/chats/{conversationId}/send  {"content": "..."}
    @MessageMapping("/chats/{conversationId}/send")
    public void sendMessage(@DestinationVariable UUID conversationId,
                            @Payload MessageRequestDTO request,
                            Principal principal) {
        chatService.sendMessage(userService.getUserByEmail(principal.getName()), conversationId, request.content());
    }

    // SEND /app/chats/{conversationId}/read
    @MessageMapping("/chats/{conversationId}/read")
    public void markAsRead(@DestinationVariable UUID conversationId, Principal principal) {
        chatService.markAsRead(userService.getUserByEmail(principal.getName()), conversationId);
    }

    // Erros de regra de negocio voltam so para quem enviou, em /user/queue/errors
    @MessageExceptionHandler(ResponseStatusException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleError(ResponseStatusException e) {
        return Map.of("status", e.getStatusCode().value(), "message", String.valueOf(e.getReason()));
    }

    // Id invalido no destino, corpo vazio ou JSON malformado: sem isso o erro so aparecia no log
    @MessageExceptionHandler({ConversionException.class, MessageConversionException.class, MethodArgumentNotValidException.class})
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleBadRequest(Exception e) {
        return Map.of("status", 400, "message", "Requisicao invalida: verifique o id da conversa e o corpo {\"content\": \"...\"}");
    }

    @MessageExceptionHandler
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleUnexpected(Exception e) {
        log.error("Erro inesperado no chat via WebSocket", e);
        return Map.of("status", 500, "message", "Erro interno ao processar a mensagem");
    }
}
