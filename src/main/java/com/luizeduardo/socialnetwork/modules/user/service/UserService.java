package com.luizeduardo.socialnetwork.modules.user.service;

import com.luizeduardo.socialnetwork.modules.user.dto.UserProfileDTO;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.FollowRepository;
import com.luizeduardo.socialnetwork.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service 
@RequiredArgsConstructor 
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // Extrai a entidade User do usuario que está fazendo a requisicao atual
    // esse metodo sera usado em toda aplicação 
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // O getName retorna o subject que foi definido no JWT 
        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "usuário não autenticado"));
    }

    public UserProfileDTO getMyProfile(){
        User currentUser = getAuthenticatedUser();
        return mapToProfileDTO(currentUser);
    }

    public UserProfileDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário nao encontrado"));
        return mapToProfileDTO(user);
    }

    private UserProfileDTO mapToProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getBio(), // Certifique-se de que sua classe User possui este atributo e o @Getter do Lombok
                user.getCreatedAt(),
                followRepository.countByFollowing_Id(user.getId()), // seguidores
                followRepository.countByFollower_Id(user.getId())   // seguindo
        );
    }

}
