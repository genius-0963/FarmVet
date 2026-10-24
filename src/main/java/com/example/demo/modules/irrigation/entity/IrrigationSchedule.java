package com.example.demo.modules.irrigation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "irrigation_schedules")
public class IrrigationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    private Long plotId;
    private LocalDate scheduledDate;
    private Double waterVolumeLiters;
    private Double currentSoilMoisturePercent;
    private Double forecastedRainfallMm;
    private Double temperatureCelsius;

    @Column(columnDefinition = "TEXT")
    private String recommendationReason;

    private String status; // SCHEDULED, COMPLETED, SKIPPED_DUE_TO_RAIN

    public IrrigationSchedule() {}

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

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Double getWaterVolumeLiters() {
        return waterVolumeLiters;
    }

    public void setWaterVolumeLiters(Double waterVolumeLiters) {
        this.waterVolumeLiters = waterVolumeLiters;
    }

    public Double getCurrentSoilMoisturePercent() {
        return currentSoilMoisturePercent;
    }

    public void setCurrentSoilMoisturePercent(Double currentSoilMoisturePercent) {
        this.currentSoilMoisturePercent = currentSoilMoisturePercent;
    }

    public Double getForecastedRainfallMm() {
        return forecastedRainfallMm;
    }

    public void setForecastedRainfallMm(Double forecastedRainfallMm) {
        this.forecastedRainfallMm = forecastedRainfallMm;
    }

    public Double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(Double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
