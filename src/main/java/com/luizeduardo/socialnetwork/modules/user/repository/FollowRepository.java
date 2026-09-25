package com.luizeduardo.socialnetwork.modules.user.repository;

import com.luizeduardo.socialnetwork.modules.user.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    
    // Verifica se o relacionamento entre usuario e seguidor existe 
    boolean existsByFollower_IdAndFollowing_Id(UUID followerId, UUID followingId);

    // Busca entidade exata para poder deletar (deixar de seguir)
    Optional<Follow> findByFollower_IdAndFollowing_Id(UUID followerId, UUID followingId);

    // Contadores para perfil de usuario
    long countByFollower_Id(UUID followerId); // quantas pessoas segue
    long countByFollowing_Id(UUID followingId); // quantos seguidores tem 

    @EntityGraph(attributePaths = {"follower"})
    Page<Follow> findByFollowing_Id(UUID followingId, Pageable pageable);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
    List<UUID> findFollowingIdsByFollowerId(@Param("followerId") UUID followerId);
}
