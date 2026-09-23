package com.luizeduardo.socialnetwork.modules.user.dto;

public record AuthResponseDTO (
    String token,
    String username,
    String email
) {}
