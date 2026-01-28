package com.furniture.furniture_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.furniture.furniture_backend.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

       // ========== PHÂN TRANG ==========

       // Phân trang tất cả sản phẩm
       Page<Product> findAll(Pageable pageable);

       // Phân trang sản phẩm theo category
       Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

       // Phân trang sản phẩm available
       Page<Product> findByIsAvailableTrue(Pageable pageable);

       // Phân trang tìm kiếm
       @Query("SELECT p FROM Product p WHERE " +
                     "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

       // Phân trang với filter phức tạp
       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN p.category c " +
                     "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
                     "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                     "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                     "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                     "AND (:isAvailable IS NULL OR p.isAvailable = :isAvailable)")
       Page<Product> findByFilters(
                     @Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     @Param("keyword") String keyword,
                     @Param("isAvailable") Boolean isAvailable,
                     Pageable pageable);

       // ========== TRUY VẤN CƠ BẢN (GIỮ NGUYÊN) ==========

       List<Product> findByCategoryId(Long categoryId);

       List<Product> findByIsAvailableTrue();

       List<Product> findByNameContainingIgnoreCase(String name);

       List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

       List<Product> findByDiscountPriceIsNotNull();

       // ========== TRUY VẤN PHỨC TẠP (GIỮ NGUYÊN) ==========

       @Query("SELECT p FROM Product p WHERE " +
                     "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Product> searchProducts(@Param("keyword") String keyword);

       @Query("SELECT p FROM Product p JOIN p.category c WHERE " +
                     "c.id = :categoryId AND p.price BETWEEN :minPrice AND :maxPrice " +
                     "AND p.isAvailable = true")
       List<Product> findByCategoryAndPriceRange(
                     @Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice);

       @Query("SELECT p FROM Product p " +
                     "LEFT JOIN p.orderItems oi " +
                     "GROUP BY p.id " +
                     "ORDER BY COUNT(oi.id) DESC, p.createdAt DESC")
       List<Product> findBestSellingProducts();

       @Query("SELECT p FROM Product p " +
                     "LEFT JOIN p.reviews r " +
                     "GROUP BY p.id " +
                     "HAVING AVG(r.rating) >= :minRating " +
                     "ORDER BY AVG(r.rating) DESC")
       List<Product> findHighRatedProducts(@Param("minRating") Double minRating);

       @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
                     "AND p.createdAt = (SELECT MAX(p2.createdAt) FROM Product p2 " +
                     "WHERE p2.category.id = :categoryId)")
       List<Product> findNewestProductsByCategory(@Param("categoryId") Long categoryId);

       @Query(value = "SELECT c.name as category_name, COUNT(p.id) as product_count " +
                     "FROM categories c " +
                     "LEFT JOIN products p ON c.id = p.category_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY product_count DESC", nativeQuery = true)
       List<Object[]> countProductsByCategory();

       @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
                     "AND p.id != :productId AND p.isAvailable = true " +
                     "ORDER BY p.createdAt DESC")
       List<Product> findRelatedProducts(
                     @Param("categoryId") Long categoryId,
                     @Param("productId") Long productId);

       @Query("SELECT p FROM Product p WHERE " +
                     "CASE WHEN p.discountPrice IS NOT NULL THEN p.discountPrice " +
                     "ELSE p.price END BETWEEN :minPrice AND :maxPrice")
       List<Product> findByFinalPriceRange(
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice);

       @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 " +
                     "AND p.stockQuantity <= :threshold " +
                     "ORDER BY p.stockQuantity ASC")
       List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

       @Query("SELECT COUNT(p.id), SUM(p.stockQuantity), " +
                     "AVG(p.price), MIN(p.price), MAX(p.price) " +
                     "FROM Product p WHERE p.isAvailable = true")
       Object getProductStatistics();

       @Query("SELECT DISTINCT p FROM Product p " +
                     "LEFT JOIN p.category c " +
                     "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
                     "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                     "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
                     "AND (:color IS NULL OR LOWER(p.color) = LOWER(:color)) " +
                     "AND (:material IS NULL OR LOWER(p.material) = LOWER(:material)) " +
                     "AND p.isAvailable = true " +
                     "ORDER BY p.createdAt DESC")
       List<Product> findByMultipleFilters(
                     @Param("categoryId") Long categoryId,
                     @Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     @Param("color") String color,
                     @Param("material") String material);

       @Query("SELECT p FROM Product p " +
                     "LEFT JOIN p.reviews r " +
                     "GROUP BY p.id " +
                     "ORDER BY COUNT(r.id) DESC")
       List<Product> findMostReviewedProducts();
}