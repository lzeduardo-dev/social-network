package com.luizeduardo.socialnetwork.modules.user.service;

import com.luizeduardo.socialnetwork.modules.user.model.Follow;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.FollowRepository;
import com.luizeduardo.socialnetwork.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service 
@RequiredArgsConstructor 
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserService userService; // Usaremos para pegar o usuário logado

    @Transactional
    public void follow(String targetUsername) {
        // 1. Pega o usuário logado (Quem está clicando no botão)
        User currentUser = userService.getAuthenticatedUser();

        // 2. Busca quem ele quer seguir
        User targetUser = userRepository.findByUsername(targetUsername.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário alvo não encontrado."));

        // 3. Regra de Negócio: Não pode seguir a si mesmo
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode seguir a si mesmo.");
        }

        // 4. Regra de Negócio: Não pode seguir duplicado
        if (followRepository.existsByFollower_IdAndFollowing_Id(currentUser.getId(), targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já segue este usuário.");
        }

        // 5. Salva o relacionamento
        Follow newFollow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(newFollow);
    }

    @Transactional
    public void unfollow(String targetUsername) {
        User currentUser = userService.getAuthenticatedUser();
        
        User targetUser = userRepository.findByUsername(targetUsername.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário alvo não encontrado."));

        // Busca o relacionamento exato para deletar
        Follow follow = followRepository.findByFollower_IdAndFollowing_Id(currentUser.getId(), targetUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não segue este usuário."));

        followRepository.delete(follow);
    }
}