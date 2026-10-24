package com.example.demo.modules.finance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "farm_financial_transactions")
public class FarmFinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    private Long plotId;

    @Column(nullable = false)
    private String transactionType; // EXPENSE, REVENUE

    @Column(nullable = false)
    private String category; // SEEDS, FERTILIZERS, PESTICIDES, IRRIGATION, LABOR, MACHINERY, CROP_SALE, BYPRODUCT_SALE

    @Column(nullable = false)
    private Double amount;

    private LocalDate transactionDate;
    private String paymentMode; // CASH, UPI, BANK_TRANSFER

    @Column(columnDefinition = "TEXT")
    private String notes;

    public FarmFinancialTransaction() {}

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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
