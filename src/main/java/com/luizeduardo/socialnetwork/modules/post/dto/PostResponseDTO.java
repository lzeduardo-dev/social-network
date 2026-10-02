package com.luizeduardo.socialnetwork.modules.post.dto;

import java.time.Instant;
import java.util.UUID;

public record PostResponseDTO (
    UUID id,
    String content,
    String mediaUrl,
    String authorUsername,
    Instant createdAt,
    long likesCount,
    long commentsCount
) {}
