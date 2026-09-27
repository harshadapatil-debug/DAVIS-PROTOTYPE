package com.davis.service;

import com.davis.model.Case;
import com.davis.repository.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public List<Case> getAllCases() {
        return caseRepository.findAll();
    }

    public Optional<Case> getCaseById(Long caseId) {
        return caseRepository.findById(caseId);
    }

    public Case saveCase(Case caseEntity) {

        // Generate creation timestamp for a new case
        if (caseEntity.getCreatedAt() == null) {
            caseEntity.setCreatedAt(LocalDateTime.now());
        }

        return caseRepository.save(caseEntity);
    }

    public void deleteCase(Long caseId) {
        caseRepository.deleteById(caseId);
    }
}