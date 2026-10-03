package com.luizeduardo.socialnetwork.modules.chat.repository;

import com.luizeduardo.socialnetwork.modules.chat.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // O par ja deve vir ordenado (user1Id < user2Id)
    Optional<Conversation> findByUser1_IdAndUser2_Id(UUID user1Id, UUID user2Id);

    @EntityGraph(attributePaths = {"user1", "user2"})
    @Query(
        value = "SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.lastMessageAt DESC",
        countQuery = "SELECT count(c) FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId"
    )
    Page<Conversation> findByParticipant(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user1", "user2"})
    @Query("SELECT c FROM Conversation c WHERE c.id = :id")
    Optional<Conversation> findWithParticipantsById(@Param("id") UUID id);
}
