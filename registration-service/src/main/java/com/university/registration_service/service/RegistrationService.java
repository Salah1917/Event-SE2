package com.university.registration_service.service;

import com.university.registration_service.dto.RegistrationDTO;
import com.university.registration_service.dto.RegistrationResponseDTO;

import java.util.List;

public interface RegistrationService {

    RegistrationResponseDTO createRegistration(RegistrationDTO dto);

    void cancelRegistration(Long id);

    List<RegistrationResponseDTO> getRegistrationsByEvent(Long eventId);

    List<RegistrationResponseDTO> getRegistrationsByParticipant(Long participantId);

    RegistrationResponseDTO getRegistrationById(Long id);
}
