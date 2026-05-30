package com.university.event_service.service;

import com.university.event_service.dto.EventDTO;
import com.university.event_service.dto.EventResponseDTO;
import com.university.event_service.exception.ResourceNotFoundException;
import com.university.event_service.model.Event;
import com.university.event_service.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public EventResponseDTO createEvent(EventDTO dto) {
        Event event = new Event();
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setCapacity(dto.getCapacity());
        event.setStatus(Event.EventStatus.UPCOMING);
        event.setOrganizerId(dto.getOrganizerId());
        Event saved = eventRepository.save(event);
        return EventResponseDTO.fromEntity(saved);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id, id));
        return EventResponseDTO.fromEntity(event);
    }

    @Override
    public EventResponseDTO updateEvent(Long id, EventDTO dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id, id));
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setCapacity(dto.getCapacity());
        if (dto.getOrganizerId() != null) {
            event.setOrganizerId(dto.getOrganizerId());
        }
        Event saved = eventRepository.save(event);
        return EventResponseDTO.fromEntity(saved);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id, id));
        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Override
    public EventResponseDTO rescheduleEvent(Long id, LocalDateTime start, LocalDateTime end) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id, id));
        event.setStartDate(start);
        event.setEndDate(end);
        Event saved = eventRepository.save(event);
        return EventResponseDTO.fromEntity(saved);
    }

    @Override
    public List<EventResponseDTO> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now()).stream()
                .map(EventResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(EventResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
