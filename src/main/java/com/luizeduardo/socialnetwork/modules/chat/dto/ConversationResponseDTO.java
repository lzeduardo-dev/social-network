package com.luizeduardo.socialnetwork.modules.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponseDTO (
    UUID id,
    String otherUsername,
    String otherName,
    String otherAvatarUrl,
    MessageResponseDTO lastMessage,
    long unreadCount,
    Instant lastMessageAt
) {}
