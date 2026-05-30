# OCL Specifications - Event Registration System

> This document defines formal Object Constraint Language (OCL) specifications for all entities, service methods, and system invariants in the Event Registration System.

---

## 1. Package Structure

```
University::EventRegistrationSystem
  ├── EventService
  │   ├── Event
  │   ├── EventDTO
  │   ├── EventResponseDTO
  │   ├── EventService (interface)
  │   └── EventServiceImpl
  ├── RegistrationService
  │   ├── Registration
  │   ├── Participant
  │   ├── RegistrationDTO
  │   ├── RegistrationResponseDTO
  │   └── RegistrationService (interface)
  └── UserService
      ├── User
      ├── Role
      ├── AuthService (interface)
      ├── UserService (interface)
      └── JwtUtils
```

---

## 2. Event Service - OCL Constraints

### 2.1 Event Entity Invariants

```
package University::EventRegistrationSystem::EventService

context Event
  -- Event name must not be empty and cannot exceed 200 characters
  inv validName:
    self.name->notEmpty() and self.name.size() <= 200

  -- Event description must not be empty
  inv validDescription:
    self.description->notEmpty()

  -- Event location must not be empty
  inv validLocation:
    self.location->notEmpty()

  -- Capacity must be at least 1
  inv validCapacity:
    self.capacity >= 1

  -- Start date must be before end date
  inv validDateRange:
    self.startDate < self.endDate

  -- Status must be one of the defined enum values
  inv validStatus:
    self.status = EventStatus::UPCOMING or
    self.status = EventStatus::ONGOING or
    self.status = EventStatus::COMPLETED or
    self.status = EventStatus::CANCELLED

  -- Organizer ID must be provided
  inv validOrganizer:
    self.organizerId->notEmpty()

  -- Created at timestamp must be set on creation
  inv createdAtNotNull:
    self.createdAt->notEmpty()

  -- An event with CANCELLED status cannot be modified
  inv cancelledEventsImmutable:
    self.status = EventStatus::CANCELLED implies
      self.updatedAt = self.createdAt

  -- Capacity must be a positive integer (not zero)
  inv capacityPositive:
    self.capacity > 0
endpackage
```

### 2.2 Event - Preconditions and Postconditions

```
package University::EventRegistrationSystem::EventService

context Event::setStatus(s: EventStatus)
  -- Cannot cancel an already cancelled event
  pre: self.status <> EventStatus::CANCELLED

  -- Status transition must be valid
  pre: (self.status = EventStatus::UPCOMING implies
         s = EventStatus::ONGOING or
         s = EventStatus::CANCELLED) and
       (self.status = EventStatus::ONGOING implies
         s = EventStatus::COMPLETED or
         s = EventStatus::CANCELLED) and
       (self.status = EventStatus::COMPLETED implies
         s = EventStatus::CANCELLED)
  post: self.status = s


context Event::setName(n: String)
  pre: n->notEmpty() and n.size() <= 200
  pre: self.status <> EventStatus::CANCELLED
  post: self.name = n


context Event::setDescription(d: String)
  pre: d->notEmpty()
  pre: self.status <> EventStatus::CANCELLED
  post: self.description = d


context Event::setLocation(l: String)
  pre: l->notEmpty()
  pre: self.status <> EventStatus::CANCELLED
  post: self.location = l


context Event::setStartDate(sd: LocalDateTime)
  pre: sd < self.endDate
  pre: self.status <> EventStatus::CANCELLED
  post: self.startDate = sd


context Event::setEndDate(ed: LocalDateTime)
  pre: ed > self.startDate
  pre: self.status <> EventStatus::CANCELLED
  post: self.endDate = ed


context Event::setCapacity(c: Integer)
  pre: c >= 1
  pre: self.status <> EventStatus::CANCELLED
  post: self.capacity = c
endpackage
```

### 2.3 EventService Interface - Preconditions and Postconditions

