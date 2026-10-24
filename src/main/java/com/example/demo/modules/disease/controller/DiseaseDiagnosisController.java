package com.example.demo.modules.disease.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.disease.entity.DiseaseDiagnosis;
import com.example.demo.modules.disease.service.DiseaseDiagnosisService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnosis")
public class DiseaseDiagnosisController {

    private final DiseaseDiagnosisService service;

    public DiseaseDiagnosisController(DiseaseDiagnosisService service) {
        this.service = service;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiseaseDiagnosis>> analyzeSymptoms(
            @RequestParam("cropName") String cropName,
            @RequestParam("symptoms") String symptoms,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws IOException {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        DiseaseDiagnosis result = service.diagnoseCrop(farmerId, cropName, symptoms, image);

        return ResponseEntity.ok(ApiResponse.ok(result, "Diagnosis completed successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<DiseaseDiagnosis>>> getHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<DiseaseDiagnosis> history = service.getFarmerDiagnosisHistory(farmerId);

        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
