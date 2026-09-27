package com.davis.repository;

import com.davis.model.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntityRepository extends JpaRepository<Entity, Long> {

    @Query("SELECT e FROM Entity e WHERE e.caseEntity.caseId = :caseId")
    List<Entity> findEntitiesByCaseId(@Param("caseId") Long caseId);
}