package com.example.demo.modules.consultation.service;

import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.common.storage.FileStorageService;
import com.example.demo.modules.consultation.entity.ConsultationBooking;
import com.example.demo.modules.consultation.repository.ConsultationBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConsultationService {

    private final ConsultationBookingRepository repository;
    private final FileStorageService fileStorageService;

    public ConsultationService(ConsultationBookingRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    public ConsultationBooking bookConsultation(ConsultationBooking booking) {
        booking.setStatus("REQUESTED");
        if (booking.getScheduledSlot() == null) {
            booking.setScheduledSlot(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        }
        booking.setMeetingRoomId("fvc-room-" + UUID.randomUUID().toString().substring(0, 8));
        return repository.save(booking);
    }

    public ConsultationBooking confirmBooking(Long bookingId) {
        ConsultationBooking booking = repository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + bookingId));
        booking.setStatus("CONFIRMED");
        return repository.save(booking);
    }

    public ConsultationBooking completeConsultation(Long bookingId, String notes, MultipartFile reportPdf) throws IOException {
        ConsultationBooking booking = repository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + bookingId));

        booking.setStatus("COMPLETED");
        booking.setClinicalNotes(notes);

        if (reportPdf != null && !reportPdf.isEmpty()) {
            String path = fileStorageService.storeFile(reportPdf, "consultation-reports");
            booking.setReportAttachmentUrl(path);
        }

        return repository.save(booking);
    }

    public List<ConsultationBooking> getFarmerConsultations(Long farmerId) {
        return repository.findByFarmerIdOrderByScheduledSlotDesc(farmerId);
    }

    public List<ConsultationBooking> getExpertConsultations(Long expertId) {
        return repository.findByExpertIdOrderByScheduledSlotDesc(expertId);
    }
}
