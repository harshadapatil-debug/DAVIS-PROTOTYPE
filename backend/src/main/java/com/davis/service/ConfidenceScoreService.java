package com.davis.service;

import com.davis.model.ConfidenceScore;
import com.davis.repository.ConfidenceScoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfidenceScoreService {

    private final ConfidenceScoreRepository confidenceScoreRepository;

    public ConfidenceScoreService(
            ConfidenceScoreRepository confidenceScoreRepository) {

        this.confidenceScoreRepository = confidenceScoreRepository;
    }

    public List<ConfidenceScore> getAllConfidenceScores() {
        return confidenceScoreRepository.findAll();
    }

    public Optional<ConfidenceScore> getConfidenceScoreById(
            Long confidenceId) {

        return confidenceScoreRepository.findById(confidenceId);
    }

    public Optional<ConfidenceScore> getConfidenceScoreByCaseId(
            Long caseId) {

        return confidenceScoreRepository
                .findConfidenceByCaseId(caseId);
    }

    public ConfidenceScore saveConfidenceScore(
            ConfidenceScore confidenceScore) {

        return confidenceScoreRepository.save(confidenceScore);
    }

    public void deleteConfidenceScore(Long confidenceId) {
        confidenceScoreRepository.deleteById(confidenceId);
    }
}