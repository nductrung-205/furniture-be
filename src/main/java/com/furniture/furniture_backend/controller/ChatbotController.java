package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.ChatRequest;
import com.furniture.furniture_backend.dto.ChatResponse;
import com.furniture.furniture_backend.entity.ChatMessage;
import com.furniture.furniture_backend.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Endpoint chính để chat
     * POST /api/chatbot/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        
        // Validate request
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Message cannot be empty")
            );
        }

        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Session ID cannot be empty")
            );
        }

        ChatResponse response = chatbotService.processChat(request);
        return ResponseEntity.ok(
            ApiResponse.success("Chat processed successfully", response)
        );
    }

    /**
     * Lấy lịch sử chat
     * GET /api/chatbot/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getChatHistory(
            @PathVariable String sessionId) {
        
        List<ChatMessage> history = chatbotService.getChatHistory(sessionId);
        return ResponseEntity.ok(
            ApiResponse.success("Chat history retrieved successfully", history)
        );
    }

    /**
     * Xóa lịch sử chat
     * DELETE /api/chatbot/history/{sessionId}
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> clearChatHistory(
            @PathVariable String sessionId) {
        
        chatbotService.clearChatHistory(sessionId);
        return ResponseEntity.ok(
            ApiResponse.success("Chat history cleared successfully", null)
        );
    }

    /**
     * Health check endpoint
     * GET /api/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Chatbot service is running", "OK")
        );
    }
}