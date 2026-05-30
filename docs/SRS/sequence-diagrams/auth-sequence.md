# Sequence Diagram - Authentication

```mermaid
sequenceDiagram
    participant U as User
    participant UI as UI (Login Form)
    participant GW as API Gateway (:8080)
    participant AC as Auth Controller
    participant AS as Auth Service
    participant AM as AuthenticationManager
    participant UD as UserDetailsService
    participant JU as JWT Utils
    participant UR as User Repository
    participant UDB as User DB (PostgreSQL)

    U->>UI: Enter username & password
    UI->>GW: POST /api/auth/login (LoginRequest JSON)
    activate GW
    GW->>GW: Public path - skip JWT filter
    GW->>AC: POST /api/auth/login
    activate AC
    AC->>AS: authenticateUser(LoginRequest)
    activate AS
    AS->>AM: authenticate(token)
    activate AM
    AM->>UD: loadUserByUsername(username)
    activate UD
    UD->>UR: findByUsername(username)
    activate UR
    UR->>UDB: SELECT * FROM users WHERE username=?
    activate UDB
    UDB-->>UR: User entity
    deactivate UDB
    UR-->>UD: User
    deactivate UR
    UD->>UD: Build UserDetailsImpl with roles
    UD-->>AM: UserDetails
    deactivate UD
    AM-->>AS: Authentication object
    deactivate AM
    AS->>JU: generateJwtToken(authentication)
    activate JU
    JU-->>AS: JWT string
    deactivate JU
    AS-->>AC: JwtResponse (token, id, username, email, roles)
    deactivate AS
    AC-->>GW: 200 OK (JwtResponse JSON)
    deactivate AC
    GW-->>UI: JSON response (JWT token)
    deactivate GW
    UI->>UI: Store token in localStorage
    UI-->>U: Redirect to dashboard
```

## Step-by-Step Flow

| Step | Action | Description |
|------|--------|-------------|
| 1 | User submits credentials | Enters username and password |
| 2 | UI forwards request | POST to API Gateway (public endpoint) |
| 3 | Gateway skips JWT filter | Path `/api/auth/login` is whitelisted |
| 4 | AuthController receives | Delegates to AuthService |
| 5 | Authentication | AuthenticationManager validates credentials |
| 6 | User lookup | UserDetailsServiceImpl loads user from DB |
| 7 | JWT generation | JwtUtils generates signed JWT token |
| 8 | Response | JwtResponse returned with token, user info, roles |
| 9 | Token storage | UI stores token in localStorage for subsequent requests |

## Endpoint Details

- **Login URL**: `POST /api/auth/login`
- **Signup URL**: `POST /api/auth/signup`
- **Authentication**: None (public endpoints)
- **Login Request Body**: LoginRequest (username, password)
- **Login Response**: 200 OK with JwtResponse (token, id, username, email, roles)
- **Signup Request Body**: SignupRequest (username, email, password, firstName, lastName, phoneNumber)
- **Signup Response**: 200 OK with MessageResponse
