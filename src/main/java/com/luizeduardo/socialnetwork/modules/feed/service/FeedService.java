package com.luizeduardo.socialnetwork.modules.feed.service;

import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.model.Post;
import com.luizeduardo.socialnetwork.modules.post.repository.CommentRepository;
import com.luizeduardo.socialnetwork.modules.post.repository.LikeRepository;
import com.luizeduardo.socialnetwork.modules.post.repository.PostRepository;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.FollowRepository;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import com.luizeduardo.socialnetwork.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final S3StorageService storageService;

    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getFeed(Pageable pageable) {
        User currentUser = userService.getAuthenticatedUser();

        List<UUID> authorIds = new ArrayList<>(followRepository.findFollowingIdsByFollowerId(currentUser.getId()));
        authorIds.add(currentUser.getId());

        return postRepository.findPostsForFeed(authorIds, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getExplore(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponseDTO);
    }

    // Reutilizamos a lógica de mapeamento aqui, pois o Feed precisa entregar DTOs
    private PostResponseDTO mapToResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getContent(),
                post.getMediaKey() != null ? storageService.getUrl(post.getMediaKey()) : null,
                post.getAuthor().getUsername(),
                post.getCreatedAt(),
                likeRepository.countByPost_Id(post.getId()),
                commentRepository.countByPost_Id(post.getId())
        );
    }
}