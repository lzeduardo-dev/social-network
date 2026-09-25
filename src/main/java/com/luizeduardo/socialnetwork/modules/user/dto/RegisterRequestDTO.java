package com.luizeduardo.socialnetwork.modules.user.dto;

public record RegisterRequestDTO (
    String name,
    String username,
    String email,
    String password
) {}


