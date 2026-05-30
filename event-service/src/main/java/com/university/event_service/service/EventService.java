package com.university.event_service.service;

import com.university.event_service.dto.EventDTO;
import com.university.event_service.dto.EventResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventResponseDTO createEvent(EventDTO dto);

    List<EventResponseDTO> getAllEvents();

    EventResponseDTO getEventById(Long id);

    EventResponseDTO updateEvent(Long id, EventDTO dto);

    void deleteEvent(Long id);

    EventResponseDTO rescheduleEvent(Long id, LocalDateTime start, LocalDateTime end);

    List<EventResponseDTO> getUpcomingEvents();

    List<EventResponseDTO> getEventsByOrganizer(Long organizerId);
}
