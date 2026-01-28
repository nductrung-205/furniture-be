package com.furniture.furniture_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String sessionId;
    private LocalDateTime timestamp;
    private List<ProductSuggestion> suggestedProducts; // Sản phẩm gợi ý (nếu có)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private Long id;
        private String name;
        private String price;
        private String imageUrl;
        private String category;
    }
}