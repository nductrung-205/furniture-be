package com.furniture.furniture_backend.service;

import com.furniture.furniture_backend.dto.ReviewRequest;
import com.furniture.furniture_backend.dto.ReviewResponse;
import com.furniture.furniture_backend.entity.Product;
import com.furniture.furniture_backend.entity.Review;
import com.furniture.furniture_backend.entity.User;
import com.furniture.furniture_backend.repository.ProductRepository;
import com.furniture.furniture_backend.repository.ReviewRepository;
import com.furniture.furniture_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        return convertToResponse(savedReview);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }

    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    // Hàm phụ trợ chuyển đổi Entity sang DTO
    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        // Lấy tên User từ quan hệ ManyToOne
        if (review.getUser() != null) {
            response.setUserName(review.getUser().getFullName());
        }
        return response;
    }
}