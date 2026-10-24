package com.example.demo.modules.schemes.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.schemes.entity.GovtScheme;
import com.example.demo.modules.schemes.service.SchemeFinderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schemes")
public class SchemeFinderController {

    private final SchemeFinderService service;

    public SchemeFinderController(SchemeFinderService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GovtScheme>>> getSchemes() {
        List<GovtScheme> schemes = service.getAllSchemes();
        return ResponseEntity.ok(ApiResponse.ok(schemes));
    }

    @GetMapping("/check-eligibility")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> checkEligibility(
            @RequestParam(value = "acres", defaultValue = "2.5") double acres,
            @RequestParam(value = "state", defaultValue = "Maharashtra") String state,
            @RequestParam(value = "beneficiaryType", defaultValue = "ALL") String beneficiaryType) {

        List<Map<String, Object>> evaluations = service.checkEligibility(acres, state, beneficiaryType);
        return ResponseEntity.ok(ApiResponse.ok(evaluations, "Government subsidy eligibility assessed"));
    }
}
