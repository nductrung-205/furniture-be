package com.furniture.furniture_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.furniture.furniture_backend.entity.User;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private User.Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}