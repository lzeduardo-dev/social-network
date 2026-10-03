package com.luizeduardo.socialnetwork.modules.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDTO (
    UUID id,
    UUID conversationId,
    String senderUsername,
    String content,
    Instant createdAt,
    Instant readAt
) {}
