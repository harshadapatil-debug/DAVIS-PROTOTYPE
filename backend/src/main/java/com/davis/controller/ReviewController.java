package com.davis.controller;

import com.davis.model.Review;
import com.davis.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/reviews")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/api/reviews/{findingId}")
    public ResponseEntity<Review> getReviewById(
            @PathVariable Long findingId) {

        return reviewService.getReviewById(findingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<Review> createReview(
            @RequestBody Review review) {

        Review savedReview = reviewService.saveReview(review);
        return ResponseEntity.ok(savedReview);
    }

    @DeleteMapping("/api/reviews/{findingId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long findingId) {

        if (reviewService.getReviewById(findingId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        reviewService.deleteReview(findingId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/cases/{caseId}/review")
    public ResponseEntity<Review> updateCaseReview(
            @PathVariable Long caseId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        String note = request.get("note");

        Review updatedReview = reviewService.updateCaseReview(
                caseId,
                status,
                note
        );

        return ResponseEntity.ok(updatedReview);
    }
}