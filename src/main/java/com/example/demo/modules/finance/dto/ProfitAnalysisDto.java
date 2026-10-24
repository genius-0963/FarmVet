package com.example.demo.modules.finance.dto;

import java.util.Map;

public class ProfitAnalysisDto {
    private Double totalRevenue;
    private Double totalExpenses;
    private Double netProfit;
    private Double profitMarginPercent;
    private Double returnOnInvestmentPercent;
    private Map<String, Double> expensesByCategory;

    public ProfitAnalysisDto() {}

    public ProfitAnalysisDto(Double totalRevenue, Double totalExpenses, Double netProfit, Double profitMarginPercent, Double returnOnInvestmentPercent, Map<String, Double> expensesByCategory) {
        this.totalRevenue = totalRevenue;
        this.totalExpenses = totalExpenses;
        this.netProfit = netProfit;
        this.profitMarginPercent = profitMarginPercent;
        this.returnOnInvestmentPercent = returnOnInvestmentPercent;
        this.expensesByCategory = expensesByCategory;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(Double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public Double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(Double netProfit) {
        this.netProfit = netProfit;
    }

    public Double getProfitMarginPercent() {
        return profitMarginPercent;
    }

    public void setProfitMarginPercent(Double profitMarginPercent) {
        this.profitMarginPercent = profitMarginPercent;
    }

    public Double getReturnOnInvestmentPercent() {
        return returnOnInvestmentPercent;
    }

    public void setReturnOnInvestmentPercent(Double returnOnInvestmentPercent) {
        this.returnOnInvestmentPercent = returnOnInvestmentPercent;
    }

    public Map<String, Double> getExpensesByCategory() {
        return expensesByCategory;
    }

    public void setExpensesByCategory(Map<String, Double> expensesByCategory) {
        this.expensesByCategory = expensesByCategory;
    }
}
