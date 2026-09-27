package com.davis.service;

import com.davis.model.Review;
import com.davis.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long findingId) {
        return reviewRepository.findById(findingId);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(Long findingId) {
        reviewRepository.deleteById(findingId);
    }

    @Transactional
    public Review updateCaseReview(
            Long caseId,
            String status,
            String note) {

        Review review = reviewRepository
                .findReviewByCaseId(caseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Review not found for case: " + caseId));

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException(
                    "Invalid review status. Use PENDING, ACCEPTED, FLAGGED or REJECTED.");
        }

        review.setStatus(status.toUpperCase());
        review.setNote(note);

        return reviewRepository.save(review);
    }

    private boolean isValidStatus(String status) {

        if (status == null) {
            return false;
        }

        return switch (status.toUpperCase()) {
            case "PENDING", "ACCEPTED", "FLAGGED", "REJECTED" -> true;
            default -> false;
        };
    }
}