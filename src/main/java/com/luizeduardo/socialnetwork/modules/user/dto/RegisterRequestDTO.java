package com.luizeduardo.socialnetwork.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequestDTO (
    @Schema(example = "Maria Silva") String name,
    @Schema(example = "maria") String username,
    @Schema(example = "maria@email.com") String email,
    @Schema(example = "senha123") String password
) {}


