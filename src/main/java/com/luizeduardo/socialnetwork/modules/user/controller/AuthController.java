package com.luizeduardo.socialnetwork.modules.user.controller;

import com.luizeduardo.socialnetwork.modules.user.dto.AuthResponseDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.LoginRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.RegisterRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
