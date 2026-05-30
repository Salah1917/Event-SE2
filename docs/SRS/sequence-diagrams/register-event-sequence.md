# Sequence Diagram - Register for Event

```mermaid
sequenceDiagram
    participant P as Participant
    participant UI as UI (Thymeleaf)
    participant GW as API Gateway (:8080)
    participant RC as Registration Controller
    participant RS as Registration Service
    participant ES as Event Service (REST call)
    participant RR as Registration Repository
    participant RDB as Registration DB

    P->>UI: Select event & click register
    UI->>GW: POST /api/registrations (JWT + RegistrationDTO)
    activate GW
    GW->>GW: Validate JWT
    GW->>RC: POST /api/registrations
    activate RC
    RC->>RS: createRegistration(RegistrationDTO)
    activate RS
    RS->>ES: GET /api/events/{eventId} (fetch capacity)
    activate ES
    ES-->>RS: EventResponseDTO (with capacity)
    deactivate ES
    RS->>RR: countByEventIdAndStatus(eventId, CONFIRMED)
    activate RR
    RR->>RDB: SELECT COUNT(*) FROM registrations...
    activate RDB
    RDB-->>RR: count
    deactivate RDB
    RR-->>RS: current registration count
    deactivate RR
    alt capacity available
        RS->>RS: Set status = CONFIRMED
    else capacity full
        RS->>RS: Set status = WAITLISTED
    end
    RS->>RR: save(Registration)
    activate RR
    RR->>RDB: INSERT INTO registrations...
    activate RDB
    RDB-->>RR: Registration entity
    deactivate RDB
    RR-->>RS: saved Registration
    deactivate RR
    RS-->>RC: RegistrationResponseDTO
    deactivate RS
    RC-->>GW: 201 Created
    deactivate RC
    GW-->>UI: JSON response
    deactivate GW
    UI-->>P: Show confirmation/waitlist message
```

## Step-by-Step Flow

| Step | Action | Description |
|------|--------|-------------|
| 1 | Participant registers | Selects event and clicks register |
| 2 | UI sends request | POST request to API Gateway with JWT |
| 3 | Gateway validates JWT | Token validation before forwarding |
| 4 | Controller receives | RegistrationController handles request |
| 5 | Fetch event capacity | RegistrationService calls EventService via REST |
| 6 | Count registrations | Check current confirmed registration count |
| 7 | Decision | If count < capacity → CONFIRMED, else → WAITLISTED |
| 8 | Persist registration | Save to registration database |
| 9 | Response | Return RegistrationResponseDTO with 201 status |

## Endpoint Details

- **URL**: `POST /api/registrations`
- **Authentication**: JWT required
- **Request Body**: RegistrationDTO (eventId, participantId, participantName, participantEmail)
- **Response**: 201 Created with RegistrationResponseDTO body
