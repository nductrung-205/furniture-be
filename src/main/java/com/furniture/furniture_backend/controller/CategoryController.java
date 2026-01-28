package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.CategoryRequest;
import com.furniture.furniture_backend.entity.Category;
import com.furniture.furniture_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
            ApiResponse.success("Categories retrieved successfully", categories)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Category retrieved successfully", category)
        );
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Category created successfully", category)
        );
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Category updated successfully", category)
        );
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
            ApiResponse.success("Category deleted successfully", null)
        );
    }
}