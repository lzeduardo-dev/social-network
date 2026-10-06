package com.luizeduardo.socialnetwork.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequestDTO (
    @Schema(example = "maria@email.com") String email,
    @Schema(example = "senha123") String password
) {}
