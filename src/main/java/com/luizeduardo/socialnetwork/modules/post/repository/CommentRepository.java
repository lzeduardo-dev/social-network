package com.luizeduardo.socialnetwork.modules.post.repository;

import java.util.UUID;
import com.luizeduardo.socialnetwork.modules.post.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByPost_IdOrderByCreatedAtDesc(UUID postId, Pageable pageable);
    long countByPost_Id(UUID postId);
    void deleteByPost_Id(UUID postId);
}
