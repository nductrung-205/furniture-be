package com.furniture.furniture_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // ← IMPORT

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String imageUrl;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore  // ← THÊM DÒNG NÀY - Không serialize products khi trả về Category
    private List<Product> products = new ArrayList<>();
}