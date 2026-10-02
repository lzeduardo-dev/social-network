package com.luizeduardo.socialnetwork.modules.post.repository;

import com.luizeduardo.socialnetwork.modules.post.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID>  {
    boolean existsByPost_IdAndUser_Id(UUID postId, UUID userId);
    Optional<Like> findByPost_IdAndUser_Id(UUID postId, UUID userId);
    long countByPost_Id(UUID postId);
    void deleteByPost_Id(UUID postId);
}
