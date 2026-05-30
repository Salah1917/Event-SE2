# Software Design Document (SDD) - Event Registration System

## 1. Introduction

### 1.1 Purpose
This Software Design Document (SDD) describes the architectural design, component decomposition, interface specifications, and design decisions for the Event Registration System. The system enables event organizers to create and manage events, participants to register for events, and administrators to oversee the entire platform.

### 1.2 Scope
The Event Registration System is a microservices-based web application that provides comprehensive event management and registration capabilities. It consists of six independently deployable services handling specific domains: user management, event management, registration processing, API routing, service discovery, and user interface rendering.

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|------|------------|
| JWT | JSON Web Token - used for stateless authentication |
| API | Application Programming Interface |
| REST | Representational State Transfer |
| CRUD | Create, Read, Update, Delete |
| DTO | Data Transfer Object |
| JPA | Jakarta Persistence API |
| Eureka | Netflix service discovery server |
| Gateway | API Gateway - single entry point for all client requests |
| AOP | Aspect-Oriented Programming |
| OCL | Object Constraint Language |
| ERD | Entity Relationship Diagram |

## 2. System Architecture

### 2.1 Architecture Style
The system follows a **microservices architecture** pattern, where each service is independently deployable, owns its data store, and communicates via RESTful HTTP APIs. The API Gateway serves as the single entry point, and Eureka provides service discovery.

### 2.2 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Client / Browser                             │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      UI Service (:8090)                              │
│                   (Thymeleaf Templates, HomeController)              │
└─────────────────────────────┬───────────────────────────────────────┘
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       API Gateway (:8080)                            │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter (GlobalFilter, order=-100)          │   │
│  │  RouteConfig: lb://user-service, lb://event-service,        │   │
│  │               lb://registration-service                      │   │
│  │  GlobalErrorHandler (ErrorAttributes, RouterFunction)        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────┬─────────────────────┬─────────────────────┬──────────────────┘
       │                     │                     │
       ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│ User Service │    │Event Service │    │Registration      │
│   (:8083)    │    │   (:8081)    │    │Service (:8082)    │
├──────────────┤    ├──────────────┤    ├──────────────────┤
│ AuthController│    │EventController│   │RegistrationCtrl  │
│ UserController│    │EventService   │   │RegistrationService│
│ AuthService   │    │EventServiceImpl│  │RegistrationImpl   │
│ UserService   │    │EventRepository │  │RegistrationRepo   │
│ JwtUtils      │    │LoggingAspect   │  │ParticipantRepo    │
│ AuthTokenFilter│   │PerfMonAspect   │  │                   │
│ WebSecurityCfg │   │GlobalExHandler │  │                   │
└──────┬────────┘    └──────┬────────┘  └────────┬─────────┘
       │                    │                     │
       ▼                    ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│  User DB     │    │  Event DB    │    │Registration DB   │
│ (PostgreSQL) │    │ (PostgreSQL) │    │(PostgreSQL)      │
└──────────────┘    └──────────────┘    └──────────────────┘

                    ┌──────────────────────┐
                    │Discovery Service     │
                    │  Eureka Server (:8761)│
                    └──────────────────────┘
```

### 2.3 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.4.x |
| Service Discovery | Spring Cloud Netflix Eureka | Latest |
| API Gateway | Spring Cloud Gateway | Latest |
| ORM | Spring Data JPA (Hibernate) | 6.x |
| Database | PostgreSQL | 16 |
| Authentication | JWT (io.jsonwebtoken) | 0.12.x |
| Security | Spring Security | 6.x |
| Password Encoding | BCryptPasswordEncoder | - |
| Frontend | Thymeleaf | Latest |
| Build Tool | Maven | 3.9+ |
| Containerization | Docker | Latest |
| Aspects | Spring AOP (AspectJ) | - |
| Logging | SLF4J + Logback | - |

## 3. Service Decomposition

### 3.1 Discovery Service (Eureka Server)

| Property | Value |
|----------|-------|
| **Port** | `8761` |
| **Application Name** | `discovery-service` |
| **Base Package** | `com.university.discovery_service` |
| **Main Class** | `DiscoveryServiceApplication` |

**Responsibilities:**
- Service registration — all microservices register with Eureka on startup
- Service discovery — clients look up service locations by logical name
- Health monitoring — track service heartbeats and remove unhealthy instances
- Load balancing — provides instance lists for Ribbon-based client-side load balancing

**Dependencies:** None (standalone Eureka server)

**Configuration:**
```yaml
server.port: 8761
eureka.client.register-with-eureka: false
eureka.client.fetch-registry: false
```

### 3.2 API Gateway

| Property | Value |
|----------|-------|
| **Port** | `8080` |
| **Application Name** | `api-gateway` |
| **Base Package** | `com.university.api_gateway` |
| **Main Class** | `ApiGatewayApplication` |

**Responsibilities:**
- **Routing**: Forward requests to the appropriate microservice based on path
- **JWT Validation**: Global filter validates JWT tokens on all protected routes
- **Load Balancing**: Uses `lb://` prefix to balance across service instances
- **Error Handling**: Custom `GlobalErrorHandler` for consistent error responses

