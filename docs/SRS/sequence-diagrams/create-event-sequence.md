# Sequence Diagram - Create Event

```mermaid
sequenceDiagram
    participant O as Event Organizer
    participant UI as UI (Thymeleaf)
    participant GW as API Gateway (:8080)
    participant C as Event Controller
    participant S as Event Service
    participant R as Event Repository
    participant DB as Event DB (PostgreSQL)

    O->>UI: Fill event form & submit
    UI->>GW: POST /api/events (JWT in Authorization header)
    activate GW
    GW->>GW: Validate JWT token
    GW->>C: POST /api/events
    activate C
    C->>S: createEvent(EventDTO)
    activate S
    S->>R: save(Event)
    activate R
    R->>DB: INSERT INTO events...
    activate DB
    DB-->>R: Event entity
    deactivate DB
    R-->>S: saved Event
    deactivate R
    S-->>C: EventResponseDTO
    deactivate S
    C-->>GW: 201 Created
    deactivate C
    GW-->>UI: JSON response
    deactivate GW
    UI-->>O: Show success page
```

## Step-by-Step Flow

| Step | Action | Description |
|------|--------|-------------|
| 1 | Organizer submits form | Organizer fills event details and clicks submit |
| 2 | UI sends request | UI sends POST request to API Gateway with JWT |
| 3 | Gateway validates JWT | Gateway checks token validity before forwarding |
| 4 | Controller receives request | EventController handles the request |
| 5 | Service processes | EventService.createEvent() maps DTO to entity |
| 6 | Repository persists | EventRepository saves to database |
| 7 | Response returned | EventResponseDTO returned to client with 201 status |

## Endpoint Details

- **URL**: `POST /api/events`
- **Authentication**: JWT required
- **Request Body**: EventDTO (name, description, location, startDate, endDate, capacity, organizerId)
- **Response**: 201 Created with EventResponseDTO body
