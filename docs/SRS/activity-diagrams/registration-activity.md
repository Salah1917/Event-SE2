# Activity Diagram - Register for Event

```mermaid
flowchart TD
    A([Start]) --> B[Participant browses available events]
    B --> C[Participant selects an event]
    C --> D[Participant clicks Register button]
    D --> E[System checks event capacity]
    E --> F[System counts current confirmed registrations]
    F --> G{Capacity available?}
    G -->|Yes| H[System creates registration with status CONFIRMED]
    H --> I[System confirms registration]
    I --> J([End])
    G -->|No| K[System creates registration with status WAITLISTED]
    K --> L[System adds participant to waitlist]
    L --> M[System notifies participant of waitlist position]
    M --> J
```

## Workflow Description

1. **Event Selection**: The participant browses available events and selects one to register for.
2. **Registration Request**: The participant clicks the register button to initiate registration.
3. **Capacity Check**: The system checks the event's maximum capacity against the current number of confirmed registrations.
4. **Confirmation Path**: If capacity is available, the registration is created with status `CONFIRMED`.
5. **Waitlist Path**: If capacity is full, the registration is created with status `WAITLISTED`, and the participant is added to the waitlist with a notification.
