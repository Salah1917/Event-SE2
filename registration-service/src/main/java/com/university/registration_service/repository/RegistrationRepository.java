package com.university.registration_service.repository;

import com.university.registration_service.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByParticipantId(Long participantId);

    long countByEventIdAndStatus(Long eventId, Registration.RegistrationStatus status);

    boolean existsByEventIdAndParticipantId(Long eventId, Long participantId);

    List<Registration> findByEventIdAndStatus(Long eventId, Registration.RegistrationStatus status);
}
