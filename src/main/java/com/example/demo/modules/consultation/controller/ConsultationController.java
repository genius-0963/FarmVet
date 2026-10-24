package com.example.demo.modules.consultation.controller;

import com.example.demo.common.dto.ApiResponse;
import com.example.demo.modules.consultation.entity.ConsultationBooking;
import com.example.demo.modules.consultation.service.ConsultationService;
import com.example.demo.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<ConsultationBooking>> book(
            @RequestBody ConsultationBooking booking,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal != null) {
            booking.setFarmerId(userPrincipal.getId());
        } else if (booking.getFarmerId() == null) {
            booking.setFarmerId(1L);
        }

        ConsultationBooking saved = service.bookConsultation(booking);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Consultation session booked"));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ConsultationBooking>> confirm(@PathVariable Long id) {
        ConsultationBooking confirmed = service.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.ok(confirmed, "Consultation confirmed"));
    }

    @PostMapping(value = "/{id}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ConsultationBooking>> complete(
            @PathVariable Long id,
            @RequestParam("clinicalNotes") String clinicalNotes,
            @RequestParam(value = "reportPdf", required = false) MultipartFile reportPdf) throws IOException {

        ConsultationBooking completed = service.completeConsultation(id, clinicalNotes, reportPdf);
        return ResponseEntity.ok(ApiResponse.ok(completed, "Consultation finalized with clinical notes"));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<ConsultationBooking>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long farmerId = (userPrincipal != null) ? userPrincipal.getId() : 1L;
        List<ConsultationBooking> bookings = service.getFarmerConsultations(farmerId);
        return ResponseEntity.ok(ApiResponse.ok(bookings));
    }
}
