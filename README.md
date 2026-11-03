# Bank Accounts App

An Android app for bank customers to log in, view their accounts and account
details, and set an account nickname. Built natively in Kotlin with Jetpack
Compose, backed by a TypeScript REST API and PostgreSQL.

## Features
- Log in with customer number and password (JWT authentication)
- View the list of your accounts with balances
- View an account's details (number, balance, nickname)
- Add, change or remove an account's nickname (max 255 characters)
- Log out, and automatic return to login when the session expires
- Input validation in the app and on the API

## Tech stack
| Layer | Technology |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, MVVM, Navigation Compose, Retrofit, OkHttp, kotlinx.serialization, DataStore |
| Backend | Node.js, TypeScript, Express, zod |
| Database | PostgreSQL 17 (Docker) |
| Auth | bcrypt password hashing, JWT bearer tokens (15 minutes) |

## Repository structure
```
app/                 Android application
backend/             REST API
docs/                Architecture and communication diagrams
docker-compose.yml   PostgreSQL database
```

## Getting started

### Prerequisites
- Docker (Docker Desktop or OrbStack)
- Node.js 22+
- Android Studio with an emulator (Android 7.0 / API 24 or newer)

### 1. Start the database
```bash
docker compose up -d
```
PostgreSQL runs on port `5433`, to avoid clashing with a local install on `5432`.

### 2. Run the backend
```bash
cd backend
cp .env.example .env
```
Set `JWT_SECRET` in `.env` to a long random value, for example:
```bash
openssl rand -base64 48
```
Then install, seed the test data and start the API:
```bash
npm install
npm run seed
npm run dev
```
The API runs at `http://localhost:3000` (check with `http://localhost:3000/health`).
`npm run seed` can be re-run at any time to reset the data.

### 3. Run the Android app
1. Open the repository root in Android Studio and wait for the Gradle sync to finish.
2. Select an emulator and press **Run**.
3. Keep the backend running. The emulator reaches it at `http://10.0.2.2:3000`.

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
| PATCH | `/api/accounts/:accountNumber` | Set or remove the nickname (`{ "nickname": "..." }` or `null`) |

| Status | Meaning |
|---|---|
| 400 | Invalid input (e.g. account number format, nickname over 255 characters) |
| 401 | Wrong credentials, or missing / invalid / expired token |
| 404 | Account doesn't exist **or** belongs to another customer |

## Data model
| Field | Type | Constraint |
|---|---|---|
| Customer Number | INTEGER | 9 digits, primary key |
| Account Number | CHAR(9) | 9 digits, unique globally |
| Balance | NUMERIC(15,3) | Exact decimal, 3 places (KWD) |
| Nickname | VARCHAR(255) | Optional |

## Architecture
Diagrams of how the app, API and database communicate:
[docs/architecture.md](docs/architecture.md)

**Android (MVVM)**
```
ui/            Screens (Compose) and ViewModels: login, accounts, accountdetails, navigation
data/          Repositories: the single source of data for the ViewModels
data/remote/   Retrofit API, DTOs, OkHttp interceptors
data/local/    DataStore token storage
```
Screens only render state, ViewModels hold screen state as `StateFlow`,
repositories call the API, and an OkHttp interceptor attaches the token to
every request.

**Backend:** routes → auth middleware → controllers (validation with zod)
→ services (SQL).

## Design decisions
**Security**
- **Accounts are scoped to the logged-in customer:** every query filters by the
  customer in the token. Another customer's account returns 404, so its
  existence isn't revealed.
- **Passwords are hashed with bcrypt;** the database never stores plain text.
- **Parameterised SQL queries** prevent SQL injection.
- **Secrets come from environment variables,** never from code.
- **The token is stored in the app's private DataStore,** and app backup is
  disabled so it can't be copied into cloud backups.
- **Network logging only in debug builds,** with the `Authorization` header redacted.
- **Plain HTTP is allowed only for `10.0.2.2`** (the emulator's address for the
  host machine) through a network security config. Everything else requires HTTPS.

**Data**
- **Balance is `NUMERIC` in the database, not float:** floating point causes
  rounding errors with money. The app only displays it.
- **Empty nicknames are stored as `NULL`,** and nicknames are trimmed on both
  client and server.

**Android**
- **`targetSdk` is 36, not 37:** Android 17 blocks apps targeting API 37 from
  connecting to local network addresses like `10.0.2.2` without an extra
  permission. The production API would be a public HTTPS domain, where this
  doesn't apply.
- **Manual dependency injection** (`AppContainer`) instead of Hilt, to keep a
  small app simple.
- **Expired sessions:** a 401 clears the stored token, and navigation returns
  to the login screen.

## Troubleshooting
| Problem | Fix |
|---|---|
| App shows "Can't reach the server" | Check `npm run dev` is running and `http://localhost:3000/health` responds |
| Backend fails with `JWT_SECRET is not set` | Set `JWT_SECRET` in `backend/.env` |
| Seed fails to connect | Check `docker compose ps` shows the database running |

## Known limitations
- The app starts on the login screen each time it's launched; the saved token
  isn't reused yet.
- Balances are shown with `Double` in the app (display only, no calculations).
- No refresh tokens: users log in again after 15 minutes.


## Use of AI
I used Claude as an assistant during this project:
- **Planning:** breaking the task into steps and choosing the tech stack.
- **Learning:** explanations of Android, Kotlin and Jetpack Compose concepts,
  since this was my first native Android app.
- **Code suggestions:** example code for each step, which I typed, adapted and
  tested myself in Android Studio.
- **Debugging:** help reading errors and logs. It also inspected my project
  files and emulator logs to diagnose issues (for example, finding that
  `targetSdk 37` blocked connections to `10.0.2.2`).
- **Documentation:** drafting this README and the architecture diagrams in
  `docs/architecture.md`, which I reviewed and edited.

All changes to the code and all commits were made by me, and I can walk
through and explain every part of the solution.