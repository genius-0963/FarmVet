package com.example.demo.modules.pesticide.dto;

public class DosageCalculationDto {
    private String pesticideName;
    private String activeIngredient;
    private Double acres;
    private Double dosagePerAcre;
    private String unit; // ml, grams
    private Double totalRequired;
    private Integer phiDays; // Pre-Harvest Interval
    private String safetyInstructions;

    public DosageCalculationDto() {}

    public DosageCalculationDto(String pesticideName, String activeIngredient, Double acres, Double dosagePerAcre, String unit, Double totalRequired, Integer phiDays, String safetyInstructions) {
        this.pesticideName = pesticideName;
        this.activeIngredient = activeIngredient;
        this.acres = acres;
        this.dosagePerAcre = dosagePerAcre;
        this.unit = unit;
        this.totalRequired = totalRequired;
        this.phiDays = phiDays;
        this.safetyInstructions = safetyInstructions;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getTotalRequired() {
        return totalRequired;
    }

    public void setTotalRequired(Double totalRequired) {
        this.totalRequired = totalRequired;
    }

    public Integer getPhiDays() {
        return phiDays;
    }

    public void setPhiDays(Integer phiDays) {
        this.phiDays = phiDays;
    }

    public String getSafetyInstructions() {
        return safetyInstructions;
    }

    public void setSafetyInstructions(String safetyInstructions) {
        this.safetyInstructions = safetyInstructions;
    }
}
