package com.example.demo.modules.consultation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_bookings")
public class ConsultationBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmerId;

    private Long expertId;
    private String expertName;
    private String expertType; // VETERINARIAN, AGRONOMIST, SOIL_SCIENTIST

    @Column(columnDefinition = "TEXT")
    private String issueSummary;

    private LocalDateTime scheduledSlot;
    private String status; // REQUESTED, CONFIRMED, COMPLETED, CANCELLED
    private String meetingRoomId;

    @Column(columnDefinition = "TEXT")
    private String clinicalNotes;

    private String reportAttachmentUrl;
    private Instant createdAt = Instant.now();

    public ConsultationBooking() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public String getExpertType() {
        return expertType;
    }

    public void setExpertType(String expertType) {
        this.expertType = expertType;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public LocalDateTime getScheduledSlot() {
        return scheduledSlot;
    }

    public void setScheduledSlot(LocalDateTime scheduledSlot) {
        this.scheduledSlot = scheduledSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMeetingRoomId() {
        return meetingRoomId;
    }

    public void setMeetingRoomId(String meetingRoomId) {
        this.meetingRoomId = meetingRoomId;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getReportAttachmentUrl() {
        return reportAttachmentUrl;
    }

    public void setReportAttachmentUrl(String reportAttachmentUrl) {
        this.reportAttachmentUrl = reportAttachmentUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
