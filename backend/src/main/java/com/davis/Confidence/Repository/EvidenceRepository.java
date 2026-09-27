package com.davis.repository;

import com.davis.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    @Query("""
            SELECT e
            FROM Evidence e
            WHERE e.relationship.caseEntity.caseId = :caseId
            ORDER BY e.evidenceId ASC
            """)
    List<Evidence> findEvidenceByCaseId(
            @Param("caseId") Long caseId
    );
}