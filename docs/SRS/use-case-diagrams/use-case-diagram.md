# Use Case Diagram - Event Registration System

```mermaid
graph TB
    subgraph "Event Registration System"
        UC1["Create Event"]
        UC2["Update Event"]
        UC3["Cancel Event"]
        UC4["Reschedule Event"]
        UC5["View Events"]
        UC6["View Registrations"]
        UC7["Register for Event"]
        UC8["Cancel Registration"]
        UC9["View My Registrations"]
        UC10["Manage Users"]
        UC11["Manage All Events"]
        UC12["View All Registrations"]
    end

    Organizer["Event Organizer"] --> UC1
    Organizer --> UC2
    Organizer --> UC3
    Organizer --> UC4
    Organizer --> UC5
    Organizer --> UC6

    Participant["Participant"] --> UC5
    Participant --> UC7
    Participant --> UC8
    Participant --> UC9

    Admin["Admin"] --> UC10
    Admin --> UC11
    Admin --> UC12
```

## Actors

| Actor | Description |
|-------|-------------|
| **Event Organizer** | Creates and manages events, views registrations |
| **Participant** | Browses events, registers, manages own registrations |
| **Admin** | Manages users, oversees all events and registrations |

## Use Cases

| ID | Use Case | Actor | Description |
|----|----------|-------|-------------|
| UC1 | Create Event | Organizer | Fill event details form and submit to create a new event |
| UC2 | Update Event | Organizer | Modify existing event details |
| UC3 | Cancel Event | Organizer | Cancel an existing event |
| UC4 | Reschedule Event | Organizer | Change event date/time |
| UC5 | View Events | Organizer, Participant | Browse list of available events |
| UC6 | View Registrations | Organizer | View all registrations for an event |
| UC7 | Register for Event | Participant | Register to attend an event |
| UC8 | Cancel Registration | Participant | Cancel own registration |
| UC9 | View My Registrations | Participant | View personal registration history |
| UC10 | Manage Users | Admin | Create, update, disable user accounts |
| UC11 | Manage All Events | Admin | Oversee and manage any event in the system |
| UC12 | View All Registrations | Admin | View all registrations across all events |