**Dependencies:** Discovery Service

**Route Configuration:**

| Route ID | Path | Target URI |
|----------|------|------------|
| `user-service` | `/api/auth/**`, `/api/users/**` | `lb://user-service` |
| `event-service` | `/api/events/**` | `lb://event-service` |
| `registration-service` | `/api/registrations/**`, `/api/participants/**` | `lb://registration-service` |

**Filter Configuration:**
```java
JwtAuthenticationFilter implements GlobalFilter, Ordered
  - order: -100 (high priority)
  - white-listed paths: /api/auth/login, /api/auth/signup
  - extracts JWT from Authorization: Bearer <token> header
  - validates token using JwtUtils
  - injects userId header into downstream requests
```

**Global Error Handler:**
```java
GlobalErrorHandler @Configuration
  - custom ErrorAttributes (removes trace, path, error fields)
  - RouterFunction for all error routes
  - HTTP status determination: IllegalArgumentException → 400, SecurityException → 401
```

### 3.3 User Service

| Property | Value |
|----------|-------|
| **Port** | `8083` |
| **Application Name** | `user-service` |
| **Base Package** | `com.university.user_service` |
| **Main Class** | `UserServiceApplication` |

**Responsibilities:**
- **User Registration**: Create new user accounts with validation
- **User Authentication**: Validate credentials and issue JWT tokens
- **User Profile Management**: Read and update user profile information
- **Role-Based Authorization**: Assign roles (ROLE_USER, ROLE_ADMIN, ROLE_ORGANIZER)

**Dependencies:** Discovery Service, User DB (PostgreSQL)

**Key Endpoints:**

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/signup` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Authenticate and receive JWT |
| `GET` | `/api/users/{id}` | JWT | Get user profile |
| `PUT` | `/api/users/{id}` | JWT | Update user profile |
| `DELETE` | `/api/users/{id}` | JWT | Delete user account |

**Authentication Flow:**
1. Client sends `POST /api/auth/login` with `LoginRequest` (username, password)
2. `AuthController` delegates to `AuthServiceImpl.authenticateUser()`
3. `AuthenticationManager` validates credentials via `UserDetailsServiceImpl`
4. `UserDetailsServiceImpl.loadUserByUsername()` fetches user from database
5. On success, `JwtUtils.generateJwtToken()` creates a signed JWT (24h expiry)
6. `JwtResponse` returned: `{ token, type: "Bearer", id, username, email, roles }`

### 3.4 Event Service

| Property | Value |
|----------|-------|
| **Port** | `8081` |
| **Application Name** | `event-service` |
| **Base Package** | `com.university.event_service` |
| **Main Class** | `EventServiceApplication` |

**Responsibilities:**
- **Create Events**: Store new events with name, description, location, dates, capacity
- **Update Events**: Modify existing event details
- **Cancel Events**: Soft-delete by setting status to CANCELLED
- **Reschedule Events**: Change event start/end dates via PATCH endpoint
- **Query Events**: List all events, upcoming events, events by organizer

**Dependencies:** Discovery Service, Event DB (PostgreSQL)

**Key Endpoints:**

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/events` | JWT | Create a new event |
| `GET` | `/api/events` | JWT | Get all events |
| `GET` | `/api/events/{id}` | JWT | Get event by ID |
| `PUT` | `/api/events/{id}` | JWT | Update event |
| `DELETE` | `/api/events/{id}` | JWT | Cancel event (set status CANCELLED) |
| `PATCH` | `/api/events/{id}/reschedule` | JWT | Reschedule event dates |
| `GET` | `/api/events/upcoming` | JWT | Get upcoming events |
| `GET` | `/api/events/organizer/{organizerId}` | JWT | Get events by organizer |

