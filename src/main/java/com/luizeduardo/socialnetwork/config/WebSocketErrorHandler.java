package com.luizeduardo.socialnetwork.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

/**
 * Monta o frame ERROR quando o WebSocketJwtInterceptor recusa um frame.
 * Sem isso o cliente recebe so "Failed to send message to ExecutorSubscribableChannel".
 */
@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        // O interceptor lanca dentro do canal, que embrulha a excecao em MessageDeliveryException
        Throwable cause = ex instanceof MessageDeliveryException && ex.getCause() != null ? ex.getCause() : ex;

        String reason;
        if (cause instanceof AuthenticationException) {
            reason = "Nao autenticado: " + cause.getMessage();
        } else if (cause instanceof AccessDeniedException) {
            reason = "Acesso negado: " + cause.getMessage();
        } else {
            return super.handleClientMessageProcessingError(clientMessage, ex);
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(reason);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(reason.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
    }
}
