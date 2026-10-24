package com.example.demo.modules.pesticide.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.pesticide.dto.DosageCalculationDto;
import com.example.demo.modules.pesticide.entity.PesticideApplication;
import com.example.demo.modules.pesticide.service.PesticideDosageService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pesticides")
public class PesticideV2Controller {

    private final PesticideDosageService dosageService;

    public PesticideV2Controller(PesticideDosageService dosageService) {
        this.dosageService = dosageService;
    }

    @GetMapping("/calculate-dosage")
    public ResponseEntity<ApiResponse<DosageCalculationDto>> calculate(
            @RequestParam("pesticideName") String pesticideName,
            @RequestParam("acres") Double acres) {
        DosageCalculationDto result = dosageService.calculateDosage(pesticideName, acres);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<PesticideApplication>> logApplication(
            @RequestBody PesticideApplication application,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal != null) {
            application.setFarmerId(userPrincipal.getId());
        } else if (application.getFarmerId() == null) {
            application.setFarmerId(1L);
        }

        PesticideApplication saved = dosageService.logApplication(application);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Pesticide application logged with safety intervals"));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<PesticideApplication>>> getApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<PesticideApplication> history = dosageService.getFarmerHistory(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
