package com.luizeduardo.socialnetwork.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostRequestDTO (
    @Schema(description = "Texto do post", example = "Meu primeiro post!") String content,
    @Schema(description = "Chave retornada por POST /api/posts/media (opcional)", example = "posts/3f2b8c1e-9a4d-4e7f-8b6a-1c2d3e4f5a6b.png") String mediaKey
){}

