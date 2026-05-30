# Activity Diagram - Create Event

```mermaid
flowchart TD
    A([Start]) --> B[Organizer navigates to create event page]
    B --> C[Organizer fills event details form\nname, description, location, dates, capacity]
    C --> D[Organizer submits the form]
    D --> E{System validates input}
    E -->|Valid| F[System saves event to database]
    F --> G[System sets event status to UPCOMING]
    G --> H[System sends notification to organizer]
    H --> I[System displays success message]
    I --> J([End])
    E -->|Invalid| K[System displays validation errors]
    K --> L[Organizer corrects input]
    L --> D
```

## Workflow Description

1. **Initiation**: The organizer navigates to the event creation page through the UI.
2. **Data Entry**: The organizer fills in event details including name, description, location, start/end dates, and capacity.
3. **Submission**: The organizer submits the form to the system.
4. **Validation**: The system validates all input fields (required fields not empty, dates are logical, capacity is positive).
5. **Success Path**: If valid, the event is persisted with status `UPCOMING` and the organizer is notified.
6. **Error Path**: If invalid, validation errors are shown and the organizer must correct them.
