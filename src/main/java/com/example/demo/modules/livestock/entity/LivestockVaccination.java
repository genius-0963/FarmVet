package com.example.demo.modules.livestock.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "livestock_vaccinations")
public class LivestockVaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long livestockId;

    @Column(nullable = false)
    private String vaccineName;

    private LocalDate administeredDate;
    private LocalDate nextDueDate;
    private String administeredBy;
    private String batchNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public LivestockVaccination() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLivestockId() {
        return livestockId;
    }

    public void setLivestockId(Long livestockId) {
        this.livestockId = livestockId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public LocalDate getAdministeredDate() {
        return administeredDate;
    }

    public void setAdministeredDate(LocalDate administeredDate) {
        this.administeredDate = administeredDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getAdministeredBy() {
        return administeredBy;
    }

    public void setAdministeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
