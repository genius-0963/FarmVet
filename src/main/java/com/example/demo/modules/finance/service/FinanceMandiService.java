package com.example.demo.modules.finance.service;

import com.example.demo.modules.finance.dto.MandiPriceDto;
import com.example.demo.modules.finance.dto.ProfitAnalysisDto;
import com.example.demo.modules.finance.entity.FarmFinancialTransaction;
import com.example.demo.modules.finance.repository.FarmFinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class FinanceMandiService {

    private final FarmFinancialTransactionRepository transactionRepository;

    public FinanceMandiService(FarmFinancialTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public FarmFinancialTransaction recordTransaction(FarmFinancialTransaction tx) {
        if (tx.getTransactionDate() == null) {
            tx.setTransactionDate(LocalDate.now());
        }
        return transactionRepository.save(tx);
    }

    public List<FarmFinancialTransaction> getFarmerLedger(Long farmerId) {
        return transactionRepository.findByFarmerIdOrderByTransactionDateDesc(farmerId);
    }

    public ProfitAnalysisDto analyzeProfitability(Long farmerId) {
        List<FarmFinancialTransaction> transactions = transactionRepository.findByFarmerIdOrderByTransactionDateDesc(farmerId);

        double totalRevenue = 0.0;
        double totalExpenses = 0.0;
        Map<String, Double> expensesByCategory = new HashMap<>();

        for (FarmFinancialTransaction tx : transactions) {
            if ("REVENUE".equalsIgnoreCase(tx.getTransactionType())) {
                totalRevenue += tx.getAmount();
            } else {
                totalExpenses += tx.getAmount();
                expensesByCategory.merge(tx.getCategory(), tx.getAmount(), Double::sum);
            }
        }

        double netProfit = totalRevenue - totalExpenses;
        double profitMargin = (totalRevenue > 0) ? (netProfit / totalRevenue) * 100.0 : 0.0;
        double roi = (totalExpenses > 0) ? (netProfit / totalExpenses) * 100.0 : 0.0;

        return new ProfitAnalysisDto(totalRevenue, totalExpenses, netProfit, profitMargin, roi, expensesByCategory);
    }

    public List<MandiPriceDto> getMandiPrices(String commodity, String state) {
        List<MandiPriceDto> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        list.add(new MandiPriceDto("Wheat", "Indore APMC", "Indore", "Madhya Pradesh", 2450.0, 2780.0, 2620.0, today));
        list.add(new MandiPriceDto("Soybean", "Nagpur APMC", "Nagpur", "Maharashtra", 4300.0, 4850.0, 4600.0, today));
        list.add(new MandiPriceDto("Cotton", "Rajkot APMC", "Rajkot", "Gujarat", 7100.0, 7800.0, 7450.0, today));
        list.add(new MandiPriceDto("Paddy (Basmati)", "Karnal APMC", "Karnal", "Haryana", 3800.0, 4400.0, 4150.0, today));
        list.add(new MandiPriceDto("Tomato", "Kolar APMC", "Kolar", "Karnataka", 1400.0, 2200.0, 1850.0, today));
        list.add(new MandiPriceDto("Onion", "Lasalgaon APMC", "Nashik", "Maharashtra", 1600.0, 2300.0, 1950.0, today));

        if (commodity != null && !commodity.isBlank()) {
            list.removeIf(p -> !p.getCommodity().equalsIgnoreCase(commodity.trim()));
        }
        if (state != null && !state.isBlank()) {
            list.removeIf(p -> !p.getState().equalsIgnoreCase(state.trim()));
        }

        return list;
    }
}