**AOP Aspects Applied:**
- `LoggingAspect`: Logs method entry/exit with arguments and execution time
- `PerformanceMonitoringAspect`: Warns if service method exceeds 1000ms threshold

### 3.5 Registration Service

| Property | Value |
|----------|-------|
| **Port** | `8082` |
| **Application Name** | `registration-service` |
| **Base Package** | `com.university.registration_service` |
| **Main Class** | `RegistrationServiceApplication` |

**Responsibilities:**
- **Participant Registration**: Register a participant for an event with capacity check
- **Waitlist Management**: Auto-waitlist when capacity is full
- **Cancel Registration**: Set registration status to CANCELLED
- **Query Registrations**: List registrations by event, participant, or registration ID
- **Participant Management**: Create/find participants by email

**Dependencies:** Discovery Service, Event Service (REST call for capacity check), Registration DB (PostgreSQL)

**Key Endpoints:**

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/registrations` | JWT | Register for an event |
| `DELETE` | `/api/registrations/{id}` | JWT | Cancel a registration |
| `GET` | `/api/registrations/{id}` | JWT | Get registration by ID |
| `GET` | `/api/events/{eventId}/registrations` | JWT | List registrations for an event |
| `GET` | `/api/participants/{participantId}/registrations` | JWT | List registrations by participant |

**Inter-Service Communication:**
- Registration Service calls Event Service via `RestTemplate` at `GET /api/events/{eventId}` to fetch event capacity
- This is done synchronously during registration creation

**Capacity Logic:**
```
if (currentConfirmedRegistrations < eventCapacity) {
    status = CONFIRMED
} else {
    status = WAITLISTED
}
```

### 3.6 UI Service

| Property | Value |
|----------|-------|
| **Port** | `8090` |
| **Application Name** | `ui-service` |
| **Base Package** | `com.university.ui_service` |
| **Main Class** | `UiServiceApplication` |

**Responsibilities:**
- **Frontend Rendering**: Serve Thymeleaf HTML templates
- **Page Routing**: Map URL paths to view templates
- **Static Resources**: Serve CSS, JavaScript, and image assets

**Dependencies:** Discovery Service, API Gateway

**View Templates:**

| URL Path | Template | Description |
|----------|----------|-------------|
| `/` | `index.html` | Home/landing page |
| `/login` | `login.html` | User login form |
| `/register` | `register.html` | User registration form |
| `/events` | `events/list.html` | Event listing page |
| `/events/create` | `events/create.html` | Create new event form |
| `/events/{id}` | `events/detail.html` | Event detail page |
| `/profile` | `user-profile.html` | User profile page |

**Static Assets:**
- `css/` - Stylesheets
- `js/auth.js` - Authentication JavaScript (JWT token management, localStorage)
- `images/` - Image assets

## 4. Database Design

### 4.1 Schema Overview

The system uses three separate PostgreSQL databases, one per service (User Service, Event Service, Registration Service), following the database-per-service pattern.

### 4.2 User Service Schema

**Table: `users`**

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Unique user identifier |
| `username` | `VARCHAR(50)` | UNIQUE, NOT NULL | Login username |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL | User email address |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt-encrypted password |
| `first_name` | `VARCHAR(50)` | NULLABLE | User's first name |
| `last_name` | `VARCHAR(50)` | NULLABLE | User's last name |
| `phone_number` | `VARCHAR(20)` | NULLABLE | Contact phone number |
| `enabled` | `BOOLEAN` | DEFAULT TRUE | Account enabled flag |
| `account_non_locked` | `BOOLEAN` | DEFAULT TRUE | Account lock status |
| `account_non_expired` | `BOOLEAN` | DEFAULT TRUE | Account expiry status |
| `credentials_non_expired` | `BOOLEAN` | DEFAULT TRUE | Credential expiry |
| `last_login_date` | `TIMESTAMP` | NULLABLE | Last successful login |
| `created_at` | `TIMESTAMP` | NOT NULL | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL | Last update timestamp |

**Table: `roles`**

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Unique role identifier |
| `name` | `VARCHAR(20)` | UNIQUE, NOT NULL | Role name (ROLE_USER, ROLE_ADMIN, ROLE_ORGANIZER) |

**Table: `user_roles`** (Junction Table)

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `user_id` | `BIGINT` | PK (composite), FK → users(id) | Reference to user |
| `role_id` | `BIGINT` | PK (composite), FK → roles(id) | Reference to role |

### 4.3 Event Service Schema

**Table: `events`**

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Unique event identifier |
| `name` | `VARCHAR(200)` | NOT NULL | Event name |
| `description` | `TEXT` | NOT NULL | Event description |
| `location` | `VARCHAR(255)` | NOT NULL | Event location/venue |
| `start_date` | `TIMESTAMP` | NOT NULL | Event start date/time |
| `end_date` | `TIMESTAMP` | NOT NULL | Event end date/time |
| `capacity` | `INTEGER` | NOT NULL | Maximum attendees |
| `status` | `VARCHAR(20)` | NOT NULL | UPCOMING, ONGOING, COMPLETED, CANCELLED |
| `organizer_id` | `BIGINT` | NOT NULL | ID of the organizer user |
| `created_at` | `TIMESTAMP` | NOT NULL | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL | Last update timestamp |

### 4.4 Registration Service Schema

**Table: `participants`**

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Unique participant identifier |
| `name` | `VARCHAR(100)` | NOT NULL | Participant's full name |
| `email` | `VARCHAR(100)` | UNIQUE, NOT NULL | Participant email (used for lookup) |
| `phone` | `VARCHAR(20)` | NULLABLE | Contact phone number |
| `created_at` | `TIMESTAMP` | NOT NULL | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL | Last update timestamp |

**Table: `registrations`**

| Column | Type | Constraints | Description |
|--------|------|------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Unique registration identifier |
| `event_id` | `BIGINT` | FK → events(id), NOT NULL | Event being registered for |
| `participant_id` | `BIGINT` | FK → participants(id), NOT NULL | Registered participant |
| `registration_date` | `TIMESTAMP` | NOT NULL | When registration occurred |
| `status` | `VARCHAR(20)` | NOT NULL | CONFIRMED, CANCELLED, WAITLISTED |
| `created_at` | `TIMESTAMP` | NOT NULL | Record creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL | Last update timestamp |

### 4.5 Index Recommendations

| Table | Index Columns | Type | Rationale |
|-------|--------------|------|-----------|
| `users` | `username` | UNIQUE | Login lookup (exact match) |
| `users` | `email` | UNIQUE | Profile lookup |
| `events` | `start_date` | B-tree | Sorting upcoming events |
| `events` | `status` | B-tree | Filter by status |
| `events` | `organizer_id` | B-tree | Organizer event listing |
| `registrations` | `event_id` | B-tree | Event registration listing |
| `registrations` | `participant_id` | B-tree | Participant history |
| `registrations` | `event_id, status` | Composite | Capacity count query |
| `participants` | `email` | UNIQUE | Find-or-create lookup |

### 4.6 Entity Relationships

- **users** `1 --- *` **user_roles**: A user can have multiple role assignments
- **roles** `1 --- *` **user_roles**: A role can be assigned to multiple users
- **events** `1 --- *` **registrations**: An event can have many registrations (logical FK, no database-level constraint as registrations are in a different database)
- **participants** `1 --- *` **registrations**: A participant can make multiple registrations

## 5. Interface Specifications

### 5.1 RESTful API Conventions

- **Base URL**: `http://<gateway-host>:8080`
- **Content-Type**: `application/json` for all requests and responses
- **Authentication**: JWT token in `Authorization: Bearer <token>` header
- **Resource Naming**: Plural nouns (`/api/events`, `/api/users`, `/api/registrations`)
- **HTTP Methods**: GET (read), POST (create), PUT (update), PATCH (partial update), DELETE (delete/cancel)

