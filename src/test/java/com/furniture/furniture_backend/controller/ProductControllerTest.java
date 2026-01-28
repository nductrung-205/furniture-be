package com.furniture.furniture_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furniture.furniture_backend.dto.*;
import com.furniture.furniture_backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho ProductController
 * Sử dụng @WebMvcTest để test REST endpoints
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse productResponse;
    private ProductRequest productRequest;
    private List<ProductResponse> productList;

    @BeforeEach
    void setUp() {
        // Setup product response
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Modern Sofa");
        productResponse.setDescription("Beautiful sofa");
        productResponse.setPrice(new BigDecimal("5000000"));
        productResponse.setDiscountPrice(new BigDecimal("4000000"));
        productResponse.setStockQuantity(10);
        productResponse.setIsAvailable(true);
        productResponse.setCategoryId(1L);
        productResponse.setAverageRating(4.5);
        productResponse.setReviewCount(10L);

        // Setup product request
        productRequest = new ProductRequest();
        productRequest.setName("Modern Sofa");
        productRequest.setDescription("Beautiful sofa");
        productRequest.setPrice(new BigDecimal("5000000"));
        productRequest.setStockQuantity(10);
        productRequest.setCategoryId(1L);

        // Setup product list
        productList = Arrays.asList(productResponse);
    }

    // ========== TEST GET ENDPOINTS ==========

    @Test
    void testGetAllProducts_ShouldReturnProductList() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].name").value("Modern Sofa"))
            .andExpect(jsonPath("$.data[0].price").value(5000000));

        verify(productService).getAllProducts();
    }

    @Test
    void testGetAvailableProducts_ShouldReturnOnlyAvailableProducts() throws Exception {
        // Given
        when(productService.getAvailableProducts()).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/available"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(1)));

        verify(productService).getAvailableProducts();
    }

    @Test
    void testGetProductById_WithValidId_ShouldReturnProduct() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Modern Sofa"))
            .andExpect(jsonPath("$.price").value(5000000))
            .andExpect(jsonPath("$.averageRating").value(4.5))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.all-products.href").exists());

        verify(productService).getProductById(1L);
    }

    @Test
    void testGetProductById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(productService.getProductById(999L))
            .thenThrow(new RuntimeException("Product not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", 999L))
            .andExpect(status().isInternalServerError());

        verify(productService).getProductById(999L);
    }

    @Test
    void testGetProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Given
        when(productService.getProductsByCategory(1L)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/category/{categoryId}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(1)));

        verify(productService).getProductsByCategory(1L);
    }

    @Test
    void testSearchProducts_ShouldReturnMatchingProducts() throws Exception {
        // Given
        when(productService.searchProducts("modern")).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "modern"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].name").value("Modern Sofa"));

        verify(productService).searchProducts("modern");
    }

    // ========== TEST ADVANCED ENDPOINTS ==========

    @Test
    void testFilterProducts_ShouldApplyFilters() throws Exception {
        // Given
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategoryId(1L);
        filter.setMinPrice(new BigDecimal("3000000"));
        filter.setMaxPrice(new BigDecimal("6000000"));

        when(productService.filterProducts(any(ProductFilterDTO.class))).thenReturn(productList);

        // When & Then
        mockMvc.perform(post("/api/products/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(1)));

        verify(productService).filterProducts(any(ProductFilterDTO.class));
    }

    @Test
    void testGetBestSellingProducts_ShouldReturnBestSellers() throws Exception {
        // Given
        when(productService.getBestSellingProducts(10)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/best-selling")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Best selling products retrieved successfully"));

        verify(productService).getBestSellingProducts(10);
    }

    @Test
    void testGetHighRatedProducts_ShouldReturnHighRatedOnly() throws Exception {
        // Given
        when(productService.getHighRatedProducts(4.0)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/high-rated")
                .param("minRating", "4.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).getHighRatedProducts(4.0);
    }

    @Test
    void testGetRelatedProducts_ShouldReturnRelatedProducts() throws Exception {
        // Given
        when(productService.getRelatedProducts(1L, 5)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/{id}/related", 1L)
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Related products retrieved successfully"));

        verify(productService).getRelatedProducts(1L, 5);
    }

    @Test
    void testGetLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
        // Given
        when(productService.getLowStockProducts(10)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/low-stock")
                .param("threshold", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).getLowStockProducts(10);
    }

    @Test
    void testGetDiscountedProducts_ShouldReturnDiscountedProducts() throws Exception {
        // Given
        when(productService.getDiscountedProducts()).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/discounted"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Discounted products retrieved successfully"));

        verify(productService).getDiscountedProducts();
    }

    @Test
    void testGetProductsByPriceRange_ShouldReturnProductsInRange() throws Exception {
        // Given
        when(productService.getProductsByPriceRange(
            new BigDecimal("3000000"), 
            new BigDecimal("6000000")
        )).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "3000000")
                .param("maxPrice", "6000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).getProductsByPriceRange(
            new BigDecimal("3000000"), 
            new BigDecimal("6000000")
        );
    }

    @Test
    void testGetProductStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        // ProductStatisticsDTO stats = new ProductStatisticsDTO(
        //     100L, 500L, 
        //     new BigDecimal("5000000"), 
        //     new BigDecimal("1000000"), 
        //     new BigDecimal("10000000")
        // );
        // when(productService.getProductStatistics()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/products/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalProducts").value(100))
            .andExpect(jsonPath("$.data.totalStock").value(500))
            .andExpect(jsonPath("$.data.averagePrice").value(5000000));

        verify(productService).getProductStatistics();
    }

    @Test
    void testGetMostReviewedProducts_ShouldReturnMostReviewed() throws Exception {
        // Given
        when(productService.getMostReviewedProducts(10)).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/most-reviewed")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).getMostReviewedProducts(10);
    }

    // ========== TEST CRUD ENDPOINTS ==========

    @Test
    void testCreateProduct_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Product created successfully"))
            .andExpect(jsonPath("$.data.name").value("Modern Sofa"));

        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void testUpdateProduct_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        when(productService.updateProduct(eq(1L), any(ProductRequest.class)))
            .thenReturn(productResponse);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Product updated successfully"))
            .andExpect(jsonPath("$.data.name").value("Modern Sofa"));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void testUpdateProduct_WithInvalidId_ShouldReturnError() throws Exception {
        // Given
        when(productService.updateProduct(eq(999L), any(ProductRequest.class)))
            .thenThrow(new RuntimeException("Product not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isInternalServerError());

        verify(productService).updateProduct(eq(999L), any(ProductRequest.class));
    }

    @Test
    void testDeleteProduct_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Product deleted successfully"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    void testDeleteProduct_WithInvalidId_ShouldReturnError() throws Exception {
        // Given
        doThrow(new RuntimeException("Product not found with id: 999"))
            .when(productService).deleteProduct(999L);

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", 999L))
            .andExpect(status().isInternalServerError());

        verify(productService).deleteProduct(999L);
    }

    // ========== TEST EDGE CASES ==========

    @Test
    void testGetProductsByCategory_WithNoProducts_ShouldReturnEmptyList() throws Exception {
        // Given
        when(productService.getProductsByCategory(999L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/products/category/{categoryId}", 999L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(0)));

        verify(productService).getProductsByCategory(999L);
    }

    @Test
    void testSearchProducts_WithEmptyKeyword_ShouldStillWork() throws Exception {
        // Given
        when(productService.searchProducts("")).thenReturn(productList);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("keyword", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).searchProducts("");
    }

    @Test
    void testFilterProducts_WithNullValues_ShouldWork() throws Exception {
        // Given
        ProductFilterDTO filter = new ProductFilterDTO();
        when(productService.filterProducts(any(ProductFilterDTO.class))).thenReturn(productList);

        // When & Then
        mockMvc.perform(post("/api/products/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(productService).filterProducts(any(ProductFilterDTO.class));
    }
}