```
package University::EventRegistrationSystem::EventService

context EventService::createEvent(dto: EventDTO)
  pre: dto.name->notEmpty() and dto.name.size() <= 200
  pre: dto.description->notEmpty()
  pre: dto.location->notEmpty()
  pre: dto.startDate < dto.endDate
  pre: dto.capacity >= 1
  pre: dto.organizerId->notEmpty()
  post: result.id->notEmpty()
  post: result.name = dto.name
  post: result.description = dto.description
  post: result.location = dto.location
  post: result.startDate = dto.startDate
  post: result.endDate = dto.endDate
  post: result.capacity = dto.capacity
  post: result.status = 'UPCOMING'
  post: result.organizerId = dto.organizerId
  post: result.createdAt->notEmpty()
  post: result.updatedAt->notEmpty()


context EventService::getEventById(id: Long)
  pre: id->notEmpty()
  post: result.id = id


context EventService::getAllEvents()
  post: result->notEmpty() implies
    result->forAll(e: EventResponseDTO | e.id->notEmpty())


context EventService::updateEvent(id: Long, dto: EventDTO)
  pre: id->notEmpty()
  pre: dto.name->notEmpty() and dto.name.size() <= 200
  pre: dto.description->notEmpty()
  pre: dto.location->notEmpty()
  pre: dto.startDate < dto.endDate
  pre: dto.capacity >= 1
  post: result.id = id
  post: result.name = dto.name
  post: result.description = dto.description
  post: result.location = dto.location
  post: result.startDate = dto.startDate
  post: result.endDate = dto.endDate
  post: result.capacity = dto.capacity


context EventService::deleteEvent(id: Long)
  pre: id->notEmpty()
  post: let event: Event = Event::allInstances()->any(e: Event | e.id = id) in
    event.status = EventStatus::CANCELLED


context EventService::rescheduleEvent(id: Long, start: LocalDateTime, end: LocalDateTime)
  pre: id->notEmpty()
  pre: start < end
  pre: self.getEventById(id).status <> 'CANCELLED'
  post: result.startDate = start
  post: result.endDate = end


context EventService::getUpcomingEvents()
  post: result->forAll(e: EventResponseDTO |
    e.startDate > Date::now() and e.status = 'UPCOMING')


context EventService::getEventsByOrganizer(organizerId: Long)
  pre: organizerId->notEmpty()
  post: result->forAll(e: EventResponseDTO | e.organizerId = organizerId)
endpackage
```

### 2.4 EventDTO Constraints

```
package University::EventRegistrationSystem::EventService

context EventDTO
  inv: self.name->notEmpty() implies self.name.size() <= 200
  inv: self.capacity->notEmpty() implies self.capacity >= 1
  inv: self.startDate->notEmpty() and self.endDate->notEmpty() implies
         self.startDate < self.endDate
endpackage
```

---

## 3. Registration Service - OCL Constraints

### 3.1 Registration Entity Invariants

```
package University::EventRegistrationSystem::RegistrationService

context Registration
  -- Registration date cannot be in the future
  inv validRegistrationDate:
    self.registrationDate <= Date::now()

  -- Event ID must be provided
  inv validEventId:
    self.eventId->notEmpty()

  -- Participant must be associated
  inv validParticipant:
    self.participant->notEmpty()

  -- Status must be a valid enum value
  inv validStatus:
    self.status = RegistrationStatus::CONFIRMED or
    self.status = RegistrationStatus::CANCELLED or
    self.status = RegistrationStatus::WAITLISTED

  -- A cancelled registration cannot be changed
  inv cancelledRegistrationImmutable:
    self.status = RegistrationStatus::CANCELLED implies
      self.updatedAt = self.registrationDate

  -- Created at must be set
  inv createdAtNotNull:
    self.createdAt->notEmpty()
endpackage
```

### 3.2 Registration - Preconditions and Postconditions

