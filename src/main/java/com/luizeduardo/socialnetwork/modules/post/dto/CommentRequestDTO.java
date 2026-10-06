package com.luizeduardo.socialnetwork.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentRequestDTO (
    @Schema(example = "Que legal!") String text
){}
