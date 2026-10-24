package com.example.demo.modules.farm.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.farm.dto.CropCalendarMilestoneDto;
import com.example.demo.modules.farm.entity.FarmPlot;
import com.example.demo.modules.farm.service.FarmService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/farm")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping("/plots")
    public ResponseEntity<ApiResponse<List<FarmPlot>>> getFarmerPlots(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<FarmPlot> plots = farmService.getPlotsByFarmer(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(plots));
    }

    @PostMapping("/plots")
    public ResponseEntity<ApiResponse<FarmPlot>> createPlot(
            @RequestBody FarmPlot plot,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            plot.setFarmerId(userPrincipal.getId());
        } else if (plot.getFarmerId() == null) {
            plot.setFarmerId(1L);
        }
        FarmPlot saved = farmService.createPlot(plot);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Farm plot registered successfully"));
    }

    @GetMapping("/plots/{id}")
    public ResponseEntity<ApiResponse<FarmPlot>> getPlot(@PathVariable Long id) {
        FarmPlot plot = farmService.getPlotById(id);
        return ResponseEntity.ok(ApiResponse.ok(plot));
    }

    @PatchMapping("/plots/{id}/stage")
    public ResponseEntity<ApiResponse<FarmPlot>> updateStage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String stage = body.get("growthStage");
        FarmPlot updated = farmService.updateGrowthStage(id, stage);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Crop stage updated successfully"));
    }

    @GetMapping("/plots/{id}/calendar")
    public ResponseEntity<ApiResponse<List<CropCalendarMilestoneDto>>> getCropCalendar(@PathVariable Long id) {
        List<CropCalendarMilestoneDto> calendar = farmService.generateCropCalendar(id);
        return ResponseEntity.ok(ApiResponse.ok(calendar));
    }
}
