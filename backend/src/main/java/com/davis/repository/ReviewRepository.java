package com.davis.repository;

import com.davis.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
            SELECT r
            FROM Review r
            WHERE r.caseEntity.caseId = :caseId
            """)
    Optional<Review> findReviewByCaseId(
            @Param("caseId") Long caseId);
}