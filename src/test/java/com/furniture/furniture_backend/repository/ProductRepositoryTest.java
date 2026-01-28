package com.furniture.furniture_backend.repository;

import com.furniture.furniture_backend.entity.Category;
import com.furniture.furniture_backend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit Test cho ProductRepository
 * Sử dụng @DataJpaTest để test với H2 in-memory database
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category category;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Tạo category
        category = new Category();
        category.setName("Sofa");
        category.setDescription("Living room sofas");
        category = entityManager.persistAndFlush(category);

        // Tạo sản phẩm 1
        product1 = new Product();
        product1.setName("Modern Sofa");
        product1.setDescription("Beautiful modern sofa");
        product1.setPrice(new BigDecimal("5000000"));
        product1.setDiscountPrice(new BigDecimal("4000000"));
        product1.setStockQuantity(10);
        product1.setIsAvailable(true);
        product1.setCategory(category);
        product1.setColor("Gray");
        product1.setMaterial("Fabric");
        product1 = entityManager.persistAndFlush(product1);

        // Tạo sản phẩm 2
        product2 = new Product();
        product2.setName("Luxury Sofa");
        product2.setDescription("Premium luxury sofa");
        product2.setPrice(new BigDecimal("8000000"));
        product2.setStockQuantity(5);
        product2.setIsAvailable(true);
        product2.setCategory(category);
        product2.setColor("Black");
        product2.setMaterial("Leather");
        product2 = entityManager.persistAndFlush(product2);
    }

    // ========== TEST CÁC PHƯƠNG THỨC CƠ BẢN ==========

    @Test
    void testFindByCategoryId_ShouldReturnProducts() {
        // When
        List<Product> products = productRepository.findByCategoryId(category.getId());

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
            .containsExactlyInAnyOrder("Modern Sofa", "Luxury Sofa");
    }

    @Test
    void testFindByIsAvailableTrue_ShouldReturnOnlyAvailableProducts() {
        // Given - Tạo sản phẩm không available
        Product unavailableProduct = new Product();
        unavailableProduct.setName("Out of Stock Sofa");
        unavailableProduct.setPrice(new BigDecimal("3000000"));
        unavailableProduct.setStockQuantity(0);
        unavailableProduct.setIsAvailable(false);
        unavailableProduct.setCategory(category);
        entityManager.persistAndFlush(unavailableProduct);

        // When
        List<Product> products = productRepository.findByIsAvailableTrue();

        // Then
        assertThat(products).hasSize(2);
        assertThat(products).allMatch(Product::getIsAvailable);
    }

    @Test
    void testFindByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // When
        List<Product> products = productRepository.findByNameContainingIgnoreCase("modern");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
    }

    @Test
    void testFindByPriceBetween_ShouldReturnProductsInPriceRange() {
        // When
        List<Product> products = productRepository.findByPriceBetween(
            new BigDecimal("4000000"), 
            new BigDecimal("6000000")
        );

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
    }

    @Test
    void testFindByDiscountPriceIsNotNull_ShouldReturnOnlyDiscountedProducts() {
        // When
        List<Product> products = productRepository.findByDiscountPriceIsNotNull();

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
        assertThat(products.get(0).getDiscountPrice()).isNotNull();
    }

    // ========== TEST TRUY VẤN PHỨC TẠP ==========

    @Test
    void testSearchProducts_ShouldSearchInNameAndDescription() {
        // When
        List<Product> products = productRepository.searchProducts("luxury");

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Luxury Sofa");
    }

    @Test
    void testFindByCategoryAndPriceRange_ShouldReturnFilteredProducts() {
        // When
        List<Product> products = productRepository.findByCategoryAndPriceRange(
            category.getId(),
            new BigDecimal("4000000"),
            new BigDecimal("6000000")
        );

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
    }

    @Test
    void testFindLowStockProducts_ShouldReturnProductsBelowThreshold() {
        // When
        List<Product> products = productRepository.findLowStockProducts(7);

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Luxury Sofa");
        assertThat(products.get(0).getStockQuantity()).isLessThanOrEqualTo(7);
    }

    @Test
    void testFindByMultipleFilters_WithAllFilters_ShouldReturnMatchingProducts() {
        // When
        List<Product> products = productRepository.findByMultipleFilters(
            category.getId(),
            new BigDecimal("4000000"),
            new BigDecimal("6000000"),
            "Gray",
            "Fabric"
        );

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
    }

    @Test
    void testFindByMultipleFilters_WithNullFilters_ShouldReturnAllProducts() {
        // When
        List<Product> products = productRepository.findByMultipleFilters(
            null, null, null, null, null
        );

        // Then
        assertThat(products).hasSize(2);
    }

    @Test
    void testFindByFinalPriceRange_ShouldConsiderDiscountPrice() {
        // When - Tìm theo giá sau discount
        List<Product> products = productRepository.findByFinalPriceRange(
            new BigDecimal("3500000"),
            new BigDecimal("4500000")
        );

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Modern Sofa");
        assertThat(products.get(0).getDiscountPrice()).isNotNull();
    }

    // @Test
    // void testGetProductStatistics_ShouldReturnCorrectStats() {
    //     // When
    //     Object[] stats = productRepository.getProductStatistics();

    //     // Then
    //     assertThat(stats).isNotNull();
    //     assertThat(((Number) stats[0]).longValue()).isEqualTo(2); // totalProducts
    //     assertThat(((Number) stats[1]).longValue()).isEqualTo(15); // totalStock
    //     assertThat((BigDecimal) stats[2]).isNotNull(); // averagePrice
    //     assertThat((BigDecimal) stats[3]).isEqualTo(new BigDecimal("5000000")); // minPrice
    //     assertThat((BigDecimal) stats[4]).isEqualTo(new BigDecimal("8000000")); // maxPrice
    // }

    @Test
    void testFindRelatedProducts_ShouldExcludeCurrentProduct() {
        // Given - Thêm sản phẩm thứ 3 cùng category
        Product product3 = new Product();
        product3.setName("Classic Sofa");
        product3.setPrice(new BigDecimal("6000000"));
        product3.setStockQuantity(8);
        product3.setIsAvailable(true);
        product3.setCategory(category);
        entityManager.persistAndFlush(product3);

        // When
        List<Product> relatedProducts = productRepository.findRelatedProducts(
            category.getId(), 
            product1.getId()
        );

        // Then
        assertThat(relatedProducts).hasSize(2);
        assertThat(relatedProducts).extracting(Product::getName)
            .containsExactlyInAnyOrder("Classic Sofa", "Luxury Sofa")
            .doesNotContain("Modern Sofa");
    }

    // ========== TEST EDGE CASES ==========

    @Test
    void testFindByCategoryId_WithInvalidCategory_ShouldReturnEmptyList() {
        // When
        List<Product> products = productRepository.findByCategoryId(999L);

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void testSearchProducts_WithNoMatch_ShouldReturnEmptyList() {
        // When
        List<Product> products = productRepository.searchProducts("nonexistent");

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    void testFindLowStockProducts_WithZeroThreshold_ShouldReturnEmptyList() {
        // When
        List<Product> products = productRepository.findLowStockProducts(0);

        // Then
        assertThat(products).isEmpty();
    }
}