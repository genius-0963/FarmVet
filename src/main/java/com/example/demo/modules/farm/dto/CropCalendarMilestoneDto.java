package com.example.demo.modules.farm.dto;

import java.time.LocalDate;

public class CropCalendarMilestoneDto {
    private String milestoneName;
    private LocalDate targetDate;
    private String description;
    private String recommendation;

    public CropCalendarMilestoneDto() {}

    public CropCalendarMilestoneDto(String milestoneName, LocalDate targetDate, String description, String recommendation) {
        this.milestoneName = milestoneName;
        this.targetDate = targetDate;
        this.description = description;
        this.recommendation = recommendation;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
