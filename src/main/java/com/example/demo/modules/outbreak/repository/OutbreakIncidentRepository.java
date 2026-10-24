package com.example.demo.modules.outbreak.repository;

import com.example.demo.modules.outbreak.entity.OutbreakIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutbreakIncidentRepository extends JpaRepository<OutbreakIncident, Long> {
    List<OutbreakIncident> findByIsVerifiedTrueOrderByReportedDateDesc();
    List<OutbreakIncident> findByStateIgnoreCase(String state);
}