### 5.2 Authentication Flow

```
Client                    API Gateway                    User Service
  │                            │                              │
  │  POST /api/auth/login      │                              │
  │  {username, password}      │                              │
  │ ─────────────────────────► │  Forward to lb://user-service │
  │                            │ ────────────────────────────► │
  │                            │                              │
  │                            │    Validate credentials      │
  │                            │    Generate JWT token        │
  │                            │                              │
  │  JwtResponse               │                              │
  │  {token, id, username,     │ ◄──────────────────────────── │
  │   email, roles}            │                              │
  │ ◄───────────────────────── │                              │
  │                            │                              │
  │ Store token in localStorage│                              │
  │                            │                              │
  │  GET /api/events           │                              │
  │  Authorization: Bearer JWT │                              │
  │ ─────────────────────────► │  Validate JWT                │
  │                            │  Forward with userId header  │
  │                            │ ────────────────────────────► │
  │  EventResponseDTO[]        │                              │
  │ ◄───────────────────────── │ ◄──────────────────────────── │
```

### 5.3 Request/Response Formats

**EventDTO (Request):**
```json
{
  "name": "Spring Boot Workshop",
  "description": "Hands-on workshop on Spring Boot microservices",
  "location": "Room 301, Engineering Building",
  "startDate": "2026-06-15T09:00:00",
  "endDate": "2026-06-15T17:00:00",
  "capacity": 50,
  "organizerId": 1
}
```

