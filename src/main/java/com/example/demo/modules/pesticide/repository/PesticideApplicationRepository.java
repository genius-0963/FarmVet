package com.example.demo.modules.pesticide.repository;

import com.example.demo.modules.pesticide.entity.PesticideApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PesticideApplicationRepository extends JpaRepository<PesticideApplication, Long> {
    List<PesticideApplication> findByFarmerIdOrderByApplicationDateDesc(Long farmerId);
}
