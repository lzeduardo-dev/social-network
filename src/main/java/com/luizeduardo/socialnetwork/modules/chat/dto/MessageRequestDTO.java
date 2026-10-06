package com.luizeduardo.socialnetwork.modules.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageRequestDTO (
    @Schema(description = "Ate 2000 caracteres", example = "Oi, tudo bem?") String content
){}
