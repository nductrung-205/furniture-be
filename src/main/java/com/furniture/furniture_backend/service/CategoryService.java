package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.CategoryRequest;
import com.furniture.furniture_backend.entity.Category;
import com.furniture.furniture_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    @Transactional
    public Category createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists with name: " + request.getName());
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryById(id);
        
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists with name: " + request.getName());
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}