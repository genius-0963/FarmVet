package com.example.demo.modules.outbreak.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "outbreak_incidents")
public class OutbreakIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String diseaseName;

    @Column(nullable = false)
    private String affectedHost; // Crop or Animal species

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String district;
    private String state;
    private Double radiusKm = 25.0;
    private String severity = "HIGH"; // MODERATE, HIGH, EPIDEMIC
    private Integer reportCount = 1;
    private Boolean isVerified = true;
    private LocalDate reportedDate = LocalDate.now();

    public OutbreakIncident() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getAffectedHost() {
        return affectedHost;
    }

    public void setAffectedHost(String affectedHost) {
        this.affectedHost = affectedHost;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDate reportedDate) {
        this.reportedDate = reportedDate;
    }
}
