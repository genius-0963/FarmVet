package com.example.demo.modules.farm.service;

import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.modules.farm.dto.CropCalendarMilestoneDto;
import com.example.demo.modules.farm.entity.FarmPlot;
import com.example.demo.modules.farm.repository.FarmPlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FarmService {

    private final FarmPlotRepository farmPlotRepository;

    public FarmService(FarmPlotRepository farmPlotRepository) {
        this.farmPlotRepository = farmPlotRepository;
    }

    public List<FarmPlot> getPlotsByFarmer(Long farmerId) {
        return farmPlotRepository.findByFarmerId(farmerId);
    }

    public FarmPlot getPlotById(Long plotId) {
        return farmPlotRepository.findById(plotId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm plot not found with id: " + plotId));
    }

    public FarmPlot createPlot(FarmPlot plot) {
        plot.setCreatedAt(Instant.now());
        plot.setUpdatedAt(Instant.now());
        return farmPlotRepository.save(plot);
    }

    public FarmPlot updateGrowthStage(Long plotId, String newStage) {
        FarmPlot plot = getPlotById(plotId);
        plot.setGrowthStage(newStage);
        plot.setUpdatedAt(Instant.now());
        return farmPlotRepository.save(plot);
    }

    public List<CropCalendarMilestoneDto> generateCropCalendar(Long plotId) {
        FarmPlot plot = getPlotById(plotId);
        LocalDate sowing = (plot.getSowingDate() != null) ? plot.getSowingDate() : LocalDate.now();

        List<CropCalendarMilestoneDto> calendar = new ArrayList<>();
        calendar.add(new CropCalendarMilestoneDto(
                "Basal Fertilization & Seed Treatment",
                sowing,
                "Pre-sowing soil conditioning with organic manure and phosphorus",
                "Ensure soil moisture level between 50-60% before sowing."
        ));
        calendar.add(new CropCalendarMilestoneDto(
                "First Weeding & Nitrogen Top Dressing",
                sowing.plusDays(21),
                "Remove emerging broadleaf weeds and apply initial nitrogen booster",
                "Inspect lower leaves for early fungal spotting."
        ));
        calendar.add(new CropCalendarMilestoneDto(
                "Peak Vegetative Irrigation Cycle",
                sowing.plusDays(45),
                "Critical vegetative canopy development requiring steady moisture",
                "Check for stem borer and aphid infestation."
        ));
        calendar.add(new CropCalendarMilestoneDto(
                "Flowering & Pest Monitoring",
                sowing.plusDays(70),
                "Pollen formation stage; maintain soil moisture avoid water stress",
                "Avoid spraying harsh chemicals during peak bee pollination hours."
        ));
        calendar.add(new CropCalendarMilestoneDto(
                "Harvesting Window",
                sowing.plusDays(110),
                "Crop physiological maturity attained; grain moisture < 14%",
                "Schedule harvesting during dry clear weather."
        ));

        return calendar;
    }
}
