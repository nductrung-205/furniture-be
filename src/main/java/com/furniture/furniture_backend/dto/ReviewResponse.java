package com.furniture.furniture_backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponse {
    private Long id;
    private String userName; 
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
