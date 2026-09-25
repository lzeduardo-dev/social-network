package com.luizeduardo.socialnetwork.modules.user.service;

import com.luizeduardo.socialnetwork.modules.user.dto.AuthResponseDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.LoginRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.RegisterRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.UserRepository;
import com.luizeduardo.socialnetwork.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor // Lombok gera o construtor para injeção de dependências
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Username sempre em minusculo para que "Luiz" e "luiz" sejam o mesmo usuario
        String username = request.username().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username ja em uso");
        }

        User user = User.builder()
                .name(request.name())
                .username(username)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);

        return toResponse(user);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            // Valida email + senha contra o hash BCrypt via UserDetailsService
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos"));

        return toResponse(user);
    }

    private AuthResponseDTO toResponse(User user) {
        return new AuthResponseDTO(jwtService.generateToken(user.getEmail()), user.getUsername(), user.getEmail());
    }
}
