package com.example.demo.modules.disease.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "disease_diagnoses")
public class DiseaseDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    private String cropName;

    @Column(columnDefinition = "TEXT")
    private String symptomDescription;

    private String imageUrl;
    private String diagnosedDisease;
    private String pathogenCategory; // FUNGAL, BACTERIAL, VIRAL, PEST_INDUCED, NUTRIENT_DEFICIENCY
    private Double confidenceScore;
    private String severity; // LOW, MODERATE, CRITICAL

    @Column(columnDefinition = "TEXT")
    private String chemicalTreatment;

    @Column(columnDefinition = "TEXT")
    private String organicRemedy;

    private Boolean expertReviewRequired;
    private Instant diagnosedAt = Instant.now();

    public DiseaseDiagnosis() {}

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

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getSymptomDescription() {
        return symptomDescription;
    }

    public void setSymptomDescription(String symptomDescription) {
        this.symptomDescription = symptomDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDiagnosedDisease() {
        return diagnosedDisease;
    }

    public void setDiagnosedDisease(String diagnosedDisease) {
        this.diagnosedDisease = diagnosedDisease;
    }

    public String getPathogenCategory() {
        return pathogenCategory;
    }

    public void setPathogenCategory(String pathogenCategory) {
        this.pathogenCategory = pathogenCategory;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getChemicalTreatment() {
        return chemicalTreatment;
    }

    public void setChemicalTreatment(String chemicalTreatment) {
        this.chemicalTreatment = chemicalTreatment;
    }

    public String getOrganicRemedy() {
        return organicRemedy;
    }

    public void setOrganicRemedy(String organicRemedy) {
        this.organicRemedy = organicRemedy;
    }

    public Boolean getExpertReviewRequired() {
        return expertReviewRequired;
    }

    public void setExpertReviewRequired(Boolean expertReviewRequired) {
        this.expertReviewRequired = expertReviewRequired;
    }

    public Instant getDiagnosedAt() {
        return diagnosedAt;
    }

    public void setDiagnosedAt(Instant diagnosedAt) {
        this.diagnosedAt = diagnosedAt;
    }
}
