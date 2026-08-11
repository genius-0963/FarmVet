package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pesticides")
public class Pesticides {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long farmerId;
    private String cropName;
    private String acres;
    private String diseaseName;
    private String pesticideName;
    private String pesticideStatus;
    private String pesticideQuantity;

    public Pesticides() {
    }

    public Pesticides(Long id, Long farmerId, String cropName, String acres, String diseaseName, String pesticideName, String pesticideStatus, String pesticideQuantity) {
        this.id = id;
        this.farmerId = farmerId;
        this.cropName = cropName;
        this.acres = acres;
        this.diseaseName = diseaseName;
        this.pesticideName = pesticideName;
        this.pesticideStatus = pesticideStatus;
        this.pesticideQuantity = pesticideQuantity;
    }

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

    public String getAcres() {
        return acres;
    }

    public void setAcres(String acres) {
        this.acres = acres;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getPesticideName() {
        return pesticideName;
    }

    public void setPesticideName(String pesticideName) {
        this.pesticideName = pesticideName;
    }

    public String getPesticideStatus() {
        return pesticideStatus;
    }

    public void setPesticideStatus(String pesticideStatus) {
        this.pesticideStatus = pesticideStatus;
    }

    public String getPesticideQuantity() {
        return pesticideQuantity;
    }

    public void setPesticideQuantity(String pesticideQuantity) {
        this.pesticideQuantity = pesticideQuantity;
    }
}
