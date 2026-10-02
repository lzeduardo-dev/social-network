package com.luizeduardo.socialnetwork.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Autentica a sessao STOMP no frame CONNECT usando o mesmo JWT da API REST.
 * O handshake HTTP em /ws e publico; quem barra conexoes anonimas e este interceptor.
 */
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            // O usuario fica associado a sessao e vira o Principal das mensagens seguintes
            accessor.setUser(authenticate(accessor.getFirstNativeHeader("Authorization")));
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            if (accessor.getUser() == null) {
                throw new AccessDeniedException("Sessao WebSocket nao autenticada");
            }

            String destination = accessor.getDestination();
            // Cada usuario so escuta as proprias filas (/user/queue/...), nunca a de outra pessoa
            if (StompCommand.SUBSCRIBE.equals(command) && (destination == null || !destination.startsWith("/user/queue/"))) {
                throw new AccessDeniedException("Destino de inscricao nao permitido: " + destination);
            }
            // Envios passam sempre pelos @MessageMapping (/app/...), nunca direto para o broker
            if (StompCommand.SEND.equals(command) && (destination == null || !destination.startsWith("/app/"))) {
                throw new AccessDeniedException("Destino de envio nao permitido: " + destination);
            }
        }

        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Header Authorization ausente no CONNECT");
        }

        String email = jwtService.extractEmail(header.substring(BEARER_PREFIX.length()));
        if (email == null) {
            throw new BadCredentialsException("Token invalido ou expirado");
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("Usuario do token nao existe mais");
        }
    }
}
