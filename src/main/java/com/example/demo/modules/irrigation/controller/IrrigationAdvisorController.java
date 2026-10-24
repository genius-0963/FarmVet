package com.example.demo.modules.irrigation.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.irrigation.entity.IrrigationSchedule;
import com.example.demo.modules.irrigation.entity.SoilMoistureTelemetry;
import com.example.demo.modules.irrigation.service.IrrigationAdvisorService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/irrigation")
public class IrrigationAdvisorController {

    private final IrrigationAdvisorService advisorService;

    public IrrigationAdvisorController(IrrigationAdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<IrrigationSchedule>> calculateIrrigation(
            @RequestParam("plotId") Long plotId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        IrrigationSchedule schedule = advisorService.calculateIrrigationRecommendation(farmerId, plotId);
        return ResponseEntity.ok(ApiResponse.ok(schedule, "Irrigation recommendation generated"));
    }

    @PostMapping("/telemetry")
    public ResponseEntity<ApiResponse<SoilMoistureTelemetry>> recordTelemetry(
            @RequestBody Map<String, Object> payload) {

        Long plotId = Long.valueOf(payload.get("plotId").toString());
        Double moisture = Double.valueOf(payload.get("moisturePercentage").toString());
        Double temp = payload.containsKey("soilTemperature") ? Double.valueOf(payload.get("soilTemperature").toString()) : 25.0;
        String sensorId = payload.getOrDefault("sensorId", "IOT_SN_DEFAULT").toString();

        SoilMoistureTelemetry recorded = advisorService.recordSensorTelemetry(plotId, moisture, temp, sensorId);
        return ResponseEntity.ok(ApiResponse.ok(recorded, "Sensor telemetry received"));
    }

    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<List<IrrigationSchedule>>> getSchedules(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<IrrigationSchedule> list = advisorService.getFarmerSchedules(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