```
package University::EventRegistrationSystem::RegistrationService

context Registration::setStatus(s: RegistrationStatus)
  -- Cannot modify a cancelled registration
  pre: self.status <> RegistrationStatus::CANCELLED

  -- Valid status transitions
  pre: (self.status = RegistrationStatus::CONFIRMED implies
         s = RegistrationStatus::CANCELLED) and
       (self.status = RegistrationStatus::WAITLISTED implies
         s = RegistrationStatus::CONFIRMED or
         s = RegistrationStatus::CANCELLED)
  post: self.status = s


context Registration::cancel(): void
  -- Cannot cancel an already cancelled registration
  pre: self.status <> RegistrationStatus::CANCELLED
  post: self.status = RegistrationStatus::CANCELLED
endpackage
```

### 3.3 Participant Entity Invariants

```
package University::EventRegistrationSystem::RegistrationService

context Participant
  -- Name must not be empty
  inv validName:
    self.name->notEmpty() and self.name.size() <= 100

  -- Email must not be empty and must contain @
  inv validEmail:
    self.email->notEmpty() and self.email->includes('@')

  -- Email must be unique across all participants
  inv uniqueEmail:
    Participant::allInstances()->forAll(p: Participant |
      p <> self implies p.email <> self.email)

  -- Created at must be set
  inv createdAtNotNull:
    self.createdAt->notEmpty()
endpackage
```

### 3.4 RegistrationService Interface - Preconditions and Postconditions

```
package University::EventRegistrationSystem::RegistrationService

context RegistrationService::createRegistration(dto: RegistrationDTO)
  pre: dto.eventId->notEmpty()
  pre: dto.participantName->notEmpty()
  pre: dto.participantEmail->notEmpty() and
       dto.participantEmail->includes('@')
  post: result.eventId = dto.eventId
  post: result.participantName = dto.participantName
  post: result.participantEmail = dto.participantEmail
  post: result.registrationDate->notEmpty()
  post: result.createdAt->notEmpty()
  -- Note: Status determined by capacity check at runtime
  post: result.status = 'CONFIRMED' or result.status = 'WAITLISTED'


context RegistrationService::cancelRegistration(id: Long)
  pre: id->notEmpty()
  post: let reg: Registration = Registration::allInstances()
          ->any(r: Registration | r.id = id) in
    reg.status = RegistrationStatus::CANCELLED


context RegistrationService::getRegistrationsByEvent(eventId: Long)
  pre: eventId->notEmpty()
  post: result->forAll(r: RegistrationResponseDTO | r.eventId = eventId)


context RegistrationService::getRegistrationsByParticipant(participantId: Long)
  pre: participantId->notEmpty()
  post: result->forAll(r: RegistrationResponseDTO |
         r.participantId = participantId)


context RegistrationService::getRegistrationById(id: Long)
  pre: id->notEmpty()
  post: result.id = id
endpackage
```

### 3.5 RegistrationDTO Constraints

```
package University::EventRegistrationSystem::RegistrationService

context RegistrationDTO
  inv: self.eventId->notEmpty()
  inv: self.participantName->notEmpty() implies
         self.participantName.size() <= 100
  inv: self.participantEmail->notEmpty() implies
         self.participantEmail->includes('@')
endpackage
```

---

## 4. User Service - OCL Constraints

### 4.1 User Entity Invariants

```
package University::EventRegistrationSystem::UserService

context User
  -- Username must be between 3 and 50 characters
  inv validUsername:
    self.username.size() >= 3 and self.username.size() <= 50

  -- Username must be unique across all users
  inv uniqueUsername:
    User::allInstances()->forAll(u: User |
      u <> self implies u.username <> self.username)

  -- Email must contain @ symbol
  inv validEmail:
    self.email->includes('@')

  -- Email must be unique across all users
  inv uniqueEmail:
    User::allInstances()->forAll(u: User |
      u <> self implies u.email <> self.email)

  -- Password must be at least 8 characters
  inv validPassword:
    self.password.size() >= 8

  -- At least one role must be assigned
  inv hasAtLeastOneRole:
    self.roles->notEmpty()

  -- Created at must be set
  inv createdAtNotNull:
    self.createdAt->notEmpty()
endpackage
```

### 4.2 User - Preconditions and Postconditions

