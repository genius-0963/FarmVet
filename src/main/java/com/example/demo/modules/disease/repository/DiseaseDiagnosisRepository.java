package com.example.demo.modules.disease.repository;

import com.example.demo.modules.disease.entity.DiseaseDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseaseDiagnosisRepository extends JpaRepository<DiseaseDiagnosis, Long> {
    List<DiseaseDiagnosis> findByFarmerIdOrderByDiagnosedAtDesc(Long farmerId);
}
