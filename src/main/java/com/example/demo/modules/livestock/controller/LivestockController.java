package com.example.demo.modules.livestock.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.livestock.entity.LivestockProfile;
import com.example.demo.modules.livestock.entity.LivestockVaccination;
import com.example.demo.modules.livestock.service.LivestockService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/livestock")
public class LivestockController {

    private final LivestockService service;

    public LivestockController(LivestockService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LivestockProfile>> registerAnimal(
            @RequestBody LivestockProfile profile,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal != null) {
            profile.setFarmerId(userPrincipal.getId());
        } else if (profile.getFarmerId() == null) {
            profile.setFarmerId(1L);
        }

        LivestockProfile saved = service.registerAnimal(profile);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Livestock profile registered successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LivestockProfile>>> getMyAnimals(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<LivestockProfile> list = service.getAnimalsByFarmer(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LivestockProfile>> getAnimal(@PathVariable Long id) {
        LivestockProfile profile = service.getAnimalById(id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PostMapping("/{id}/vaccinations")
    public ResponseEntity<ApiResponse<LivestockVaccination>> addVaccination(
            @PathVariable Long id,
            @RequestBody LivestockVaccination record) {

        record.setLivestockId(id);
        LivestockVaccination saved = service.recordVaccination(record);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Vaccination recorded and booster projected"));
    }

    @GetMapping("/{id}/vaccinations")
    public ResponseEntity<ApiResponse<List<LivestockVaccination>>> getVaccinations(@PathVariable Long id) {
        List<LivestockVaccination> list = service.getVaccinations(id);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/diagnose")
    public ResponseEntity<ApiResponse<Map<String, Object>>> diagnose(
            @RequestBody Map<String, String> request) {

        String species = request.getOrDefault("species", "CATTLE");
        String symptoms = request.getOrDefault("symptoms", "");
        Map<String, Object> result = service.diagnoseLivestockSymptoms(species, symptoms);
        return ResponseEntity.ok(ApiResponse.ok(result, "Livestock clinical evaluation completed"));
    }
}
