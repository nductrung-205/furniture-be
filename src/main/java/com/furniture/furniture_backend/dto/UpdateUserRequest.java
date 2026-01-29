package com.furniture.furniture_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải có 10 chữ số")
    private String phone;

    private String address;

    // Không có @NotBlank, chỉ kiểm tra độ dài NẾU người dùng nhập mật khẩu mới
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String password;
}