**EventResponseDTO (Response):**
```json
{
  "id": 1,
  "name": "Spring Boot Workshop",
  "description": "Hands-on workshop on Spring Boot microservices",
  "location": "Room 301, Engineering Building",
  "startDate": "2026-06-15T09:00:00",
  "endDate": "2026-06-15T17:00:00",
  "capacity": 50,
  "status": "UPCOMING",
  "organizerId": 1,
  "createdAt": "2026-05-29T10:30:00",
  "updatedAt": "2026-05-29T10:30:00"
}
```

**RegistrationDTO (Request):**
```json
{
  "eventId": 1,
  "participantId": 2,
  "participantName": "John Doe",
  "participantEmail": "john@example.com",
  "status": "CONFIRMED"
}
```

**RegistrationResponseDTO (Response):**
```json
{
  "id": 100,
  "eventId": 1,
  "participantId": 2,
  "participantName": "John Doe",
  "participantEmail": "john@example.com",
  "registrationDate": "2026-05-29T11:00:00",
  "status": "CONFIRMED",
  "createdAt": "2026-05-29T11:00:00"
}
```

**SignupRequest:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**LoginRequest:**
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**JwtResponse:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### 5.4 HTTP Status Codes

| Status Code | Usage |
|-------------|-------|
| `200 OK` | Successful GET, PUT, PATCH requests |
| `201 Created` | Successful POST (resource created) |
| `204 No Content` | Successful DELETE |
| `400 Bad Request` | Validation failure, malformed request |
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Insufficient role permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate resource (e.g., duplicate username) |
| `500 Internal Server Error` | Unexpected server error |

## 6. Error Handling Strategy

### 6.1 Global Exception Handling per Service

Each service implements a `@RestControllerAdvice` (Spring MVC) or equivalent (Spring WebFlux for Gateway) to provide centralized error handling.

### 6.2 User Service Error Responses

```json
{
  "message": "Error description"
}
```

| Exception | HTTP Status | Message |
|-----------|-------------|---------|
| `ResourceNotFoundException` | 404 | "User not found with id: {id}" |
| `BadCredentialsException` | 401 | "Invalid username or password" |
| `UsernameNotFoundException` | 404 | "User not found with username: {username}" |
| `IllegalArgumentException` | 400 | Exception message |
| `Exception` (generic) | 500 | "An unexpected error occurred" |

### 6.3 Event Service Error Responses

```json
{
  "timestamp": "2026-05-29T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: 999",
  "id": 999
}
```

