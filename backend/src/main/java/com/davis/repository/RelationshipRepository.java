package com.davis.repository;

import com.davis.model.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    @Query("SELECT r FROM Relationship r WHERE r.caseEntity.caseId = :caseId")
    List<Relationship> findRelationshipsByCaseId(@Param("caseId") Long caseId);
}