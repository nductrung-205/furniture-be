package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.RevenueReportRequest;
import com.furniture.furniture_backend.dto.RevenueReportResponse;
import com.furniture.furniture_backend.entity.Order;
import com.furniture.furniture_backend.entity.OrderItem;
import com.furniture.furniture_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueReportService {
    
    private final OrderRepository orderRepository;
    
    /**
     * Tạo báo cáo doanh thu theo yêu cầu
     */
    public RevenueReportResponse generateReport(RevenueReportRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);
        
        // Lấy tất cả đơn hàng đã thanh toán trong khoảng thời gian
        List<Order> orders = orderRepository.findByCreatedAtBetweenAndPaymentStatus(
                startDateTime, 
                endDateTime, 
                Order.PaymentStatus.PAID
        );
        
        // Tính toán các chỉ số tổng quan
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = orders.size();
        
        int totalProductsSold = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Tính lợi nhuận (giả sử lợi nhuận = doanh thu - giá vốn)
        // Ở đây đơn giản hóa: lợi nhuận = 30% doanh thu
        BigDecimal totalProfit = totalRevenue.multiply(BigDecimal.valueOf(0.3));
        
        // Top sản phẩm bán chạy
        List<RevenueReportResponse.TopSellingProduct> topProducts = getTopSellingProducts(orders, 10);
        
        // Doanh thu theo từng kỳ
        List<RevenueReportResponse.RevenueByPeriod> revenueByPeriods = 
                groupRevenueByPeriod(orders, request.getReportType());
        
        // Tạo response
        RevenueReportResponse response = new RevenueReportResponse();
        response.setTotalRevenue(totalRevenue);
        response.setTotalProfit(totalProfit);
        response.setTotalOrders(totalOrders);
        response.setTotalProductsSold(totalProductsSold);
        response.setAverageOrderValue(averageOrderValue);
        response.setTopSellingProducts(topProducts);
        response.setRevenueByPeriods(revenueByPeriods);
        
        return response;
    }
    
    /**
     * Lấy top sản phẩm bán chạy
     */
    private List<RevenueReportResponse.TopSellingProduct> getTopSellingProducts(List<Order> orders, int limit) {
        Map<Long, RevenueReportResponse.TopSellingProduct> productMap = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                Long productId = item.getProduct().getId();
                
                productMap.putIfAbsent(productId, new RevenueReportResponse.TopSellingProduct(
                        productId,
                        item.getProduct().getName(),
                        0,
                        BigDecimal.ZERO
                ));
                
                RevenueReportResponse.TopSellingProduct product = productMap.get(productId);
                product.setQuantitySold(product.getQuantitySold() + item.getQuantity());
                product.setRevenue(product.getRevenue().add(item.getSubtotal()));
            }
        }
        
        return productMap.values().stream()
                .sorted(Comparator.comparing(RevenueReportResponse.TopSellingProduct::getQuantitySold).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Nhóm doanh thu theo từng kỳ (ngày, tuần, tháng, năm)
     */
    private List<RevenueReportResponse.RevenueByPeriod> groupRevenueByPeriod(
            List<Order> orders, 
            RevenueReportRequest.ReportType reportType
    ) {
        Map<String, RevenueReportResponse.RevenueByPeriod> periodMap = new LinkedHashMap<>();
        
        for (Order order : orders) {
            String period = formatPeriod(order.getCreatedAt(), reportType);
            
            periodMap.putIfAbsent(period, new RevenueReportResponse.RevenueByPeriod(
                    period,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    0
            ));
            
            RevenueReportResponse.RevenueByPeriod periodData = periodMap.get(period);
            periodData.setRevenue(periodData.getRevenue().add(order.getTotalAmount()));
            periodData.setProfit(periodData.getProfit().add(order.getTotalAmount().multiply(BigDecimal.valueOf(0.3))));
            periodData.setOrderCount(periodData.getOrderCount() + 1);
            
            int productsSold = order.getOrderItems().stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
            periodData.setProductsSold(periodData.getProductsSold() + productsSold);
        }
        
        return new ArrayList<>(periodMap.values());
    }
    
    /**
     * Format thời gian theo loại báo cáo
     */
    private String formatPeriod(LocalDateTime dateTime, RevenueReportRequest.ReportType reportType) {
        LocalDate date = dateTime.toLocalDate();
        
        switch (reportType) {
            case DAY:
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE); // 2024-01-15
                
            case WEEK:
                int weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = date.get(IsoFields.WEEK_BASED_YEAR);
                return String.format("%d-W%02d", year, weekOfYear); // 2024-W03
                
            case MONTH:
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM")); // 2024-01
                
            case YEAR:
                return String.valueOf(date.getYear()); // 2024
                
            default:
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}