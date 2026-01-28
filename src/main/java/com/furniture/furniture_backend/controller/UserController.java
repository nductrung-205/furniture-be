package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.AuthResponse;
import com.furniture.furniture_backend.dto.LoginRequest;
import com.furniture.furniture_backend.dto.RegisterRequest;
import com.furniture.furniture_backend.dto.UserResponse;
import com.furniture.furniture_backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Validated  // Bật validation cho controller
public class UserController {
    
    private final UserService userService;
    
    // ⭐ Public endpoints
    
    /**
     * Đăng ký tài khoản mới
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {  // ✅ @Valid để trigger validation
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Đăng ký thành công", response)
        );
    }
    
    /**
     * Đăng nhập
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {  // ✅ @Valid để trigger validation
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(
            ApiResponse.success("Đăng nhập thành công", response)
        );
    }
    
    // ⭐ Protected endpoints (cần JWT token)
    
    /**
     * Lấy danh sách tất cả users (ADMIN only)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
            ApiResponse.success("Lấy danh sách người dùng thành công", users)
        );
    }
    
    /**
     * Lấy user theo ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Lấy thông tin người dùng thành công", user)
        );
    }
    
    /**
     * Lấy user theo email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(
            ApiResponse.success("Lấy thông tin người dùng thành công", user)
        );
    }
    
    /**
     * Cập nhật thông tin user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest request) {  // ✅ @Valid cho update
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Cập nhật thông tin thành công", user)
        );
    }
    
    /**
     * Xóa user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
            ApiResponse.success("Xóa người dùng thành công", null)
        );
    }
}