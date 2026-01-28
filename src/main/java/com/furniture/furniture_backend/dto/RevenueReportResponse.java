package com.furniture.furniture_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    
    private BigDecimal totalRevenue;           // Tổng doanh thu
    private BigDecimal totalProfit;            // Tổng lợi nhuận
    private Integer totalOrders;               // Số lượng đơn hàng
    private Integer totalProductsSold;         // Tổng số sản phẩm đã bán
    private BigDecimal averageOrderValue;      // Giá trị đơn hàng trung bình
    
    private List<TopSellingProduct> topSellingProducts;  // Top sản phẩm bán chạy
    private List<RevenueByPeriod> revenueByPeriods;      // Doanh thu theo từng kỳ
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingProduct {
        private Long productId;
        private String productName;
        private Integer quantitySold;
        private BigDecimal revenue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByPeriod {
        private String period;              // "2024-01-01", "2024-W01", "2024-01", "2024"
        private BigDecimal revenue;
        private BigDecimal profit;
        private Integer orderCount;
        private Integer productsSold;
    }
}