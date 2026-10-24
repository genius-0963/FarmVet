package com.example.demo.modules.irrigation.repository;

import com.example.demo.modules.irrigation.entity.IrrigationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrrigationScheduleRepository extends JpaRepository<IrrigationSchedule, Long> {
    List<IrrigationSchedule> findByFarmerIdOrderByScheduledDateAsc(Long farmerId);
}
