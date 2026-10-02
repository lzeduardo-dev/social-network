package com.luizeduardo.socialnetwork.modules.post.controller;

import com.luizeduardo.socialnetwork.modules.post.dto.CommentRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.CommentResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.service.PostService;
import com.luizeduardo.socialnetwork.storage.S3StorageService;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final S3StorageService storageService;

    //=== UPLOAD E CRIACAO ===

    // Ex: POST /api/posts
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadMedia(@RequestParam("file") MultipartFile file) {
        String key = storageService.upload(file, "posts");
        return Map.of("key", key, "url", storageService.getUrl(key));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDTO createPost(@RequestBody PostRequestDTO request) {
        return postService.createPost(request);
    }

    // === BUSCA E FEED ===

    @GetMapping("/user/{username}")
    public Page<PostResponseDTO> getUserPosts(
            @PathVariable String username,
            @PageableDefault(size = 10) Pageable pageable) {
        return postService.getUserPosts(username, pageable);
    }

    // === AÇOES SOBRE O POST ===
    @DeleteMapping("/{Id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID Id) {
       postService.deletePost(Id);
    }

    @PostMapping("/{Id}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likePost(@PathVariable UUID Id) {
        postService.likePost(Id);
    }

    @DeleteMapping("/{Id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePost(@PathVariable UUID Id) {
        postService.unlikePost(Id);
    }

    @PostMapping("/{Id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDTO addComment(
        @PathVariable UUID Id,
        @RequestBody CommentRequestDTO request
    ) {
        return postService.addComment(Id, request);
    }

}