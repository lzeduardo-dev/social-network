package com.luizeduardo.socialnetwork.modules.post.service;

import com.luizeduardo.socialnetwork.modules.post.dto.CommentRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.CommentResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostRequestDTO;
import com.luizeduardo.socialnetwork.modules.post.dto.PostResponseDTO;
import com.luizeduardo.socialnetwork.modules.post.model.Comment;
import com.luizeduardo.socialnetwork.modules.post.model.Like;
import com.luizeduardo.socialnetwork.modules.post.model.Post;
import com.luizeduardo.socialnetwork.modules.post.repository.CommentRepository;
import com.luizeduardo.socialnetwork.modules.post.repository.LikeRepository;
import com.luizeduardo.socialnetwork.modules.post.repository.PostRepository;
import com.luizeduardo.socialnetwork.modules.user.model.User;
import com.luizeduardo.socialnetwork.modules.user.repository.UserRepository;
import com.luizeduardo.socialnetwork.modules.user.service.UserService;
import com.luizeduardo.socialnetwork.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostService {

    // Formato gerado pelo upload: posts/<uuid> com extensao opcional
    private static final Pattern MEDIA_KEY_PATTERN =
            Pattern.compile("^posts/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(\\.[A-Za-z0-9]+)?$");

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final S3StorageService storageService;

    // ---------- Posts ----------

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request) {
        if (isBlank(request.content()) && isBlank(request.mediaKey())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O post precisa ter texto ou midia");
        }

        String mediaKey = isBlank(request.mediaKey()) ? null : request.mediaKey().trim();
        if (mediaKey != null) {
            validateMediaKey(mediaKey);
        }

        User author = userService.getAuthenticatedUser();

        Post post = Post.builder()
                    .content(request.content())
                    .mediaKey(mediaKey)
                    .author(author)
                    .build();
        // Flush imediato para o @CreationTimestamp preencher o createdAt antes do mapeamento
        postRepository.saveAndFlush(post);

        return mapToResponseDTO(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDTO getPost(UUID postId) {
        return mapToResponseDTO(findPostOrThrow(postId));
    }

    // Posts de um usuario especifico (tela de perfil)
    @Transactional(readOnly = true)
    public Page<PostResponseDTO> getUserPosts(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuário nao encontrado"));

        return postRepository.findByAuthor_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    public void deletePost(UUID postId) {
        User currentUser = userService.getAuthenticatedUser();
        Post post = findPostOrThrow(postId);

        // Regra de negocio: apenas o autor pode apagar o post
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Voce so pode apagar seus proprios posts");
        }

        // Remove curtidas e comentarios antes para nao violar as chaves estrangeiras
        likeRepository.deleteByPost_Id(postId);
        commentRepository.deleteByPost_Id(postId);
        postRepository.delete(post);

        // Remove a midia do bucket so depois do commit, para nao perder o arquivo se o delete falhar
        String mediaKey = post.getMediaKey();
        if (mediaKey != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    storageService.delete(mediaKey);
                }
            });
        }
    }

    // ---------- Curtidas ----------

    @Transactional
    public void likePost(UUID postId) {
        User currentUser = userService.getAuthenticatedUser();
        Post post = findPostOrThrow(postId);

        if (likeRepository.existsByPost_IdAndUser_Id(postId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voce ja curtiu este post");
        }

        Like like = Like.builder()
                .post(post)
                .user(currentUser)
                .build();
        likeRepository.save(like);
    }

    @Transactional
    public void unlikePost(UUID postId) {
        User currentUser = userService.getAuthenticatedUser();

        Like like = likeRepository.findByPost_IdAndUser_Id(postId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voce nao curtiu este post"));

        likeRepository.delete(like);
    }

    // ---------- Comentarios ----------

    @Transactional
    public CommentResponseDTO addComment(UUID postId, CommentRequestDTO request) {
        if (isBlank(request.text())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O comentario nao pode ser vazio");
        }

        User currentUser = userService.getAuthenticatedUser();
        Post post = findPostOrThrow(postId);

        Comment comment = Comment.builder()
                .post(post)
                .user(currentUser)
                .content(request.text().trim())
                .build();
        commentRepository.save(comment);

        return mapToCommentDTO(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getComments(UUID postId, Pageable pageable) {
        findPostOrThrow(postId);

        return commentRepository.findByPost_IdOrderByCreatedAtDesc(postId, pageable )
                .map(this::mapToCommentDTO);
    }

    // ---------- Auxiliares ----------

    private Post findPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post nao encontrado"));
    }

    // Aceita apenas chaves geradas pelo upload e que existam de fato no bucket
    private void validateMediaKey(String mediaKey) {
        if (!MEDIA_KEY_PATTERN.matcher(mediaKey).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mediaKey invalida");
        }
        if (!storageService.exists(mediaKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Midia nao encontrada; envie o arquivo em /api/posts/media");
        }
    }

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

    private CommentResponseDTO mapToCommentDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getCreatedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
