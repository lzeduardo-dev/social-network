package com.luizeduardo.socialnetwork.modules.user.repository;

import com.luizeduardo.socialnetwork.modules.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Autenticação e Login
    // Essencial para o Spring Security carregar o usuario no momento do login
    Optional<User> findByEmail(String email);

    // Usado para carregar a página de perfil através da URL (ex: meudominio.com/@joao)
    Optional<User> findByUsername(String username);

    /**
     * 2. Validações de Cadastro
     * Métodos "existsBy" são muito mais rápidos que "findBy" porque eles 
     * geram um "SELECT 1 ... LIMIT 1" no banco, não trafegando dados desnecessários.
     */
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);

    /**
     * 3. Motor de Busca (Barra de Pesquisa)
     */
    // Busca simples apenas pelo nome de usuário (ignorando maiúsculas e minúsculas)
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    // Busca mais avançada: Procura o termo tanto no nome de usuário quanto na Bio do perfil
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}