| Exception | HTTP Status | Error |
|-----------|-------------|-------|
| `ResourceNotFoundException` | 404 | "Not Found" |
| `IllegalArgumentException` | 400 | "Bad Request" |
| `Exception` (generic) | 500 | "Internal Server Error" |

### 6.4 Gateway Error Responses

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

| Condition | HTTP Status |
|-----------|-------------|
| Missing/invalid JWT | 401 Unauthorized |
| IllegalArgumentException | 400 Bad Request |
| SecurityException | 401 Unauthorized |
| Unhandled errors | 500 Internal Server Error |

### 6.5 Standard Error Response Format

```json
{
  "status": <HTTP_STATUS_CODE>,
  "error": "<ERROR_TYPE>",
  "message": "<HUMAN_READABLE_MESSAGE>",
  "timestamp": "<ISO_TIMESTAMP>"
}
```

## 7. Security Design

### 7.1 Authentication (JWT)

The system uses **JSON Web Token (JWT)** for stateless authentication:

1. **Token Generation** (`JwtUtils.generateJwtToken`):
   - Signing algorithm: HMAC-SHA512 (via `Keys.hmacShaKeyFor`)
   - Claims: `sub` (username), `iat` (issued at), `exp` (expiration)
   - Expiration: 24 hours (configurable via `app.jwtExpirationMs`)

2. **Token Validation** (`JwtUtils.validateJwtToken`):
   - Signature verification
   - Expiration check
   - Malformed/unsupported token detection

3. **Token Extraction** (`AuthTokenFilter.parseJwt`):
   - Reads `Authorization: Bearer <token>` header
   - Strips "Bearer " prefix
   - Returns null if header is missing or malformed

### 7.2 Authorization (Role-Based)

**Roles Defined:**
| Role | Constant | Description |
|------|----------|-------------|
| `ROLE_USER` | Default role | Basic authenticated user, can register for events |
| `ROLE_ADMIN` | Admin | Full system access, manage users and all events |
| `ROLE_ORGANIZER` | Organizer | Can create and manage events |

**Security Configuration** (`WebSecurityConfig`):
- CSRF protection disabled (stateless API)
- Session management: STATELESS
- Public paths: `/api/auth/**` (permit all)
- All other paths: authenticated
- `AuthTokenFilter` added before `UsernamePasswordAuthenticationFilter`

### 7.3 Password Encryption

- Algorithm: **BCrypt** (via `BCryptPasswordEncoder`)
- Salt is automatically generated and embedded in the hash
- Passwords are never stored in plaintext
- Used during signup (encoding) and login (matching)

### 7.4 API Gateway as Security Perimeter

- The API Gateway is the **single entry point** for all client requests
- The `JwtAuthenticationFilter` (GlobalFilter, order=-100) validates all incoming JWTs
- Whitelisted paths (`/api/auth/login`, `/api/auth/signup`) bypass JWT validation
- Validated user identity is forwarded to downstream services via the `userId` header
- Downstream services can perform additional authorization checks

## 8. Aspect-Oriented Programming

### 8.1 LoggingAspect

**Location:** `com.university.event_service.aspect.LoggingAspect`

**Pointcuts:**
- `serviceLayer()`: matches `com.university.event_service.service..*.*(..)`
- `repositoryLayer()`: matches `com.university.event_service.repository..*.*(..)`

**Advice:**

| Type | Pointcut | Behavior |
|------|----------|----------|
| `@Around` | serviceLayer() OR repositoryLayer() | Logs method entry with arguments, exit with execution time, exception with timing |
| `@AfterThrowing` | serviceLayer() OR repositoryLayer() | Logs full exception stack trace when thrown |

**Log Output Examples:**
```
INFO  - Entering: EventServiceImpl.createEvent(..) with arguments: [EventDTO{...}]
INFO  - Exiting: EventServiceImpl.createEvent(..) completed in 45 ms
ERROR - Exception in: EventServiceImpl.getEventById(..) after 12 ms: Event not found with id: 999
```

### 8.2 PerformanceMonitoringAspect

**Location:** `com.university.event_service.aspect.PerformanceMonitoringAspect`

**Pointcut:**
- `serviceLayer()`: matches `com.university.event_service.service..*.*(..)`

