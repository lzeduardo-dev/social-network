package com.luizeduardo.socialnetwork.modules.post.repository;

import com.luizeduardo.socialnetwork.modules.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    
    // 1. Busca os posts de um usuário específico (Ideal para a tela de Perfil do usuário).
    //@EntityGraph faz um LEFT JOIN com a tabela de usuários automaticamente
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    
    // 2. Busca todos os posts da rede (Ideal para uma aba "Explorar" ou Feed Global).
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 3. Busca posts de uma lista de usuários (O coração do Feed Personalizado).
     * O JOIN FETCH é a alternativa via JPQL ao @EntityGraph. Ele busca o Post
     * e o User na mesma query SQL.
     */
    @Query(
        value = "SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds ORDER BY p.createdAt DESC",
        countQuery = "SELECT count(p) FROM Post p WHERE p.user.id IN :userIds"
    )
    Page<Post> findPostsForFeed(@Param("userIds") List<UUID> userIds, Pageable pageable);
}