package com.university.registration_service.controller;

import com.university.registration_service.dto.RegistrationDTO;
import com.university.registration_service.dto.RegistrationResponseDTO;
import com.university.registration_service.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/registrations")
    public ResponseEntity<RegistrationResponseDTO> createRegistration(@RequestBody RegistrationDTO dto) {
        RegistrationResponseDTO response = registrationService.createRegistration(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/registrations/{id}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long id) {
        registrationService.cancelRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/registrations/{id}")
    public ResponseEntity<RegistrationResponseDTO> getRegistrationById(@PathVariable Long id) {
        RegistrationResponseDTO response = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/events/{eventId}/registrations")
    public ResponseEntity<List<RegistrationResponseDTO>> getRegistrationsByEvent(@PathVariable Long eventId) {
        List<RegistrationResponseDTO> response = registrationService.getRegistrationsByEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/participants/{participantId}/registrations")
    public ResponseEntity<List<RegistrationResponseDTO>> getRegistrationsByParticipant(@PathVariable Long participantId) {
        List<RegistrationResponseDTO> response = registrationService.getRegistrationsByParticipant(participantId);
        return ResponseEntity.ok(response);
    }
}
