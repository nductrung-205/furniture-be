package com.furniture.furniture_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportRequest {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private ReportType reportType; // DAY, WEEK, MONTH, YEAR
    
    public enum ReportType {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}