package com.davis.service;

import com.davis.model.Case;
import com.davis.model.Indicator;
import com.davis.repository.CaseRepository;
import com.davis.repository.IndicatorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IndicatorService {

    private final IndicatorRepository indicatorRepository;
    private final CaseRepository caseRepository;

    public IndicatorService(
            IndicatorRepository indicatorRepository,
            CaseRepository caseRepository) {

        this.indicatorRepository = indicatorRepository;
        this.caseRepository = caseRepository;
    }

    public List<Indicator> getAllIndicators() {
        return indicatorRepository.findAll();
    }

    public Optional<Indicator> getIndicatorById(Long indicatorId) {
        return indicatorRepository.findById(indicatorId);
    }

    public Indicator saveIndicator(Indicator indicator) {
        return indicatorRepository.save(indicator);
    }

    public void deleteIndicator(Long indicatorId) {
        indicatorRepository.deleteById(indicatorId);
    }

    public Indicator addIndicatorToCase(
            Long caseId,
            String indicatorType,
            String indicatorValue,
            String description) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Case not found: " + caseId));

        Indicator indicator = new Indicator();

        indicator.setCaseEntity(caseEntity);
        indicator.setIndicatorType(indicatorType);
        indicator.setIndicatorValue(indicatorValue);
        indicator.setDescription(description);

        return indicatorRepository.save(indicator);
    }
}