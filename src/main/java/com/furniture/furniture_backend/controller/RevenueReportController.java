package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.RevenueReportRequest;
import com.furniture.furniture_backend.dto.RevenueReportResponse;
import com.furniture.furniture_backend.service.ExcelExportService;
import com.furniture.furniture_backend.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // Chỉ Admin mới được truy cập
public class RevenueReportController {
    
    private final RevenueReportService revenueReportService;
    private final ExcelExportService excelExportService;
    
    /**
     * Lấy báo cáo doanh thu (JSON)
     * GET /api/reports/revenue?startDate=2024-01-01&endDate=2024-12-31&reportType=MONTH
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam RevenueReportRequest.ReportType reportType
    ) {
        try {
            RevenueReportRequest request = new RevenueReportRequest();
            request.setStartDate(LocalDate.parse(startDate));
            request.setEndDate(LocalDate.parse(endDate));
            request.setReportType(reportType);
            
            RevenueReportResponse report = revenueReportService.generateReport(request);
            
            return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", report));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo báo cáo: " + e.getMessage()));
        }
    }
    
    /**
     * Xuất báo cáo doanh thu ra file Excel
     * GET /api/reports/revenue/export?startDate=2024-01-01&endDate=2024-12-31&reportType=MONTH
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportRevenueReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam RevenueReportRequest.ReportType reportType
    ) {
        try {
            // Tạo báo cáo
            RevenueReportRequest request = new RevenueReportRequest();
            request.setStartDate(LocalDate.parse(startDate));
            request.setEndDate(LocalDate.parse(endDate));
            request.setReportType(reportType);
            
            RevenueReportResponse report = revenueReportService.generateReport(request);
            
            // Xuất Excel
            byte[] excelBytes = excelExportService.exportRevenueReport(report, startDate, endDate);
            ByteArrayResource resource = new ByteArrayResource(excelBytes);
            
            // Tạo tên file
            String fileName = String.format("Bao_Cao_Doanh_Thu_%s_%s.xlsx", 
                    startDate.replace("-", ""), 
                    endDate.replace("-", ""));
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(excelBytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xuất báo cáo: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy báo cáo nhanh (tháng này, quý này, năm này)
     * GET /api/reports/revenue/quick?period=THIS_MONTH
     */
    @GetMapping("/quick")
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getQuickReport(
            @RequestParam QuickPeriod period
    ) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate;
            LocalDate endDate = today;
            RevenueReportRequest.ReportType reportType;
            
            switch (period) {
                case TODAY:
                    startDate = today;
                    reportType = RevenueReportRequest.ReportType.DAY;
                    break;
                    
                case THIS_WEEK:
                    startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    reportType = RevenueReportRequest.ReportType.DAY;
                    break;
                    
                case THIS_MONTH:
                    startDate = today.withDayOfMonth(1);
                    reportType = RevenueReportRequest.ReportType.DAY;
                    break;
                    
                case THIS_QUARTER:
                    int currentMonth = today.getMonthValue();
                    int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    startDate = today.withMonth(quarterStartMonth).withDayOfMonth(1);
                    reportType = RevenueReportRequest.ReportType.MONTH;
                    break;
                    
                case THIS_YEAR:
                    startDate = today.withDayOfYear(1);
                    reportType = RevenueReportRequest.ReportType.MONTH;
                    break;
                    
                default:
                    startDate = today.withDayOfMonth(1);
                    reportType = RevenueReportRequest.ReportType.DAY;
            }
            
            RevenueReportRequest request = new RevenueReportRequest(startDate, endDate, reportType);
            RevenueReportResponse report = revenueReportService.generateReport(request);
            
            return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo thành công", report));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi tạo báo cáo: " + e.getMessage()));
        }
    }
    
    public enum QuickPeriod {
        TODAY,
        THIS_WEEK,
        THIS_MONTH,
        THIS_QUARTER,
        THIS_YEAR
    }
}