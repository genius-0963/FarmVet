package com.example.demo.modules.irrigation.service;

import com.example.demo.modules.farm.entity.FarmPlot;
import com.example.demo.modules.farm.service.FarmService;
import com.example.demo.modules.irrigation.entity.IrrigationSchedule;
import com.example.demo.modules.irrigation.entity.SoilMoistureTelemetry;
import com.example.demo.modules.irrigation.repository.IrrigationScheduleRepository;
import com.example.demo.modules.irrigation.repository.SoilMoistureTelemetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class IrrigationAdvisorService {

    private final IrrigationScheduleRepository scheduleRepository;
    private final SoilMoistureTelemetryRepository telemetryRepository;
    private final FarmService farmService;

    public IrrigationAdvisorService(IrrigationScheduleRepository scheduleRepository,
                                  SoilMoistureTelemetryRepository telemetryRepository,
                                  FarmService farmService) {
        this.scheduleRepository = scheduleRepository;
        this.telemetryRepository = telemetryRepository;
        this.farmService = farmService;
    }

    public IrrigationSchedule calculateIrrigationRecommendation(Long farmerId, Long plotId) {
        FarmPlot plot = farmService.getPlotById(plotId);
        double acres = (plot.getAcres() != null && plot.getAcres() > 0) ? plot.getAcres() : 1.0;

        // Fetch latest telemetry if available, else default to 45% moisture
        List<SoilMoistureTelemetry> telemetryList = telemetryRepository.findTop10ByPlotIdOrderByRecordedAtDesc(plotId);
        double currentMoisture = telemetryList.isEmpty() ? 45.0 : telemetryList.get(0).getMoisturePercentage();

        // Simulated weather climate model (e.g., OpenWeather integration)
        double forecastedRainMm = 2.5; // low rain
        double ambientTempCelsius = 31.0;

        IrrigationSchedule schedule = new IrrigationSchedule();
        schedule.setFarmerId(farmerId);
        schedule.setPlotId(plotId);
        schedule.setScheduledDate(LocalDate.now());
        schedule.setCurrentSoilMoisturePercent(currentMoisture);
        schedule.setForecastedRainfallMm(forecastedRainMm);
        schedule.setTemperatureCelsius(ambientTempCelsius);

        if (forecastedRainMm > 15.0) {
            schedule.setWaterVolumeLiters(0.0);
            schedule.setStatus("SKIPPED_DUE_TO_RAIN");
            schedule.setRecommendationReason(String.format("Forecast predicts %.1f mm precipitation. Irrigation postponed to prevent root rot and nitrogen leaching.", forecastedRainMm));
        } else if (currentMoisture < 35.0) {
            // High moisture deficit: 25,000 liters / acre
            double volume = acres * 25000.0;
            schedule.setWaterVolumeLiters(volume);
            schedule.setStatus("SCHEDULED");
            schedule.setRecommendationReason(String.format("Critical soil moisture deficit detected (%.1f%%). Recommend deep watering via %s irrigation.",
                    currentMoisture, (plot.getIrrigationType() != null ? plot.getIrrigationType() : "DRIP")));
        } else if (currentMoisture < 55.0) {
            // Normal maintenance irrigation: 12,000 liters / acre
            double volume = acres * 12000.0;
            schedule.setWaterVolumeLiters(volume);
            schedule.setStatus("SCHEDULED");
            schedule.setRecommendationReason(String.format("Soil moisture moderate (%.1f%%). Maintain vegetative growth with maintenance watering.", currentMoisture));
        } else {
            schedule.setWaterVolumeLiters(0.0);
            schedule.setStatus("SCHEDULED");
            schedule.setRecommendationReason(String.format("Optimal soil moisture level (%.1f%%). No additional irrigation required today.", currentMoisture));
        }

        return scheduleRepository.save(schedule);
    }

    public SoilMoistureTelemetry recordSensorTelemetry(Long plotId, Double moisture, Double temp, String sensorId) {
        SoilMoistureTelemetry telemetry = new SoilMoistureTelemetry();
        telemetry.setPlotId(plotId);
        telemetry.setMoisturePercentage(moisture);
        telemetry.setSoilTemperatureCelsius(temp);
        telemetry.setSensorId(sensorId);
        return telemetryRepository.save(telemetry);
    }

    public List<IrrigationSchedule> getFarmerSchedules(Long farmerId) {
        return scheduleRepository.findByFarmerIdOrderByScheduledDateAsc(farmerId);
    }
}
