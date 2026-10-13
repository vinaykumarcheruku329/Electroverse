package com.electroverse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "lifecycle_assessments", indexes = {
        @Index(name = "idx_lifecycle_product", columnList = "product_id")
})
public class LifecycleAssessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int repairability;

    @Column(nullable = false)
    private int upgradeability;

    @Column(name = "manufacturer_support", nullable = false)
    private int manufacturerSupport;

    @Column(nullable = false)
    private int warranty;

    @Column(name = "energy_efficiency", nullable = false)
    private int energyEfficiency;

    @Column(nullable = false)
    private double overall;

    @Column(length = 1500)
    private String explanation;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "methodology_version", nullable = false, length = 30)
    private String methodologyVersion;

    @Column(name = "demo_data", nullable = false)
    private boolean demoData = false;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getRepairability() {
        return repairability;
    }

    public void setRepairability(int repairability) {
        this.repairability = repairability;
    }

    public int getUpgradeability() {
        return upgradeability;
    }

    public void setUpgradeability(int upgradeability) {
        this.upgradeability = upgradeability;
    }

    public int getManufacturerSupport() {
        return manufacturerSupport;
    }

    public void setManufacturerSupport(int manufacturerSupport) {
        this.manufacturerSupport = manufacturerSupport;
    }

    public int getWarranty() {
        return warranty;
    }

    public void setWarranty(int warranty) {
        this.warranty = warranty;
    }

    public int getEnergyEfficiency() {
        return energyEfficiency;
    }

    public void setEnergyEfficiency(int energyEfficiency) {
        this.energyEfficiency = energyEfficiency;
    }

    public double getOverall() {
        return overall;
    }

    public void setOverall(double overall) {
        this.overall = overall;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getMethodologyVersion() {
        return methodologyVersion;
    }

    public void setMethodologyVersion(String methodologyVersion) {
        this.methodologyVersion = methodologyVersion;
    }

    public boolean isDemoData() {
        return demoData;
    }

    public void setDemoData(boolean demoData) {
        this.demoData = demoData;
    }
}
