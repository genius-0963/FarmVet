package com.example.demo.modules.livestock.repository;

import com.example.demo.modules.livestock.entity.LivestockVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivestockVaccinationRepository extends JpaRepository<LivestockVaccination, Long> {
    List<LivestockVaccination> findByLivestockIdOrderByAdministeredDateDesc(Long livestockId);
}
