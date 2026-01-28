package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.*;
import com.furniture.furniture_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService productService;

    // ========== ENDPOINTS PHÂN TRANG ==========

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ProductResponse> products = productService.getAllProductsPaginated(
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/paginated/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProductsPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products = productService.searchProductsPaginated(
                keyword, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", products));
    }

    @GetMapping("/paginated/filter")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> filterProductsPaginated(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<ProductResponse> products = productService.filterProductsPaginated(
                categoryId, minPrice, maxPrice, keyword, isAvailable,
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("Filtered products retrieved successfully", products));
    }

    // ========== ENDPOINTS CƠ BẢN ==========

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailableProducts() {
        List<ProductResponse> products = productService.getAvailableProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Available products retrieved successfully", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);

        EntityModel<ProductResponse> resource = EntityModel.of(product);

        resource.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getProductById(id))
                .withSelfRel());

        resource.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts())
                .withRel("all-products"));

        resource.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getProductsByCategory(product.getCategoryId()))
                .withRel("category-products"));

        resource.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getRelatedProducts(id, 5))
                .withRel("related-products"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(
                ApiResponse.success("Products by category retrieved successfully", products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword) {
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", products));
    }

    // ========== ENDPOINTS NÂNG CAO ==========

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> filterProducts(
            @RequestBody ProductFilterDTO filter) {
        List<ProductResponse> products = productService.filterProducts(filter);
        return ResponseEntity.ok(
                ApiResponse.success("Filtered products retrieved successfully", products));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponse> products = productService.getBestSellingProducts(limit);
        return ResponseEntity.ok(
                ApiResponse.success("Best selling products retrieved successfully", products));
    }

    @GetMapping("/high-rated")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getHighRatedProducts(
            @RequestParam(defaultValue = "4.0") Double minRating) {
        List<ProductResponse> products = productService.getHighRatedProducts(minRating);
        return ResponseEntity.ok(
                ApiResponse.success("High rated products retrieved successfully", products));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRelatedProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductResponse> products = productService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(
                ApiResponse.success("Related products retrieved successfully", products));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(
                ApiResponse.success("Low stock products retrieved successfully", products));
    }

    @GetMapping("/discounted")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getDiscountedProducts() {
        List<ProductResponse> products = productService.getDiscountedProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Discounted products retrieved successfully", products));
    }

    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(
                ApiResponse.success("Products by price range retrieved successfully", products));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ProductStatisticsDTO>> getProductStatistics() {
        ProductStatisticsDTO stats = productService.getProductStatistics();
        return ResponseEntity.ok(
                ApiResponse.success("Product statistics retrieved successfully", stats));
    }

    @GetMapping("/most-reviewed")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMostReviewedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<ProductResponse> products = productService.getMostReviewedProducts(limit);
        return ResponseEntity.ok(
                ApiResponse.success("Most reviewed products retrieved successfully", products));
    }

    // ========== ENDPOINTS CRUD ==========

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> deleteMultiple(@RequestBody List<Long> ids) {
        productService.deleteMultipleProducts(ids);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }

    @PostMapping("/import-excel")
    public ResponseEntity<ApiResponse<Void>> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        productService.importProductsFromExcel(file);
        return ResponseEntity.ok(ApiResponse.success("Imported successfully", null));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] template = productService.generateExcelTemplate();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=product_template.xlsx")
                .body(template);
    }
}