**Threshold:** 1000 ms (configurable via `THRESHOLD_MS` constant)

**Advice:**

| Type | Pointcut | Behavior |
|------|----------|----------|
| `@Around` | serviceLayer() | Measures execution time; logs WARN if duration exceeds 1000ms |

**Log Output Example:**
```
WARN  - Performance warning: com.university.event_service.service.EventServiceImpl.getAllEvents
        took 2340 ms (threshold: 1000 ms)
```

## 9. OCL Constraints

See [ocl-specifications.md](./ocl-specifications.md) for the complete OCL specification document.

### 9.1 Event Invariants

```
context Event inv:
  self.name->notEmpty() and self.name.size() <= 200

context Event inv:
  self.capacity >= 1

context Event inv:
  self.startDate < self.endDate
```

### 9.2 Event Pre/Post Conditions

```
context Event::setStatus(s):
  pre: self.status <> EventStatus::CANCELLED
```

### 9.3 Registration Invariants

```
context Registration inv:
  self.registrationDate <= Date::now()

context Registration inv:
  self.eventId <> null
```

### 9.4 Registration Pre/Post Conditions

```
context Registration::cancel():
  post: self.status = RegistrationStatus::CANCELLED
```

### 9.5 User Invariants

```
context User inv:
  self.username.size() >= 3 and self.username.size() <= 50

context User inv:
  self.email->includes('@')

context User inv:
  self.password.size() >= 8
```

## 10. Deployment

### 10.1 Docker Compose Configuration

```yaml
version: '3.8'
services:
  discovery-service:
    build: ./discovery-service
    ports:
      - "8761:8761"
    container_name: discovery-service

  user-service:
    build: ./user-service
    ports:
      - "8083:8083"
    environment:
      - eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/
      - spring.datasource.url=jdbc:postgresql://user-db:5432/userdb
    depends_on:
      - discovery-service
      - user-db
    container_name: user-service

  event-service:
    build: ./event-service
    ports:
      - "8081:8081"
    environment:
      - eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/
      - spring.datasource.url=jdbc:postgresql://event-db:5432/eventdb
    depends_on:
      - discovery-service
      - event-db
    container_name: event-service

  registration-service:
    build: ./registration-service
    ports:
      - "8082:8082"
    environment:
      - eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/
      - spring.datasource.url=jdbc:postgresql://registration-db:5432/registrationdb
    depends_on:
      - discovery-service
      - registration-db
    container_name: registration-service

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/
    depends_on:
      - discovery-service
    container_name: api-gateway

  ui-service:
    build: ./ui-service
    ports:
      - "8090:8090"
    environment:
      - eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/
    depends_on:
      - discovery-service
      - api-gateway
    container_name: ui-service

  user-db:
    image: postgres:16
    environment:
      POSTGRES_DB: userdb
      POSTGRES_USER: user_service
      POSTGRES_PASSWORD: user_pass
    ports:
      - "5433:5432"
    container_name: user-db

  event-db:
    image: postgres:16
    environment:
      POSTGRES_DB: eventdb
      POSTGRES_USER: event_service
      POSTGRES_PASSWORD: event_pass
    ports:
      - "5434:5432"
    container_name: event-db

  registration-db:
    image: postgres:16
    environment:
      POSTGRES_DB: registrationdb
      POSTGRES_USER: registration_service
      POSTGRES_PASSWORD: registration_pass
    ports:
      - "5435:5432"
    container_name: registration-db
```

### 10.2 Build and Run Instructions

**Prerequisites:**
- Java 17 JDK
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (for local development without Docker)

**Build all services:**
```bash
mvn clean package -DskipTests
```

**Run with Docker Compose:**
```bash
docker-compose up --build
```

**Run individually (development):**
```bash
# Start discovery service first
cd discovery-service && mvn spring-boot:run

# Start other services in any order
cd user-service && mvn spring-boot:run
cd event-service && mvn spring-boot:run
cd registration-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd ui-service && mvn spring-boot:run
```

**Verify deployment:**
- Eureka Dashboard: http://localhost:8761
- UI: http://localhost:8090
- API Gateway: http://localhost:8080
