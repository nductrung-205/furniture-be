package com.furniture.furniture_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDTO {
    private Long totalProducts;
    private Long totalStock;
    private Double averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}