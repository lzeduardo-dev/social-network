package com.luizeduardo.socialnetwork.modules.post.controller;

import com.luizeduardo.socialnetwork.modules.post.dto.CommentRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.CommentResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.service.PostService;
import com.luizeduardo.socialnetwork.storage.S3StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Criacao de posts com midia, curtidas e comentarios")
public class PostController {

    private final PostService postService;
    private final S3StorageService storageService;

    //=== UPLOAD E CRIACAO ===

    // Ex: POST /api/posts/media
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia a midia de um post",
            description = "Primeiro passo para post com imagem: retorna a `key` a ser usada em `mediaKey` no POST /api/posts. Limite de 10MB.")
    public Map<String, String> uploadMedia(@RequestParam("file") MultipartFile file) {
        String key = storageService.upload(file, "posts");
        return Map.of("key", key, "url", storageService.getUrl(key));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um post", description = "Precisa ter `content`, `mediaKey` ou ambos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado"),
            @ApiResponse(responseCode = "400", description = "Post vazio, mediaKey invalida ou midia inexistente no bucket")
    })
    public PostResponseDTO createPost(@RequestBody PostRequestDTO request) {
        return postService.createPost(request);
    }

    // === BUSCA E FEED ===

    @GetMapping("/user/{username}")
    @Operation(summary = "Lista os posts de um usuario", description = "Mais recentes primeiro (tela de perfil).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de posts"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    public Page<PostResponseDTO> getUserPosts(
            @PathVariable String username,
            @PageableDefault(size = 10) Pageable pageable) {
        return postService.getUserPosts(username, pageable);
    }

    // === AÇOES SOBRE O POST ===
    @DeleteMapping("/{Id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Apaga um post", description = "Apenas o autor pode apagar; a midia e removida do bucket.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post apagado"),
            @ApiResponse(responseCode = "403", description = "O post e de outro usuario"),
            @ApiResponse(responseCode = "404", description = "Post nao encontrado")
    })
    public void deletePost(@PathVariable UUID Id) {
       postService.deletePost(Id);
    }

    @PostMapping("/{Id}/like")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Curte um post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post curtido"),
            @ApiResponse(responseCode = "404", description = "Post nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Post ja curtido")
    })
    public void likePost(@PathVariable UUID Id) {
        postService.likePost(Id);
    }

    @DeleteMapping("/{Id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a curtida de um post")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curtida removida"),
            @ApiResponse(responseCode = "400", description = "O post nao estava curtido")
    })
    public void unlikePost(@PathVariable UUID Id) {
        postService.unlikePost(Id);
    }

    @PostMapping("/{Id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Comenta em um post")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentario criado"),
            @ApiResponse(responseCode = "400", description = "Comentario vazio"),
            @ApiResponse(responseCode = "404", description = "Post nao encontrado")
    })
    public CommentResponseDTO addComment(
        @PathVariable UUID Id,
        @RequestBody CommentRequestDTO request
    ) {
        return postService.addComment(Id, request);
    }

}