# Event Registration System

A microservices-based application for event registration, developed for **Software Engineering-2 (Spring 2025)**.

## Architecture

```
┌──────────┐     ┌──────────────┐     ┌─────────────────┐
│  Browser  │────>│  UI Service  │────>│   API Gateway   │
│  :8090    │     │  (Thymeleaf) │     │   :8080 (JWT)   │
└──────────┘     └──────────────┘     └────────┬────────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    │                          │                          │
                    ▼                          ▼                          ▼
             ┌────────────┐           ┌──────────────┐           ┌──────────────┐
             │User Service│           │ Event Service │          │Registration  │
             │:8083 (JWT) │           │   :8081       │          │Service :8082 │
             ├────────────┤           ├──────────────┤           ├──────────────┤
             │PostgreSQL  │           │  PostgreSQL  │           │  PostgreSQL  │
             │  user_db   │           │   event_db   │           │registratn_db│
             └────────────┘           └──────────────┘           └──────────────┘
                                               │                          │
                                               └──────────────────────────┘
                                                    (REST call: capacity
                                                     check via WebClient)

                    ┌─────────────────────────────────────┐
                    │       Discovery Service (Eureka)    │
                    │              :8761                  │
                    └─────────────────────────────────────┘
```

## Microservices

| Service | Port | Description |
|---------|------|-------------|
| **Discovery Service** | 8761 | Eureka Server for service registration and discovery |
| **API Gateway** | 8080 | Spring Cloud Gateway with JWT auth filtering and routing |
| **User Service** | 8083 | Authentication, JWT token generation, user profile management |
| **Event Service** | 8081 | Event CRUD operations, scheduling, status management |
| **Registration Service** | 8082 | Participant registration, capacity checking, waitlisting |
| **UI Service** | 8090 | Thymeleaf-based frontend with HTML/CSS/JS |

## Tech Stack

- **Java 17** + **Spring Boot 3.4.5**
- **Spring Cloud 2024.0.1** (Eureka, Gateway)
- **Spring Data JPA** + **PostgreSQL 16**
- **Spring Security** + **JWT** (jjwt 0.12.6)
- **Thymeleaf** for server-side rendering
- **Maven** multi-module build
- **Docker** + **Docker Compose**

## Project Structure

```
Event-SE2/
├── pom.xml                          # Parent multi-module POM
├── docker-compose.yml               # Full infrastructure orchestration
├── .env                             # Environment variables
├── README.md
├── docs/
│   ├── SRS/                         # Software Requirements Specification
│   │   ├── use-case-diagrams/       # PlantUML + Mermaid diagrams
│   │   ├── activity-diagrams/       # Event creation & registration flows
│   │   ├── sequence-diagrams/       # Auth, create-event, register flows
│   │   ├── class-diagrams/          # Full class diagram (all 6 services)
│   │   └── erd/                     # Entity-Relationship Diagram
│   └── SDD/                         # Software Design Document
│       ├── software-design-document.md  # Full SDD (10 sections)
│       └── ocl-specifications.md       # Formal OCL constraints
├── discovery-service/               # Eureka Server
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
├── event-service/                   # Event CRUD microservice
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
├── registration-service/            # Registration microservice
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
├── user-service/                    # Auth + User management
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
├── api-gateway/                     # Spring Cloud Gateway
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
└── ui-service/                      # Thymeleaf frontend
    ├── pom.xml
    ├── Dockerfile
    └── src/main/
```

## API Endpoints

### User Service (`:8083`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/signup` | No | Register a new user |
| POST | `/api/auth/login` | No | Login, returns JWT token |
| GET | `/api/users/{id}` | Yes | Get user profile |
| PUT | `/api/users/{id}` | Yes | Update user profile |
| DELETE | `/api/users/{id}` | Yes | Delete user account |

### Event Service (`:8081`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events` | Create a new event |
| GET | `/api/events` | Get all events |
| GET | `/api/events/upcoming` | Get upcoming events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/organizer/{id}` | Get events by organizer |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Cancel event (soft-delete) |
| PATCH | `/api/events/{id}/reschedule` | Reschedule event |

### Registration Service (`:8082`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/participants` | Register a new participant |
| GET | `/api/participants/{id}` | Get participant details |
| POST | `/api/registrations` | Register for an event |
| GET | `/api/registrations/{id}` | Get registration by ID |
| DELETE | `/api/registrations/{id}` | Cancel registration |
| GET | `/api/events/{eventId}/registrations` | List registrations for event |
| GET | `/api/participants/{id}/registrations` | List participant's registrations |

### API Gateway (`:8080`)
All above endpoints are accessible through the gateway at `:8080` (e.g., `http://localhost:8080/api/events`).

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.9+**
- **Docker** & **Docker Compose** (for containerized run)
- **PostgreSQL 16** (if running locally without Docker)

## Getting Started

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repo-url>
cd Event-SE2

# Build and start all services
docker-compose up --build

# Wait for all services to start (may take 2-3 minutes)
# Access the UI at: http://localhost:8090
# Access Eureka Dashboard at: http://localhost:8761
```

### Option 2: Run Locally (Without Docker)

**Step 1:** Start PostgreSQL databases

```bash
# Create databases
createdb event_db
createdb registration_db
createdb user_db
```

**Step 2:** Start Discovery Service

```bash
cd discovery-service
mvn spring-boot:run
```

**Step 3:** Start microservices (in separate terminals)

```bash
# Terminal 2 - Event Service
cd event-service
mvn spring-boot:run

# Terminal 3 - Registration Service
cd registration-service
mvn spring-boot:run

# Terminal 4 - User Service
cd user-service
mvn spring-boot:run
```

**Step 4:** Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

**Step 5:** Start UI Service

```bash
cd ui-service
mvn spring-boot:run
```

**Step 6:** Open [http://localhost:8090](http://localhost:8090) in your browser.

### Option 3: Build All Services

```bash
# From the project root
mvn clean package -DskipTests

# Run a specific service
java -jar event-service/target/*.jar
```

## Authentication Flow

1. **Sign Up** at `/register` or via `POST /api/auth/signup`
2. **Log In** at `/login` or via `POST /api/auth/login` → receive JWT token
3. Token is stored in `localStorage` and sent as `Authorization: Bearer <token>` header
4. **API Gateway** validates the JWT before forwarding requests to services
5. Protected endpoints require a valid JWT token

Default roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_ORGANIZER`

## Documentation

All documentation is located in the `docs/` directory:

| Document | Location | Format |
|----------|----------|--------|
| Use Case Diagrams | `docs/SRS/use-case-diagrams/` | PlantUML + Mermaid |
| Activity Diagrams | `docs/SRS/activity-diagrams/` | PlantUML + Mermaid |
| Sequence Diagrams | `docs/SRS/sequence-diagrams/` | PlantUML + Mermaid |
| Class Diagrams | `docs/SRS/class-diagrams/` | PlantUML + Mermaid |
| ER Diagram | `docs/SRS/erd/` | PlantUML + Mermaid |
| SDD | `docs/SDD/software-design-document.md` | Markdown |
| OCL Specifications | `docs/SDD/ocl-specifications.md` | Markdown |

To render PlantUML diagrams, use the [PlantUML Online Server](https://www.plantuml.com/plantuml/uml/) or install the PlantUML CLI.
