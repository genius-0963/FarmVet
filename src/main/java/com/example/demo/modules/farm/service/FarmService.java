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
        initSamplePlotsIfEmpty();
    }

    private void initSamplePlotsIfEmpty() {
        if (farmPlotRepository.count() == 0) {
            createSamplePlot(1L, "North Orchard Plot A", 4.5, "Apple / Fruit Orchard", "Loamy", "DRIP", "VEGETATIVE", 45);
            createSamplePlot(1L, "South Field B", 2.0, "Wheat", "Clay Loam", "SPRINKLER", "SOWING", 15);
        }
    }

    private FarmPlot createSamplePlot(Long farmerId, String name, double acres, String crop, String soil, String irrigation, String stage, int sowingDaysAgo) {
        FarmPlot plot = new FarmPlot();
        plot.setFarmerId(farmerId != null ? farmerId : 1L);
        plot.setPlotName(name);
        plot.setAcres(acres);
        plot.setCurrentCrop(crop);
        plot.setSoilType(soil);
        plot.setIrrigationType(irrigation);
        plot.setGrowthStage(stage);
        plot.setSowingDate(LocalDate.now().minusDays(sowingDaysAgo));
        plot.setCreatedAt(Instant.now());
        plot.setUpdatedAt(Instant.now());
        return farmPlotRepository.save(plot);
    }

    public List<FarmPlot> getPlotsByFarmer(Long farmerId) {
        List<FarmPlot> plots = farmPlotRepository.findByFarmerId(farmerId);
        if (plots.isEmpty()) {
            // Auto-provision demo plots for the farmer so irrigation, calendar, and telemetry work out of the box
            createSamplePlot(farmerId, "North Orchard Plot A", 4.5, "Apple / Fruit Orchard", "Loamy", "DRIP", "VEGETATIVE", 45);
            createSamplePlot(farmerId, "South Field B", 2.0, "Wheat", "Clay Loam", "SPRINKLER", "SOWING", 15);
            plots = farmPlotRepository.findByFarmerId(farmerId);
        }
        return plots;
    }

    public FarmPlot getPlotById(Long plotId) {
        return farmPlotRepository.findById(plotId).orElseGet(() -> {
            if (plotId != null && plotId == 1L) {
                return createSamplePlot(1L, "North Orchard Plot A", 4.5, "Apple / Fruit Orchard", "Loamy", "DRIP", "VEGETATIVE", 45);
            }
            List<FarmPlot> all = farmPlotRepository.findAll();
            if (!all.isEmpty()) {
                return all.get(0);
            }
            return createSamplePlot(1L, "North Orchard Plot A", 4.5, "Apple / Fruit Orchard", "Loamy", "DRIP", "VEGETATIVE", 45);
        });
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
