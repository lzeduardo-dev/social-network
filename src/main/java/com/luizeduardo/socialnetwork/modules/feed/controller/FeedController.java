package com.luizeduardo.socialnetwork.modules.feed.controller;

import com.luizeduardo.socialnetwork.modules.feed.service.FeedService;
import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
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
public class FeedController {

    private final FeedService feedService;

    // GET /api/feed
    @GetMapping
    public Page<PostResponseDTO> getFeed(@PageableDefault(size = 10) Pageable pageable) {
        return feedService.getFeed(pageable);
    }

    // GET /api/feed/explore
    @GetMapping("/explore")
    public Page<PostResponseDTO> getExplore(@PageableDefault(size = 10) Pageable pageable) {
        return feedService.getExplore(pageable);
    }
}