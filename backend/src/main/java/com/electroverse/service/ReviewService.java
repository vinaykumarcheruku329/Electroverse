package com.electroverse.service;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.CatalogResponses;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Product;
import com.electroverse.entity.Review;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.ReviewStatus;
import com.electroverse.exception.ConflictException;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.ProductRepository;
import com.electroverse.repository.ReviewRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         SecurityUtils securityUtils) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public CatalogResponses.Paged<ShoppingResponses.Review> listByProduct(Long productId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                productId, ReviewStatus.APPROVED, pageable);
        return DtoMapper.toPaged(page, DtoMapper::toReview);
    }

    @Transactional
    public ShoppingResponses.Review create(Long productId, ShoppingRequests.Review request) {
        User user = securityUtils.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new ConflictException("You have already reviewed this product");
        }
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());
        review.setStatus(ReviewStatus.APPROVED);
        Review saved = reviewRepository.save(review);
        recomputeRating(product);
        return DtoMapper.toReview(saved);
    }

    @Transactional(readOnly = true)
    public List<ShoppingResponses.Review> listMine() {
        User user = securityUtils.getCurrentUser();
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .map(DtoMapper::toReview)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.electroverse.dto.response.AdminResponses.ReviewModeration> adminList() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DtoMapper::toReviewModeration)
                .toList();
    }

    @Transactional
    public com.electroverse.dto.response.AdminResponses.ReviewModeration moderate(Long id, ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + id));
        review.setStatus(status);
        Review saved = reviewRepository.save(review);
        recomputeRating(saved.getProduct());
        return DtoMapper.toReviewModeration(saved);
    }

    @Transactional
    public void delete(Long id, boolean admin) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + id));
        if (!admin && !review.getUser().getId().equals(securityUtils.getCurrentUserId())) {
            throw new ResourceNotFoundException("Review not found: " + id);
        }
        Product product = review.getProduct();
        reviewRepository.delete(review);
        recomputeRating(product);
    }

    private void recomputeRating(Product product) {
        List<Review> approved = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                product.getId(), ReviewStatus.APPROVED);
        if (approved.isEmpty()) {
            product.setRatingAverage(0.0);
            product.setRatingCount(0);
        } else {
            double avg = approved.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setRatingAverage(Math.round(avg * 10.0) / 10.0);
            product.setRatingCount(approved.size());
        }
        productRepository.save(product);
    }
}
