package com.example.demo.modules.pesticide.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pesticide_applications")
public class PesticideApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    private Long plotId;
    private String cropName;
    private String pesticideName;
    private String activeIngredient;
    private Double acres;
    private Double dosagePerAcre;
    private Double totalVolumeCalculated;
    private LocalDate applicationDate;
    private Integer preHarvestIntervalDays; // PHI
    private LocalDate safeHarvestDate;
    private Integer reapplicationIntervalDays;
    private LocalDate nextRecommendedSprayDate;

    @Column(columnDefinition = "TEXT")
    private String safetyWarning;

    private String status; // LOGGED, COMPLETED

    public PesticideApplication() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getPesticideName() {
        return pesticideName;
    }

    public void setPesticideName(String pesticideName) {
        this.pesticideName = pesticideName;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }

    public void setActiveIngredient(String activeIngredient) {
        this.activeIngredient = activeIngredient;
    }

    public Double getAcres() {
        return acres;
    }

    public void setAcres(Double acres) {
        this.acres = acres;
    }

    public Double getDosagePerAcre() {
        return dosagePerAcre;
    }

    public void setDosagePerAcre(Double dosagePerAcre) {
        this.dosagePerAcre = dosagePerAcre;
    }

    public Double getTotalVolumeCalculated() {
        return totalVolumeCalculated;
    }

    public void setTotalVolumeCalculated(Double totalVolumeCalculated) {
        this.totalVolumeCalculated = totalVolumeCalculated;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Integer getPreHarvestIntervalDays() {
        return preHarvestIntervalDays;
    }

    public void setPreHarvestIntervalDays(Integer preHarvestIntervalDays) {
        this.preHarvestIntervalDays = preHarvestIntervalDays;
    }

    public LocalDate getSafeHarvestDate() {
        return safeHarvestDate;
    }

    public void setSafeHarvestDate(LocalDate safeHarvestDate) {
        this.safeHarvestDate = safeHarvestDate;
    }

    public Integer getReapplicationIntervalDays() {
        return reapplicationIntervalDays;
    }

    public void setReapplicationIntervalDays(Integer reapplicationIntervalDays) {
        this.reapplicationIntervalDays = reapplicationIntervalDays;
    }

    public LocalDate getNextRecommendedSprayDate() {
        return nextRecommendedSprayDate;
    }

    public void setNextRecommendedSprayDate(LocalDate nextRecommendedSprayDate) {
        this.nextRecommendedSprayDate = nextRecommendedSprayDate;
    }

    public String getSafetyWarning() {
        return safetyWarning;
    }

    public void setSafetyWarning(String safetyWarning) {
        this.safetyWarning = safetyWarning;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
