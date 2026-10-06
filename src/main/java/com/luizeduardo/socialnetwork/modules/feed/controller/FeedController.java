package com.luizeduardo.socialnetwork.modules.feed.controller;

import com.luizeduardo.socialnetwork.modules.feed.service.FeedService;
import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Linha do tempo e exploracao de posts")
public class FeedController {

    private final FeedService feedService;

    // GET /api/feed
    @GetMapping
    @Operation(summary = "Feed do usuario logado", description = "Posts de quem ele segue e os proprios, mais recentes primeiro.")
    public Page<PostResponseDTO> getFeed(@PageableDefault(size = 10) Pageable pageable) {
        return feedService.getFeed(pageable);
    }

    // GET /api/feed/explore
    @GetMapping("/explore")
    @Operation(summary = "Explorar", description = "Todos os posts da rede, mais recentes primeiro.")
    public Page<PostResponseDTO> getExplore(@PageableDefault(size = 10) Pageable pageable) {
        return feedService.getExplore(pageable);
    }
}