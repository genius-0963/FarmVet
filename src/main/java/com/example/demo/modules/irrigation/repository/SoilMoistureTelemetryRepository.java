package com.example.demo.modules.irrigation.repository;

import com.example.demo.modules.irrigation.entity.SoilMoistureTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoilMoistureTelemetryRepository extends JpaRepository<SoilMoistureTelemetry, Long> {
    List<SoilMoistureTelemetry> findTop10ByPlotIdOrderByRecordedAtDesc(Long plotId);
}
