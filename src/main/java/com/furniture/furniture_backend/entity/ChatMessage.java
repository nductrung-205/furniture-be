package com.furniture.furniture_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(nullable = false)
    private String role; // "user" or "assistant"

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Optional: nếu muốn lưu theo user

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}