```
package University::EventRegistrationSystem::UserService

context User::setUsername(u: String)
  pre: u.size() >= 3 and u.size() <= 50
  pre: User::allInstances()->forAll(user: User |
         user <> self implies user.username <> u)
  post: self.username = u


context User::setPassword(p: String)
  pre: p.size() >= 8
  post: self.password = p


context User::setEmail(e: String)
  pre: e->includes('@')
  pre: User::allInstances()->forAll(user: User |
         user <> self implies user.email <> e)
  post: self.email = e
endpackage
```

### 4.3 Role Entity Invariants

```
package University::EventRegistrationSystem::UserService

context Role
  -- Role name must not be empty
  inv validName:
    self.name->notEmpty() and self.name.size() <= 20

  -- Role name must be unique
  inv uniqueName:
    Role::allInstances()->forAll(r: Role |
      r <> self implies r.name <> self.name)

  -- Role name must start with ROLE_ prefix
  inv validRolePrefix:
    self.name->startsWith('ROLE_')
endpackage
```

### 4.4 AuthService Interface - Preconditions and Postconditions

```
package University::EventRegistrationSystem::UserService

context AuthService::registerUser(request: SignupRequest)
  pre: request.username.size() >= 3 and request.username.size() <= 50
  pre: request.email->includes('@')
  pre: request.password.size() >= 8
  pre: request.email->notEmpty()
  pre: request.username->notEmpty()
  pre: request.password->notEmpty()
  pre: User::allInstances()->forAll(u: User |
         u.username <> request.username)
  pre: User::allInstances()->forAll(u: User |
         u.email <> request.email)
  post: result.message = 'User registered successfully!' or
         result.message->startsWith('Error:')


context AuthService::authenticateUser(request: LoginRequest)
  pre: request.username->notEmpty()
  pre: request.password->notEmpty()
  -- On success:
  post: result.token->notEmpty() implies
          result.token.size() > 0 and
          result.type = 'Bearer' and
          result.id->notEmpty() and
          result.username = request.username and
          result.roles->notEmpty()
endpackage
```

### 4.5 UserService Interface - Preconditions and Postconditions

```
package University::EventRegistrationSystem::UserService

context UserService::getUserById(id: Long)
  pre: id->notEmpty()
  post: result.id = id


context UserService::updateUser(id: Long, dto: UserDTO)
  pre: id->notEmpty()
  pre: dto.firstName->notEmpty() implies dto.firstName.size() <= 50
  pre: dto.lastName->notEmpty() implies dto.lastName.size() <= 50
  pre: dto.email->notEmpty() implies dto.email->includes('@')
  pre: dto.phoneNumber->notEmpty() implies dto.phoneNumber.size() <= 20
  post: (dto.firstName->notEmpty() implies result.firstName = dto.firstName)
  post: (dto.lastName->notEmpty() implies result.lastName = dto.lastName)
  post: (dto.email->notEmpty() implies result.email = dto.email)
  post: (dto.phoneNumber->notEmpty() implies result.phoneNumber = dto.phoneNumber)


context UserService::deleteUser(id: Long)
  pre: id->notEmpty()
  post: User::allInstances()->forAll(u: User | u.id <> id)
endpackage
```

### 4.6 SignupRequest Constraints

```
package University::EventRegistrationSystem::UserService

context SignupRequest
  inv: self.username.size() >= 3 and self.username.size() <= 50
  inv: self.email->includes('@')
  inv: self.password.size() >= 8
  inv: self.firstName->notEmpty() implies self.firstName.size() <= 50
  inv: self.lastName->notEmpty() implies self.lastName.size() <= 50
endpackage
```

---

## 5. System-Wide Invariants

