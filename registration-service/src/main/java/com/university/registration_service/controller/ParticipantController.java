package com.university.registration_service.controller;

import com.university.registration_service.dto.ParticipantDTO;
import com.university.registration_service.model.Participant;
import com.university.registration_service.repository.ParticipantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipantController {

    private final ParticipantRepository participantRepository;

    public ParticipantController(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @PostMapping("/api/participants")
    public ResponseEntity<Participant> createParticipant(@RequestBody ParticipantDTO dto) {
        Participant participant = new Participant(dto.getName(), dto.getEmail(), dto.getPhone());
        Participant saved = participantRepository.save(participant);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/api/participants/{id}")
    public ResponseEntity<Participant> getParticipantById(@PathVariable Long id) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found with id: " + id));
        return ResponseEntity.ok(participant);
    }
}
