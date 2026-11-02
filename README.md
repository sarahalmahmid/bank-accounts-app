# Bank Accounts App

An Android app for bank customers to log in, view their accounts and account
details, and set an account nickname. Built natively in Kotlin with a
TypeScript + PostgreSQL backend.

## Features
- Login with customer number and password (JWT authentication)
- List of the customer's accounts
- Account details (number, balance, nickname)
- Set or change an account's nickname

## Tech stack
| Layer | Technology |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, MVVM, Retrofit, DataStore |
| Backend | Node.js, TypeScript, Express, zod |
| Database | PostgreSQL 17 (Docker) |
| Auth | bcrypt password hashing, JWT bearer tokens |

## Repository structure
```
app/                 Android application
backend/             REST API
docs/                Architecture diagrams
docker-compose.yml   PostgreSQL database
```

## Getting started

### Prerequisites
- Docker (Docker Desktop or OrbStack)
- Node.js 22+
- Android Studio (with an emulator, API 26+)

### 1. Start the database
```bash
docker compose up -d
```

### 2. Run the backend
```bash
cd backend
cp .env.example .env
npm install
npm run seed
npm run dev
```
The API runs at `http://localhost:3000`.

### 3. Run the Android app
Open the project root in Android Studio, select an emulator and press Run.
The emulator reaches the backend at `http://10.0.2.2:3000`.

## Test accounts
| Customer number | Password | Accounts |
|---|---|---|
| 100000001 | Password123! | 200000001, 200000002, 200000003 |
| 100000002 | Password123! | 200000004, 200000005 |

## API
All `/api/accounts` endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Log in, returns an access token |
| GET | `/api/accounts` | List the logged-in customer's accounts |
| GET | `/api/accounts/:accountNumber` | Get one account's details |
| PATCH | `/api/accounts/:accountNumber` | Update the account's nickname |

## Data model
| Field | Type | Constraint |
|---|---|---|
| Customer Number | INTEGER | 9 digits, primary key |
| Account Number | CHAR(9) | 9 digits, unique globally |
| Balance | NUMERIC(15,3) | Exact decimal, 3 places (KWD) |
| Nickname | VARCHAR(255) | Optional |

## Architecture
See [docs/architecture.md](docs/architecture.md).

## Design decisions
- **Balance is `NUMERIC`, not float:** floating point causes rounding errors with money.
- **Accounts are scoped to the logged-in customer:** requesting another customer's
  account returns 404, so account existence isn't revealed.
- **Passwords are hashed with bcrypt;** the database never stores plain text.
- **Parameterised SQL queries** prevent SQL injection.
- **Secrets come from environment variables,** never from code.

## What I'd add next
- Refresh tokens and token revocation
- Biometric login
- HTTPS with certificate pinning
- Automated tests and CI
- Offline caching with Room