```
package University::EventRegistrationSystem

  -- No registration can exceed event capacity
  context RegistrationService::Registration
    inv registrationWithinCapacity:
      let event: EventService::Event = EventService::Event::allInstances()
              ->any(e: EventService::Event | e.id = self.eventId) in
        RegistrationService::Registration::allInstances()
          ->select(r: RegistrationService::Registration |
            r.eventId = self.eventId and
            r.status = RegistrationService::RegistrationStatus::CONFIRMED)
          ->size() <= event.capacity

  -- An event can only have registrations if it is not CANCELLED
  context EventService::Event
    inv noRegistrationsWhenCancelled:
      self.status = EventService::EventStatus::CANCELLED implies
        RegistrationService::Registration::allInstances()
          ->select(r: RegistrationService::Registration |
            r.eventId = self.id)->isEmpty()

  -- A participant cannot register twice for the same event
  context RegistrationService::Registration
    inv noDuplicateRegistration:
      RegistrationService::Registration::allInstances()
        ->select(r: RegistrationService::Registration |
          r.eventId = self.eventId and
          r.participant.id = self.participant.id and
          r.status <> RegistrationService::RegistrationStatus::CANCELLED)
        ->size() <= 1

  -- User who creates event must have ORGANIZER role
  context EventService::Event::createEvent(dto: EventService::EventDTO)
    pre: UserService::User::allInstances()
           ->any(u: UserService::User | u.id = dto.organizerId)
           .roles->exists(r: UserService::Role |
             r.name = 'ROLE_ORGANIZER' or r.name = 'ROLE_ADMIN')
endpackage
```

---

## 6. Service Method Summary

| Service | Method | Preconditions | Postconditions |
|---------|--------|---------------|----------------|
| **Event** | `createEvent` | Valid DTO fields, valid date range, capacity >= 1 | Creates event with UPCOMING status |
| **Event** | `getEventById` | ID exists | Returns matching event |
| **Event** | `updateEvent` | ID exists, valid DTO, event not CANCELLED | Updates all provided fields |
| **Event** | `deleteEvent` | ID exists | Sets status to CANCELLED (soft delete) |
| **Event** | `rescheduleEvent` | ID exists, start < end, event not CANCELLED | Updates start and end dates |
| **Event** | `setStatus` | Status transition valid, not CANCELLED | Updates status |
| **Registration** | `createRegistration` | Valid DTO, valid event ID | Creates CONFIRMED or WAITLISTED |
| **Registration** | `cancelRegistration` | ID exists, not already CANCELLED | Sets status to CANCELLED |
| **Registration** | `getRegistrationsByEvent` | Event ID exists | Returns all registrations for event |
| **Registration** | `getRegistrationsByParticipant` | Participant ID exists | Returns all registrations by participant |
| **Auth** | `registerUser` | Unique username/email, valid password length | Creates user with ROLE_USER, encrypted password |
| **Auth** | `authenticateUser` | Valid credentials | Returns JWT token with user info and roles |
| **User** | `getUserById` | ID exists | Returns user profile without password |
| **User** | `updateUser` | ID exists, valid fields | Updates provided fields |
| **User** | `deleteUser` | ID exists | Removes user from database |

---

## 7. Enum Value Definitions

```
package University::EventRegistrationSystem

enum EventService::Event::EventStatus {
  UPCOMING   -- Event scheduled for future date
  ONGOING    -- Event currently in progress
  COMPLETED  -- Event has concluded
  CANCELLED  -- Event was cancelled
}

enum RegistrationService::Registration::RegistrationStatus {
  CONFIRMED  -- Registration is active and confirmed
  CANCELLED  -- Registration was cancelled
  WAITLISTED -- Registration added to waitlist (capacity full)
}

enum UserService::Role {
  -- Valid role names:
  'ROLE_USER'       -- Standard authenticated user
  'ROLE_ADMIN'      -- Administrator with full access
  'ROLE_ORGANIZER'  -- Event organizer who can create/manage events
}
endpackage
```

---

## 8. Data Type Constraints

| Type | Constraint | Applied To |
|------|------------|------------|
| `String` | size >= 3 | User.username, User.password |
| `String` | size >= 1 and size <= 200 | Event.name |
| `String` | size >= 1 and size <= 100 | Participant.name |
| `String` | size <= 50 | User.firstName, User.lastName |
| `String` | includes('@') | User.email, Participant.email |
| `Integer` | >= 1 | Event.capacity |
| `LocalDateTime` | startDate < endDate | Event |
| `LocalDateTime` | <= Date::now() | Registration.registrationDate |
| `Boolean` | must be true | User.enabled (for active accounts) |
