package com.luizeduardo.socialnetwork.modules.user.controller;

import com.luizeduardo.socialnetwork.modules.user.dto.UserProfileDTO;
import com.luizeduardo.socialnetwork.modules.user.service.FollowService;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Perfis e seguidores")
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    // Retorna os dados do usuario logado
    @GetMapping("/me")
    @Operation(summary = "Perfil do usuario logado")
    public UserProfileDTO getMyProfile() {
        return userService.getMyProfile();
    }

    // Busca de qualquer perfil de usuario pelo username
    @GetMapping("/{username}")
    @Operation(summary = "Perfil publico de um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    public UserProfileDTO getUsername(@PathVariable String username) {
        return userService.getUserProfile(username);
    }

    @PostMapping("/{username}/follow")
    @Operation(summary = "Segue um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passou a seguir"),
            @ApiResponse(responseCode = "400", description = "Tentativa de seguir a si mesmo"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Ja segue este usuario")
    })
    public ResponseEntity<Void> followUser(@PathVariable String username) {
        followService.follow(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/unfollow")
    @Operation(summary = "Deixa de seguir um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deixou de seguir"),
            @ApiResponse(responseCode = "400", description = "Nao segue este usuario"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    })
    public ResponseEntity<Void> unfollowUser(@PathVariable String username) {
        followService.unfollow(username);
        return ResponseEntity.noContent().build();
    }
}
