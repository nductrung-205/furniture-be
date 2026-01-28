package com.furniture.furniture_backend.repository;

import com.furniture.furniture_backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    
    // Lấy lịch sử chat theo sessionId
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
    
    // Lấy lịch sử chat theo userId (nếu có)
    List<ChatMessage> findByUserIdOrderByTimestampDesc(Long userId);
    
    // Xóa lịch sử chat cũ hơn X ngày
    void deleteByTimestampBefore(java.time.LocalDateTime date);
}