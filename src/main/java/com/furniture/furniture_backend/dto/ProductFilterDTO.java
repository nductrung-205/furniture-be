package com.furniture.furniture_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String color;
    private String material;
    private String sortBy; // price, name, newest, rating
    private String sortDirection; // asc, desc
}