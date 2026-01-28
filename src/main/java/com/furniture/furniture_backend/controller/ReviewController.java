package com.furniture.furniture_backend.controller;

import com.furniture.furniture_backend.dto.ApiResponse;
import com.furniture.furniture_backend.dto.ReviewRequest;
import com.furniture.furniture_backend.dto.ReviewResponse;
import com.furniture.furniture_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProductId(
            @PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product reviews retrieved successfully", reviews));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByUserId(
            @PathVariable Long userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User reviews retrieved successfully", reviews));
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getAverageRating(
            @PathVariable Long productId) {
        Double avgRating = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Average rating retrieved successfully",
                        Map.of("averageRating", avgRating)));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long userId,
            @Valid @RequestBody ReviewRequest request) { // Thêm @Valid ở đây
        ReviewResponse review = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Review created successfully", review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(
                ApiResponse.success("Review deleted successfully", null));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReviewResponse> reviews = reviewService.getAllReviews(
                PageRequest.of(page, size, Sort.by("createdAt").descending())).map(review -> {
                    ReviewResponse res = new ReviewResponse();
                    res.setId(review.getId());
                    res.setRating(review.getRating());
                    res.setComment(review.getComment());
                    res.setCreatedAt(review.getCreatedAt());
                    res.setUserName(review.getUser().getFullName());
                    // Thêm tên sản phẩm vào DTO (Bạn có thể thêm field productName vào
                    // ReviewResponse)
                    return res;
                });
        return ResponseEntity.ok(ApiResponse.success("All reviews retrieved", reviews));
    }
}