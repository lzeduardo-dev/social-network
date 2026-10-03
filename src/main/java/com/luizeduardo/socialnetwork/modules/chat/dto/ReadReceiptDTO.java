package com.luizeduardo.socialnetwork.modules.chat.dto;

import java.time.Instant;
import java.util.UUID;

// Enviado ao remetente quando o outro participante le a conversa
public record ReadReceiptDTO (
    UUID conversationId,
    String readerUsername,
    Instant readAt
) {}
