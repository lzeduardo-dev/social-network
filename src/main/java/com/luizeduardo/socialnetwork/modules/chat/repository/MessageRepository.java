package com.luizeduardo.socialnetwork.modules.chat.repository;

import com.luizeduardo.socialnetwork.modules.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Historico paginado, mais recentes primeiro
    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByConversation_IdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender"})
    Optional<Message> findFirstByConversation_IdOrderByCreatedAtDesc(UUID conversationId);

    // Mensagens recebidas (enviadas pelo outro participante) e ainda nao lidas
    long countByConversation_IdAndSender_IdNotAndReadAtIsNull(UUID conversationId, UUID userId);

    @Modifying
    @Query("UPDATE Message m SET m.readAt = :readAt " +
           "WHERE m.conversation.id = :conversationId AND m.sender.id <> :readerId AND m.readAt IS NULL")
    int markAsRead(@Param("conversationId") UUID conversationId,
                   @Param("readerId") UUID readerId,
                   @Param("readAt") Instant readAt);
}
