package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.*;
import com.furniture.furniture_backend.entity.Category;
import com.furniture.furniture_backend.entity.Product;
import com.furniture.furniture_backend.repository.ProductRepository;
import com.furniture.furniture_backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ReviewRepository reviewRepository;

    // ========== PHƯƠNG THỨC PHÂN TRANG ==========

    public PageResponse<ProductResponse> getAllProductsPaginated(
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        return convertToPageResponse(productPage);
    }

    public PageResponse<ProductResponse> searchProductsPaginated(
            String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);

        return convertToPageResponse(productPage);
    }

    public PageResponse<ProductResponse> filterProductsPaginated(
            Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String keyword, Boolean isAvailable,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByFilters(
                categoryId, minPrice, maxPrice, keyword, isAvailable, pageable);

        return convertToPageResponse(productPage);
    }

    // ========== PHƯƠNG THỨC CƠ BẢN (GIỮ NGUYÊN) ==========

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToResponse(product);
    }

    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========== PHƯƠNG THỨC NÂNG CAO (GIỮ NGUYÊN) ==========

    public List<ProductResponse> filterProducts(ProductFilterDTO filter) {
        return productRepository.findByMultipleFilters(
                filter.getCategoryId(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.getColor(),
                filter.getMaterial()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getBestSellingProducts(int limit) {
        return productRepository.findBestSellingProducts().stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getHighRatedProducts(Double minRating) {
        return productRepository.findHighRatedProducts(minRating).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return productRepository.findRelatedProducts(
                product.getCategory().getId(),
                productId).stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getDiscountedProducts() {
        return productRepository.findByDiscountPriceIsNotNull().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProductStatisticsDTO getProductStatistics() {
        Object result = productRepository.getProductStatistics();

        // Kiểm tra nếu không có dữ liệu
        if (result == null) {
            return new ProductStatisticsDTO(0L, 0L, 0.0, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Ép kiểu kết quả về mảng Object
        Object[] stats = (Object[]) result;

        return new ProductStatisticsDTO(
                stats[0] != null ? ((Number) stats[0]).longValue() : 0L, // COUNT
                stats[1] != null ? ((Number) stats[1]).longValue() : 0L, // SUM
                stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0, // AVG (Thường là Double)
                stats[3] != null ? (BigDecimal) stats[3] : BigDecimal.ZERO, // MIN (BigDecimal)
                stats[4] != null ? (BigDecimal) stats[4] : BigDecimal.ZERO // MAX (BigDecimal)
        );
    }

    public List<ProductResponse> getMostReviewedProducts(int limit) {
        return productRepository.findMostReviewedProducts().stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // ========== PHƯƠNG THỨC CRUD ==========

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getCategoryById(request.getCategoryId());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsAvailable(request.getStockQuantity() > 0);
        product.setCategory(category);
        product.setImageUrls(request.getImageUrls());
        product.setMaterial(request.getMaterial());
        product.setColor(request.getColor());
        product.setDimensions(request.getDimensions());
        product.setWeight(request.getWeight());

        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Category category = categoryService.getCategoryById(request.getCategoryId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsAvailable(request.getStockQuantity() > 0);
        product.setCategory(category);
        product.setImageUrls(request.getImageUrls());
        product.setMaterial(request.getMaterial());
        product.setColor(request.getColor());
        product.setDimensions(request.getDimensions());
        product.setWeight(request.getWeight());

        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(newStock);
        product.setIsAvailable(newStock > 0);
        productRepository.save(product);
    }

    @Transactional
    public void importProductsFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua tiêu đề (dòng 0)
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            Product product = new Product();
            product.setName(row.getCell(0).getStringCellValue());
            product.setDescription(row.getCell(1).getStringCellValue());
            product.setPrice(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()));
            product.setDiscountPrice(BigDecimal.valueOf(row.getCell(3).getNumericCellValue()));
            product.setStockQuantity((int) row.getCell(4).getNumericCellValue());

            // Giả sử cột 5 là Category ID
            Long categoryId = (long) row.getCell(5).getNumericCellValue();
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);

            product.setMaterial(row.getCell(6).getStringCellValue());
            product.setColor(row.getCell(7).getStringCellValue());
            product.setDimensions(row.getCell(8).getStringCellValue());
            product.setWeight(row.getCell(9).getStringCellValue());

            // Xử lý ảnh (giả sử danh sách URL cách nhau bởi dấu phẩy)
            String images = row.getCell(10).getStringCellValue();
            if (images != null && !images.isEmpty()) {
                product.setImageUrls(List.of(images.split(",")));
            }

            productRepository.save(product);
        }
        workbook.close();
    }

    public byte[] generateExcelTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Template");
        Row header = sheet.createRow(0);
        String[] columns = { "Tên", "Mô tả", "Giá", "Giá giảm", "Kho", "CategoryID", "Chất liệu", "Màu sắc",
                "Kích thước", "Cân nặng", "URLs Ảnh (cách nhau dấu ,)" };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    @Transactional
    public void deleteMultipleProducts(List<Long> ids) {
        productRepository.deleteAllByIdInBatch(ids);
    }

    // ========== HELPER ==========

    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = ProductResponse.fromProduct(product);

        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        Long reviewCount = reviewRepository.countByProductId(product.getId());

        response.setAverageRating(avgRating != null ? avgRating : 0.0);
        response.setReviewCount(reviewCount);

        return response;
    }

    private PageResponse<ProductResponse> convertToPageResponse(Page<Product> productPage) {
        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast());
    }
}