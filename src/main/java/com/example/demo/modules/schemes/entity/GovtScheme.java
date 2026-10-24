package com.example.demo.modules.schemes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "govt_schemes")
public class GovtScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String schemeName;

    private String ministry;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double minLandAcres;
    private Double maxLandAcres;
    private String targetState; // ALL or specific state
    private String beneficiaryType; // ALL, SMALL_AND_MARGINAL, LIVESTOCK_OWNER, WOMEN_FARMER

    @Column(columnDefinition = "TEXT")
    private String benefitSummary;

    @Column(columnDefinition = "TEXT")
    private String applicationGuidance;

    private String officialPortalUrl;

    public GovtScheme() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getMinistry() {
        return ministry;
    }

    public void setMinistry(String ministry) {
        this.ministry = ministry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMinLandAcres() {
        return minLandAcres;
    }

    public void setMinLandAcres(Double minLandAcres) {
        this.minLandAcres = minLandAcres;
    }

    public Double getMaxLandAcres() {
        return maxLandAcres;
    }

    public void setMaxLandAcres(Double maxLandAcres) {
        this.maxLandAcres = maxLandAcres;
    }

    public String getTargetState() {
        return targetState;
    }

    public void setTargetState(String targetState) {
        this.targetState = targetState;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }

    public String getBenefitSummary() {
        return benefitSummary;
    }

    public void setBenefitSummary(String benefitSummary) {
        this.benefitSummary = benefitSummary;
    }

    public String getApplicationGuidance() {
        return applicationGuidance;
    }

    public void setApplicationGuidance(String applicationGuidance) {
        this.applicationGuidance = applicationGuidance;
    }

    public String getOfficialPortalUrl() {
        return officialPortalUrl;
    }

    public void setOfficialPortalUrl(String officialPortalUrl) {
        this.officialPortalUrl = officialPortalUrl;
    }
}
