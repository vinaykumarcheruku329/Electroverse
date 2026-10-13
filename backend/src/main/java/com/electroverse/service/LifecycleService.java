package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import com.electroverse.dto.response.FeatureResponses;
import com.electroverse.entity.LifecycleAssessment;
import com.electroverse.entity.Product;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.LifecycleAssessmentRepository;
import com.electroverse.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LifecycleService {

    public static final String METHODOLOGY_VERSION = "1.0";
    private static final double W_REPAIR = 0.25;
    private static final double W_UPGRADE = 0.20;
    private static final double W_SUPPORT = 0.20;
    private static final double W_WARRANTY = 0.15;
    private static final double W_ENERGY = 0.20;

    private final LifecycleAssessmentRepository lifecycleRepository;
    private final ProductRepository productRepository;

    public LifecycleService(LifecycleAssessmentRepository lifecycleRepository,
                            ProductRepository productRepository) {
        this.lifecycleRepository = lifecycleRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Optional<FeatureResponses.Lifecycle> getLatest(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return lifecycleRepository.findTop1ByProductIdOrderByAssessmentDateDesc(productId)
                .map(DtoMapper::toLifecycle);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponses.Lifecycle> history(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return lifecycleRepository.findByProductIdOrderByAssessmentDateDesc(productId).stream()
                .map(DtoMapper::toLifecycle).toList();
    }

    @Transactional(readOnly = true)
    public List<FeatureResponses.Lifecycle> recent() {
        return lifecycleRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(DtoMapper::toLifecycle).toList();
    }

    @Transactional
    public FeatureResponses.Lifecycle create(FeatureRequests.Lifecycle request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
        LifecycleAssessment assessment = new LifecycleAssessment();
        assessment.setProduct(product);
        assessment.setRepairability(request.repairability());
        assessment.setUpgradeability(request.upgradeability());
        assessment.setManufacturerSupport(request.manufacturerSupport());
        assessment.setWarranty(request.warranty());
        assessment.setEnergyEfficiency(request.energyEfficiency());
        assessment.setOverall(computeOverall(request));
        assessment.setExplanation(request.explanation() != null ? request.explanation()
                : defaultExplanation(request));
        assessment.setAssessmentDate(request.assessmentDate() != null ? request.assessmentDate() : LocalDate.now());
        assessment.setMethodologyVersion(request.methodologyVersion() != null ? request.methodologyVersion() : METHODOLOGY_VERSION);
        assessment.setDemoData(request.demoData() == null || request.demoData());
        return DtoMapper.toLifecycle(lifecycleRepository.save(assessment));
    }

    @Transactional
    public void delete(Long id) {
        LifecycleAssessment assessment = lifecycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lifecycle assessment not found: " + id));
        lifecycleRepository.delete(assessment);
    }

    public static double computeOverall(FeatureRequests.Lifecycle a) {
        double value = a.repairability() * W_REPAIR
                + a.upgradeability() * W_UPGRADE
                + a.manufacturerSupport() * W_SUPPORT
                + a.warranty() * W_WARRANTY
                + a.energyEfficiency() * W_ENERGY;
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private String defaultExplanation(FeatureRequests.Lifecycle a) {
        return "Tech Lifecycle Score computed from stored evidence using methodology v" + METHODOLOGY_VERSION
                + ". Weights: repairability 25%, upgradeability 20%, manufacturer support 20%, "
                + "warranty 15%, energy efficiency 20%.";
    }
}
