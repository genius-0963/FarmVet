package com.example.demo.modules.consultation.repository;

import com.example.demo.modules.consultation.entity.ConsultationBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationBookingRepository extends JpaRepository<ConsultationBooking, Long> {
    List<ConsultationBooking> findByFarmerIdOrderByScheduledSlotDesc(Long farmerId);
    List<ConsultationBooking> findByExpertIdOrderByScheduledSlotDesc(Long expertId);
}
