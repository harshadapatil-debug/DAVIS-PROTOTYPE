package com.davis.repository;

import com.davis.model.ConfidenceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConfidenceScoreRepository extends JpaRepository<ConfidenceScore, Long> {

    @Query("""
            SELECT c
            FROM ConfidenceScore c
            WHERE c.caseEntity.caseId = :caseId
            """)
    Optional<ConfidenceScore> findConfidenceByCaseId(
            @Param("caseId") Long caseId);
}