package com.example.demo.modules.outbreak.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.outbreak.entity.OutbreakIncident;
import com.example.demo.modules.outbreak.service.OutbreakRadarService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/radar")
public class OutbreakRadarController {

    private final OutbreakRadarService radarService;

    public OutbreakRadarController(OutbreakRadarService radarService) {
        this.radarService = radarService;
    }

    @GetMapping("/outbreaks")
    public ResponseEntity<ApiResponse<List<OutbreakIncident>>> getAllOutbreaks() {
        List<OutbreakIncident> list = radarService.getAllActiveOutbreaks();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<OutbreakIncident>>> getNearby(
            @RequestParam("latitude") double lat,
            @RequestParam("longitude") double lon,
            @RequestParam(value = "radiusKm", defaultValue = "50.0") double radiusKm) {

        List<OutbreakIncident> nearby = radarService.findNearbyOutbreaks(lat, lon, radiusKm);
        return ResponseEntity.ok(ApiResponse.ok(nearby));
    }

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<OutbreakIncident>> reportOutbreak(
            @RequestBody OutbreakIncident incident,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        OutbreakIncident saved = radarService.reportCommunityOutbreak(incident, farmerId);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Community outbreak incident reported and broadcasted to local radar"));
    }
}
