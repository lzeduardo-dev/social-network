package com.luizeduardo.socialnetwork.modules.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileDTO (
    UUID id,
    String username,
    String bio,
    Instant createdAt,
    Long followersCount,
    Long followingCount
){}
