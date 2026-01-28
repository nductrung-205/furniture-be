package com.furniture.furniture_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.furniture.furniture_backend.entity.Product;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Boolean isAvailable;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls;
    private String material;
    private String color;
    private String dimensions;
    private String weight;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
    
    public static ProductResponse fromProduct(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setDiscountPrice(product.getDiscountPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsAvailable(product.getIsAvailable());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setImageUrls(product.getImageUrls());
        response.setMaterial(product.getMaterial());
        response.setColor(product.getColor());
        response.setDimensions(product.getDimensions());
        response.setWeight(product.getWeight());
        response.setCreatedAt(product.getCreatedAt());
        return response;
    }
}