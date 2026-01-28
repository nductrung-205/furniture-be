package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.RevenueReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ExcelExportService {
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    /**
     * Xuất báo cáo doanh thu ra file Excel
     */
    public byte[] exportRevenueReport(RevenueReportResponse report, String startDate, String endDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Tạo các sheet
            createSummarySheet(workbook, report, startDate, endDate);
            createTopProductsSheet(workbook, report);
            createPeriodRevenueSheet(workbook, report);
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    /**
     * Sheet 1: Tổng quan
     */
    private void createSummarySheet(Workbook workbook, RevenueReportResponse report, String startDate, String endDate) {
        Sheet sheet = workbook.createSheet("Tổng quan");
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle labelStyle = createLabelStyle(workbook);
        CellStyle valueStyle = createValueStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");
        titleCell.setCellStyle(titleStyle);
        rowNum++; // Empty row
        
        // Thời gian báo cáo
        createLabelValueRow(sheet, rowNum++, "Từ ngày:", startDate, labelStyle, valueStyle);
        createLabelValueRow(sheet, rowNum++, "Đến ngày:", endDate, labelStyle, valueStyle);
        createLabelValueRow(sheet, rowNum++, "Ngày xuất:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), labelStyle, valueStyle);
        rowNum++; // Empty row
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("CHỈ SỐ TỔNG QUAN");
        headerCell.setCellStyle(headerStyle);
        
        // Các chỉ số
        createMetricRow(sheet, rowNum++, "Tổng doanh thu:", report.getTotalRevenue(), labelStyle, currencyStyle);
        createMetricRow(sheet, rowNum++, "Tổng lợi nhuận:", report.getTotalProfit(), labelStyle, currencyStyle);
        createLabelValueRow(sheet, rowNum++, "Số đơn hàng:", String.valueOf(report.getTotalOrders()), labelStyle, valueStyle);
        createLabelValueRow(sheet, rowNum++, "Tổng sản phẩm đã bán:", String.valueOf(report.getTotalProductsSold()), labelStyle, valueStyle);
        createMetricRow(sheet, rowNum++, "Giá trị đơn hàng TB:", report.getAverageOrderValue(), labelStyle, currencyStyle);
        
        // Set column widths
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);
    }
    
    /**
     * Sheet 2: Top sản phẩm bán chạy
     */
    private void createTopProductsSheet(Workbook workbook, RevenueReportResponse report) {
        Sheet sheet = workbook.createSheet("Top sản phẩm");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Mã SP", "Tên sản phẩm", "Số lượng bán", "Doanh thu"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        int stt = 1;
        for (RevenueReportResponse.TopSellingProduct product : report.getTopSellingProducts()) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(stt++);
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(product.getProductId());
            cell1.setCellStyle(centerStyle);
            
            row.createCell(2).setCellValue(product.getProductName());
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(product.getQuantitySold());
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(product.getRevenue().doubleValue());
            cell4.setCellStyle(currencyStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Sheet 3: Doanh thu theo kỳ
     */
    private void createPeriodRevenueSheet(Workbook workbook, RevenueReportResponse report) {
        Sheet sheet = workbook.createSheet("Doanh thu theo kỳ");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        
        int rowNum = 0;
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Kỳ", "Doanh thu", "Lợi nhuận", "Số đơn hàng", "SP đã bán"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        for (RevenueReportResponse.RevenueByPeriod period : report.getRevenueByPeriods()) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(period.getPeriod());
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(period.getRevenue().doubleValue());
            cell1.setCellStyle(currencyStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(period.getProfit().doubleValue());
            cell2.setCellStyle(currencyStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(period.getOrderCount());
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(period.getProductsSold());
            cell4.setCellStyle(centerStyle);
        }
        
        // Total row
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("TỔNG CỘNG");
        totalLabelCell.setCellStyle(headerStyle);
        
        // Sử dụng công thức SUM để tính tổng
        Cell totalRevenueCell = totalRow.createCell(1);
        totalRevenueCell.setCellFormula(String.format("SUM(B2:B%d)", rowNum));
        totalRevenueCell.setCellStyle(currencyStyle);
        
        Cell totalProfitCell = totalRow.createCell(2);
        totalProfitCell.setCellFormula(String.format("SUM(C2:C%d)", rowNum));
        totalProfitCell.setCellStyle(currencyStyle);
        
        Cell totalOrdersCell = totalRow.createCell(3);
        totalOrdersCell.setCellFormula(String.format("SUM(D2:D%d)", rowNum));
        totalOrdersCell.setCellStyle(centerStyle);
        
        Cell totalProductsCell = totalRow.createCell(4);
        totalProductsCell.setCellFormula(String.format("SUM(E2:E%d)", rowNum));
        totalProductsCell.setCellStyle(centerStyle);
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // ========== Helper Methods ==========
    
    private void createLabelValueRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
    }
    
    private void createMetricRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value.doubleValue());
        valueCell.setCellStyle(valueStyle);
    }
    
    // ========== Styles ==========
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }
    
    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }
}