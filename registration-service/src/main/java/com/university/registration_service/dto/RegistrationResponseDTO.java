package com.university.registration_service.dto;

import com.university.registration_service.model.Registration;
import java.time.LocalDateTime;

public class RegistrationResponseDTO {

    private Long id;
    private Long eventId;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private LocalDateTime registrationDate;
    private String status;
    private LocalDateTime createdAt;

    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(Long id, Long eventId, Long participantId, String participantName,
                                   String participantEmail, LocalDateTime registrationDate,
                                   String status, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantEmail = participantEmail;
        this.registrationDate = registrationDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static RegistrationResponseDTO fromEntity(Registration registration) {
        RegistrationResponseDTO dto = new RegistrationResponseDTO();
        dto.setId(registration.getId());
        dto.setEventId(registration.getEventId());
        dto.setParticipantId(registration.getParticipant().getId());
        dto.setParticipantName(registration.getParticipant().getName());
        dto.setParticipantEmail(registration.getParticipant().getEmail());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus().name());
        dto.setCreatedAt(registration.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
