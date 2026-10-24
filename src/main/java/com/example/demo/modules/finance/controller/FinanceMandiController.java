package com.example.demo.modules.finance.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.finance.dto.MandiPriceDto;
import com.example.demo.modules.finance.dto.ProfitAnalysisDto;
import com.example.demo.modules.finance.entity.FarmFinancialTransaction;
import com.example.demo.modules.finance.service.FinanceMandiService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceMandiController {

    private final FinanceMandiService service;

    public FinanceMandiController(FinanceMandiService service) {
        this.service = service;
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<FarmFinancialTransaction>> recordTransaction(
            @RequestBody FarmFinancialTransaction tx,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal != null) {
            tx.setFarmerId(userPrincipal.getId());
        } else if (tx.getFarmerId() == null) {
            tx.setFarmerId(1L);
        }

        FarmFinancialTransaction saved = service.recordTransaction(tx);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Financial transaction logged"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<FarmFinancialTransaction>>> getTransactions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<FarmFinancialTransaction> ledger = service.getFarmerLedger(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(ledger));
    }

    @GetMapping("/profit-analysis")
    public ResponseEntity<ApiResponse<ProfitAnalysisDto>> getProfitAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        ProfitAnalysisDto analysis = service.analyzeProfitability(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(analysis));
    }

    @GetMapping("/mandi-prices")
    public ResponseEntity<ApiResponse<List<MandiPriceDto>>> getMandiPrices(
            @RequestParam(value = "commodity", required = false) String commodity,
            @RequestParam(value = "state", required = false) String state) {

        List<MandiPriceDto> prices = service.getMandiPrices(commodity, state);
        return ResponseEntity.ok(ApiResponse.ok(prices));
    }
}
