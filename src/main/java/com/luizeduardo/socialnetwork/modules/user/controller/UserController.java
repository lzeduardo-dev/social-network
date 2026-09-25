package com.luizeduardo.socialnetwork.modules.user.controller;

import com.luizeduardo.socialnetwork.modules.user.dto.UserProfileDTO;
import com.luizeduardo.socialnetwork.modules.user.service.FollowService;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
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
public class UserController {
    
    private final UserService userService;
    private final FollowService followService;

    // Retorna os dados do usuario logado
    @GetMapping("/me")
    public UserProfileDTO getMyProfile() {
        return userService.getMyProfile();
    }

    // Busca de qualquer perfil de usuario pelo username 
    @GetMapping("/{username}")
    public UserProfileDTO getUsername(@PathVariable String username) {
        return userService.getUserProfile(username);
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> followUser(@PathVariable String username) {
        followService.follow(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{username}/unfollow")
    public ResponseEntity<Void> unfollowUser(@PathVariable String username) {
        followService.unfollow(username);
        return ResponseEntity.noContent().build();
    }
}
