package com.luizeduardo.socialnetwork.modules.user.controller;

import com.luizeduardo.socialnetwork.modules.user.dto.AuthResponseDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.AuthRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.dto.RegisterRequestDTO;
import com.luizeduardo.socialnetwork.modules.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.luizeduardo.socialnetwork.security.LoginRateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Cadastro e login; retornam o token JWT")
// Rotas publicas: removem o requisito de JWT definido globalmente no OpenApiConfig
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiterService rateLimiterService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um usuario", description = "O username e salvo em minusculo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado; retorna o token"),
            @ApiResponse(responseCode = "409", description = "Email ou username ja cadastrado")
    })
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Faz login com email e senha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado; retorna o token"),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login do mesmo IP; aguarde 1 minuto")
    })
    public AuthResponseDTO login(@RequestBody AuthRequestDTO request, HttpServletRequest httpRequest) {
        
        // Captura o IP do usuário
        String ip = httpRequest.getRemoteAddr();
        
        // Pega o "balde de fichas" correspondente a esse IP
        Bucket bucket = rateLimiterService.resolveBucket(ip);

        // Tenta consumir 1 ficha. Se o balde estiver vazio, bloqueia.
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, 
                "Muitas tentativas de login. Por favor, aguarde 1 minuto."
            );
        }

        // Se consumiu a ficha com sucesso, prossegue com o login
        return authService.login(request);
    }
}
