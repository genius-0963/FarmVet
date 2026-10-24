package com.example.demo.modules.irrigation.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "soil_moisture_telemetry")
public class SoilMoistureTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long plotId;

    private Double moisturePercentage;
    private Double soilTemperatureCelsius;
    private String sensorId;
    private Instant recordedAt = Instant.now();

    public SoilMoistureTelemetry() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public Double getMoisturePercentage() {
        return moisturePercentage;
    }

    public void setMoisturePercentage(Double moisturePercentage) {
        this.moisturePercentage = moisturePercentage;
    }

    public Double getSoilTemperatureCelsius() {
        return soilTemperatureCelsius;
    }

    public void setSoilTemperatureCelsius(Double soilTemperatureCelsius) {
        this.soilTemperatureCelsius = soilTemperatureCelsius;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
