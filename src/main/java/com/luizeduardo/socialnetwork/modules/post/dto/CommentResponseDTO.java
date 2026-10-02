package com.luizeduardo.socialnetwork.modules.post.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDTO (
    UUID id,
    String content,
    String authorUsername,
    Instant createdAt
) {}
