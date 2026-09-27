package com.davis.service;

import com.davis.model.Evidence;
import com.davis.repository.EvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;

    public EvidenceService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }

    public List<Evidence> getAllEvidence() {
        return evidenceRepository.findAll();
    }

    public Optional<Evidence> getEvidenceById(Long evidenceId) {
        return evidenceRepository.findById(evidenceId);
    }

    public List<Evidence> getEvidenceByCaseId(Long caseId) {
        return evidenceRepository.findEvidenceByCaseId(caseId);
    }

    public Evidence saveEvidence(Evidence evidence) {
        return evidenceRepository.save(evidence);
    }

    public void deleteEvidence(Long evidenceId) {
        evidenceRepository.deleteById(evidenceId);
    }
}