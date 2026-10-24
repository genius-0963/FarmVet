package com.example.demo.modules.finance.dto;

import java.time.LocalDate;

public class MandiPriceDto {
    private String commodity;
    private String marketName;
    private String district;
    private String state;
    private Double minPricePerQuintal;
    private Double maxPricePerQuintal;
    private Double modalPricePerQuintal;
    private LocalDate reportedDate;

    public MandiPriceDto() {}

    public MandiPriceDto(String commodity, String marketName, String district, String state, Double minPricePerQuintal, Double maxPricePerQuintal, Double modalPricePerQuintal, LocalDate reportedDate) {
        this.commodity = commodity;
        this.marketName = marketName;
        this.district = district;
        this.state = state;
        this.minPricePerQuintal = minPricePerQuintal;
        this.maxPricePerQuintal = maxPricePerQuintal;
        this.modalPricePerQuintal = modalPricePerQuintal;
        this.reportedDate = reportedDate;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
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

    public Double getMinPricePerQuintal() {
        return minPricePerQuintal;
    }

    public void setMinPricePerQuintal(Double minPricePerQuintal) {
        this.minPricePerQuintal = minPricePerQuintal;
    }

    public Double getMaxPricePerQuintal() {
        return maxPricePerQuintal;
    }

    public void setMaxPricePerQuintal(Double maxPricePerQuintal) {
        this.maxPricePerQuintal = maxPricePerQuintal;
    }

    public Double getModalPricePerQuintal() {
        return modalPricePerQuintal;
    }

    public void setModalPricePerQuintal(Double modalPricePerQuintal) {
        this.modalPricePerQuintal = modalPricePerQuintal;
    }

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDate reportedDate) {
        this.reportedDate = reportedDate;
    }
}
