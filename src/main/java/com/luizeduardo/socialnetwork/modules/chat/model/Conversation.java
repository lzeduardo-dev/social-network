package com.luizeduardo.socialnetwork.modules.chat.model;

import com.luizeduardo.socialnetwork.modules.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Conversa direta entre dois usuarios.
 * O par e salvo sempre ordenado (user1.id < user2.id) para existir uma unica conversa por dupla.
 */
@Entity
@Table(name = "conversations",
       uniqueConstraints = {
        @UniqueConstraint(name = "uk_conversation_users", columnNames = {"user1_id", "user2_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    // Atualizado a cada mensagem; ordena a lista de conversas
    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean hasParticipant(UUID userId) {
        return user1.getId().equals(userId) || user2.getId().equals(userId);
    }

    public User getOtherParticipant(UUID userId) {
        return user1.getId().equals(userId) ? user2 : user1;
    }
}
