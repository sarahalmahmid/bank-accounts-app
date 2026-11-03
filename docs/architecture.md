# Architecture

How the Android app, the backend API and the database communicate.

## Components

```mermaid
flowchart LR
    subgraph Android["Android app (Kotlin, Jetpack Compose)"]
        direction TB
        UI["Screens<br/>Login · Accounts · Account details"]
        VM["ViewModels<br/>screen state (StateFlow)"]
        Repo["Repositories<br/>AuthRepository · AccountsRepository"]
        Api["Retrofit ApiService<br/>OkHttp + AuthInterceptor"]
        Store[("DataStore<br/>access token")]
        UI --> VM --> Repo
        Repo --> Api
        Repo --> Store
        Api -. reads token .-> Store
    end

    subgraph Backend["Backend API (Node.js, TypeScript, Express)"]
        direction TB
        Routes["Routes"]
        Auth["requireAuth middleware<br/>verifies JWT"]
        Ctrl["Controllers<br/>zod validation"]
        Svc["Services<br/>SQL queries"]
        Routes -- "/api/auth/login" --> Ctrl
        Routes -- "/api/accounts/*" --> Auth --> Ctrl
        Ctrl --> Svc
    end

    DB[("PostgreSQL in Docker<br/>customers · accounts")]

    Api -- "REST over HTTP, JSON<br/>Authorization: Bearer JWT" --> Routes
    Svc -- "parameterised SQL" --> DB
```

- **Android** follows MVVM: screens only draw state, ViewModels hold screen state,
  repositories are the single source of data, and Retrofit handles HTTP.
- **AuthInterceptor** adds the token to every request, and clears it on a 401.
- **Backend** is layered: routes → (auth middleware) → controllers (validation)
  → services (database).

## Main flow: login, list accounts, update nickname

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant App as Android app
    participant API as Express API
    participant DB as PostgreSQL

    Note over User,DB: Login
    User->>App: Enter customer number and password
    App->>API: POST /api/auth/login
    API->>DB: SELECT password_hash by customer_number
    DB-->>API: password hash
    API->>API: bcrypt.compare, then sign JWT (15 min)
    API-->>App: 200 accessToken
    App->>App: Save token in DataStore

    Note over User,DB: List accounts
    App->>API: GET /api/accounts with Bearer token
    API->>API: Verify JWT, read customerNumber
    API->>DB: SELECT accounts WHERE customer_number = customer
    DB-->>API: account rows
    API-->>App: 200 list of accounts
    App-->>User: Show accounts

    Note over User,DB: Account details and nickname
    User->>App: Tap an account
    App->>API: GET /api/accounts/:accountNumber with Bearer token
    API->>DB: SELECT WHERE account_number AND customer_number
    DB-->>API: account row
    API-->>App: 200 account
    User->>App: Edit nickname and save
    App->>API: PATCH /api/accounts/:accountNumber with Bearer token
    API->>API: Verify JWT, validate nickname (max 255)
    API->>DB: UPDATE WHERE account_number AND customer_number
    DB-->>API: updated row
    API-->>App: 200 updated account
    App-->>User: Show new nickname
```

## Error handling

```mermaid
sequenceDiagram
    participant App as Android app
    participant API as Express API

    Note over App,API: Another customer's account
    App->>API: GET /api/accounts/200000004 (belongs to someone else)
    API-->>App: 404 Account not found
    App->>App: Show "This account couldn't be found"

    Note over App,API: Expired or invalid token
    App->>API: Any request with an expired token
    API-->>App: 401 Unauthorized
    App->>App: AuthInterceptor clears the token
    App->>App: Navigation returns to the Login screen
```

## Data model

```mermaid
erDiagram
    CUSTOMERS ||--o{ ACCOUNTS : owns
    CUSTOMERS {
        int customer_number PK
        varchar full_name
        varchar password_hash
    }
    ACCOUNTS {
        char account_number PK
        int customer_number FK
        numeric balance
        varchar nickname
    }
```

## Security decisions

| Concern | How it's handled |
|---|---|
| Passwords | Stored as bcrypt hashes, never in plain text |
| Authentication | JWT signed with a server secret, expires after 15 minutes |
| Access to other customers' accounts | Every query filters by the customer in the token; foreign accounts return 404, so their existence isn't revealed |
| SQL injection | Parameterised queries only |
| Input validation | zod on the API, plus database constraints (9 digits, max 255 characters) |
| Token on the device | Stored in the app's private DataStore; app backup disabled |
| Network | Cleartext HTTP allowed only for the emulator's `10.0.2.2`; production would use HTTPS |
| Logs | Request logging only in debug builds, with the Authorization header redacted |