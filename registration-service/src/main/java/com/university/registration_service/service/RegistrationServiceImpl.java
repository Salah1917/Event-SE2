package com.university.registration_service.service;

import com.university.registration_service.dto.RegistrationDTO;
import com.university.registration_service.dto.RegistrationResponseDTO;
import com.university.registration_service.exception.ResourceNotFoundException;
import com.university.registration_service.model.Participant;
import com.university.registration_service.model.Registration;
import com.university.registration_service.repository.ParticipantRepository;
import com.university.registration_service.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ParticipantRepository participantRepository;
    private final String eventServiceUrl;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   ParticipantRepository participantRepository,
                                   @org.springframework.beans.factory.annotation.Value("${event.service.url:http://localhost:8081}") String eventServiceUrl) {
        this.registrationRepository = registrationRepository;
        this.participantRepository = participantRepository;
        this.eventServiceUrl = eventServiceUrl;
    }

    @Override
    public RegistrationResponseDTO createRegistration(RegistrationDTO dto) {
        Participant participant = participantRepository.findByEmail(dto.getParticipantEmail())
                .orElseGet(() -> {
                    Participant newParticipant = new Participant(
                            dto.getParticipantName(),
                            dto.getParticipantEmail(),
                            null
                    );
                    return participantRepository.save(newParticipant);
                });

        Map<String, Object> eventResponse;
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            eventResponse = restTemplate.getForObject(
                    eventServiceUrl + "/api/events/{eventId}", Map.class, dto.getEventId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch event details from event-service", e);
        }

        Integer capacity = (Integer) eventResponse.get("capacity");
        long currentRegistrations = registrationRepository.countByEventIdAndStatus(
                dto.getEventId(), Registration.RegistrationStatus.CONFIRMED);

        Registration.RegistrationStatus status;
        if (currentRegistrations < capacity) {
            status = Registration.RegistrationStatus.CONFIRMED;
        } else {
            status = Registration.RegistrationStatus.WAITLISTED;
        }

        Registration registration = new Registration(
                dto.getEventId(),
                participant,
                LocalDateTime.now(),
                status
        );

        registration = registrationRepository.save(registration);
        return RegistrationResponseDTO.fromEntity(registration);
    }

    @Override
    public void cancelRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId)
                .stream()
                .map(RegistrationResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDTO> getRegistrationsByParticipant(Long participantId) {
        return registrationRepository.findByParticipantId(participantId)
                .stream()
                .map(RegistrationResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationById(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
        return RegistrationResponseDTO.fromEntity(registration);
    }
}
