package org.sandbox.chat.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "message", nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime timestamp;

    public Message(User user, String content) {
        this.user = user;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
