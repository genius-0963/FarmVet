package com.example.demo.modules.ai.dto;

import java.time.Instant;

public class FarmerFieldProfile {

    private String crop;
    private Double acreage;
    private String soilType;
    private String location;
    private String cropStage;
    private Integer daysAfterSowing;
    private Instant lastUpdated = Instant.now();

    public FarmerFieldProfile() {}

    public FarmerFieldProfile(String crop, Double acreage, String soilType, String location, String cropStage) {
        this.crop = crop;
        this.acreage = acreage;
        this.soilType = soilType;
        this.location = location;
        this.cropStage = cropStage;
        this.lastUpdated = Instant.now();
    }

    public boolean hasContext() {
        return crop != null || acreage != null || location != null;
    }

    public String toContextSummary() {
        StringBuilder sb = new StringBuilder();
        if (crop != null) sb.append("Crop: ").append(crop).append(" • ");
        if (acreage != null) sb.append("Area: ").append(acreage).append(" Acres • ");
        if (cropStage != null) sb.append("Stage: ").append(cropStage).append(" • ");
        if (soilType != null) sb.append("Soil: ").append(soilType).append(" • ");
        if (location != null) sb.append("Location: ").append(location);

        String s = sb.toString();
        if (s.endsWith(" • ")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.isBlank() ? "General Agro Context" : s;
    }

    // Getters and Setters
    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
        this.lastUpdated = Instant.now();
    }

    public Double getAcreage() {
        return acreage;
    }

    public void setAcreage(Double acreage) {
        this.acreage = acreage;
        this.lastUpdated = Instant.now();
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
        this.lastUpdated = Instant.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.lastUpdated = Instant.now();
    }

    public String getCropStage() {
        return cropStage;
    }

    public void setCropStage(String cropStage) {
        this.cropStage = cropStage;
        this.lastUpdated = Instant.now();
    }

    public Integer getDaysAfterSowing() {
        return daysAfterSowing;
    }

    public void setDaysAfterSowing(Integer daysAfterSowing) {
        this.daysAfterSowing = daysAfterSowing;
        this.lastUpdated = Instant.now();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
