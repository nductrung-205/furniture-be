package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.*;
import com.furniture.furniture_backend.entity.Category;
import com.furniture.furniture_backend.entity.Product;
import com.furniture.furniture_backend.repository.ProductRepository;
import com.furniture.furniture_backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho ProductService
 * Sử dụng Mockito để mock dependencies
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        // Setup category
        category = new Category();
        category.setId(1L);
        category.setName("Sofa");

        // Setup product
        product = new Product();
        product.setId(1L);
        product.setName("Modern Sofa");
        product.setDescription("Beautiful sofa");
        product.setPrice(new BigDecimal("5000000"));
        product.setDiscountPrice(new BigDecimal("4000000"));
        product.setStockQuantity(10);
        product.setIsAvailable(true);
        product.setCategory(category);
        product.setColor("Gray");
        product.setMaterial("Fabric");

        // Setup product request
        productRequest = new ProductRequest();
        productRequest.setName("Modern Sofa");
        productRequest.setDescription("Beautiful sofa");
        productRequest.setPrice(new BigDecimal("5000000"));
        productRequest.setStockQuantity(10);
        productRequest.setCategoryId(1L);
    }

    // ========== TEST GET METHODS ==========

    @Test
    void testGetAllProducts_ShouldReturnAllProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Modern Sofa");
        verify(productRepository).findAll();
    }

    @Test
    void testGetAvailableProducts_ShouldReturnOnlyAvailableProducts() {
        // Given
        when(productRepository.findByIsAvailableTrue()).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getAvailableProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Modern Sofa");
        verify(productRepository).findByIsAvailableTrue();
    }

    @Test
    void testGetProductById_WithValidId_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.getAverageRatingByProductId(1L)).thenReturn(4.5);
        when(reviewRepository.countByProductId(1L)).thenReturn(10L);

        // When
        ProductResponse result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Modern Sofa");
        assertThat(result.getAverageRating()).isEqualTo(4.5);
        assertThat(result.getReviewCount()).isEqualTo(10L);
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductById_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void testGetProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        when(productRepository.findByCategoryId(1L)).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getProductsByCategory(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void testSearchProducts_ShouldReturnMatchingProducts() {
        // Given
        when(productRepository.searchProducts("modern")).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.searchProducts("modern");

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).searchProducts("modern");
    }

    // ========== TEST ADVANCED METHODS ==========

    @Test
    void testFilterProducts_ShouldApplyAllFilters() {
        // Given
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategoryId(1L);
        filter.setMinPrice(new BigDecimal("3000000"));
        filter.setMaxPrice(new BigDecimal("6000000"));
        filter.setColor("Gray");
        filter.setMaterial("Fabric");

        when(productRepository.findByMultipleFilters(any(), any(), any(), any(), any()))
            .thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.filterProducts(filter);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByMultipleFilters(1L, 
            new BigDecimal("3000000"), 
            new BigDecimal("6000000"), 
            "Gray", 
            "Fabric");
    }

    @Test
    void testGetBestSellingProducts_ShouldLimitResults() {
        // Given
        when(productRepository.findBestSellingProducts()).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getBestSellingProducts(5);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findBestSellingProducts();
    }

    @Test
    void testGetHighRatedProducts_ShouldReturnHighRatedOnly() {
        // Given
        when(productRepository.findHighRatedProducts(4.0)).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getHighRatedProducts(4.0);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findHighRatedProducts(4.0);
    }

    @Test
    void testGetRelatedProducts_ShouldExcludeCurrentProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findRelatedProducts(1L, 1L)).thenReturn(Arrays.asList());
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getRelatedProducts(1L, 5);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository).findRelatedProducts(1L, 1L);
    }

    @Test
    void testGetLowStockProducts_ShouldReturnProductsBelowThreshold() {
        // Given
        when(productRepository.findLowStockProducts(10)).thenReturn(Arrays.asList(product));
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        List<ProductResponse> result = productService.getLowStockProducts(10);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findLowStockProducts(10);
    }

    @Test
    void testGetProductStatistics_ShouldReturnCorrectStats() {
        // Given
        Object[] stats = new Object[]{
            2L, // totalProducts
            100L, // totalStock
            new BigDecimal("5000000"), // avgPrice
            new BigDecimal("3000000"), // minPrice
            new BigDecimal("8000000")  // maxPrice
        };
        when(productRepository.getProductStatistics()).thenReturn(stats);

        // When
        ProductStatisticsDTO result = productService.getProductStatistics();

        // Then
        assertThat(result.getTotalProducts()).isEqualTo(2L);
        assertThat(result.getTotalStock()).isEqualTo(100L);
        assertThat(result.getAveragePrice()).isEqualTo(new BigDecimal("5000000"));
        verify(productRepository).getProductStatistics();
    }

    // ========== TEST CRUD OPERATIONS ==========

    @Test
    void testCreateProduct_ShouldSaveAndReturnProduct() {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(0.0);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(0L);

        // When
        ProductResponse result = productService.createProduct(productRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Modern Sofa");
        verify(categoryService).getCategoryById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_WithValidId_ShouldUpdateAndReturn() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(reviewRepository.getAverageRatingByProductId(anyLong())).thenReturn(4.5);
        when(reviewRepository.countByProductId(anyLong())).thenReturn(10L);

        // When
        ProductResponse result = productService.updateProduct(1L, productRequest);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(999L, productRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void testDeleteProduct_WithValidId_ShouldDelete() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }

    @Test
    void testDeleteProduct_WithInvalidId_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void testUpdateStock_WithSufficientStock_ShouldUpdateSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateStock(1L, 5);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(argThat(p -> 
            p.getStockQuantity() == 5 && p.getIsAvailable()
        ));
    }

    @Test
    void testUpdateStock_WithInsufficientStock_ShouldThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When & Then
        assertThatThrownBy(() -> productService.updateStock(1L, 15))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void testUpdateStock_ToZero_ShouldSetAvailableToFalse() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.updateStock(1L, 10);

        // Then
        verify(productRepository).save(argThat(p -> 
            p.getStockQuantity() == 0 && !p.getIsAvailable()
        ));
    }
}