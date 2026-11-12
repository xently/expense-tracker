# Sugarcane Expense Tracker Server

The backend server is built with Kotlin and the Ktor framework. Its default API is project-scoped
and authenticated; the former global finance APIs and SSE stream are available only in explicitly
enabled compatibility mode.

---

## Environment Configurations

### 1. Server Host, Port & YAML Configuration

- Ktor `EngineMain` loads `server/resources/application.yaml` by default. It sets host `0.0.0.0`,
  API port `8081` (the web development server uses `8080`), and the `mainModule` entry point.
- **Default configuration:** `./kotlin run -m server` uses `application.yaml`, leaves demo mode
  disabled, and uses Firebase token verification.
- **Local UI demo:** `./kotlin run -m server -- -config=application-dev.yaml` loads
  `server/resources/application-dev.yaml`, which explicitly enables the in-memory demo seed and
  fixed demo token. The Amper resource path is the classpath name `application-dev.yaml`; the older
  `src/main/resources/application-dev.yaml` argument is accepted only when no file/resource exists
  at that path and the Amper classpath resource is available.
- **Configuration keys:** `app.demoMode`, `app.projectRepository`,
  `app.enableLegacyFinanceRoutes`, and `app.firebaseProjectId` are in the YAML file. For a durable
  service, set `app.projectRepository: firestore` and set `app.firebaseProjectId` in the
  deployment's YAML configuration; provide Google Application Default Credentials through the
  runtime identity or `GOOGLE_APPLICATION_CREDENTIALS`.

### 2. Database Persistence

The legacy finance repositories use a pluggable Repository Pattern. The default YAML selects
in-memory project storage. The production deployment YAML can select Firestore for durable
project-scoped storage. Firestore initialization errors are fatal and never fall back to memory.

- **Project API:** `InMemoryProjectRepository` stores user profiles, projects, memberships,
  invitations, expenses, and tranches in memory. `FirestoreProjectRepository` stores the same data
  in Firestore, with transactional username uniqueness, project creation, invitation and membership
  updates, and project-scoped finance writes.
- **Firestore configuration:** Set `app.projectRepository: firestore` and `app.firebaseProjectId` in
  YAML, configure Application Default Credentials (for example with
  `GOOGLE_APPLICATION_CREDENTIALS`), and optionally point `FIRESTORE_EMULATOR_HOST` at a local
  emulator. The server requires credentials/configuration to initialize and does not silently
  switch to in-memory storage.
- **Legacy finance repositories:** `InMemoryRepositoryProvider` contains the former seeded
  Harrison/Herman/Vin dataset and its separate legacy Firestore adapter; `app.projectRepository`
  controls the project-scoped API described above.
- **Credentials:** `GOOGLE_APPLICATION_CREDENTIALS` may point to a Firebase service account key
  JSON file (for example `/etc/secrets/firebase-adminsdk.json`); use the platform's ADC identity
  instead where available.

### 3. Authentication & Authorization

Project endpoints require a valid `Authorization: Bearer <token>` header. The authenticated subject
is obtained only from the verifier; user IDs and project membership are not accepted from request
bodies.

- **Default:** `FirebaseAuthTokenVerifier` verifies Firebase ID tokens. If Firebase Admin Auth is
  unavailable, requests fail closed as unauthorized.
- **OIDC:** `OidcAuthTokenVerifier` is an injection point for a real issuer/JWKS verifier; its
  default callback rejects tokens. Do not treat unverified JWT-shaped strings as credentials.
- **Demo mode:** `application-dev.yaml` enables `DevelopmentAuthTokenVerifier` for the public fixed
  token `local-demo-token`, and seeds Vin's account, two projects, partners, expenses, a tranche and
  an incoming invitation. It is intentionally available only with in-memory storage. Never deploy
  this configuration: the demo token is not a secret or real authentication credential.
- **Tests:** Pass a test verifier explicitly to `Application.module`; default `application.yaml`
  keeps the development verifier disabled.

### 4. FCM Notifications & Scheduled Jobs

- **Legacy overdue check:** The old seeded-finance scheduler is started only when the legacy
  compatibility switch is enabled. Project-scoped overdue scheduling and push delivery are not yet
  implemented.
- **Push Provider:** `FcmPushNotificationService` delivers push notifications to registered partner
  devices via Firebase Cloud Messaging for the legacy finance flow.

### Legacy compatibility warning

Set `app.enableLegacyFinanceRoutes: true` in YAML only for isolated development or compatibility
tests. It exposes the old global finance routes and SSE stream, which do not apply
project-membership authorization. Keep this disabled in normal deployments; it is disabled by
default.

---

## Swagger / OpenAPI API Documentation

The server exposes built-in OpenAPI 3.0 specifications and interactive Swagger UI:

- **Interactive Swagger UI:** `http://localhost:8081/api/v1/swagger`
- **OpenAPI 3.0 JSON Spec:** `http://localhost:8081/api/v1/openapi.json`

The same API endpoints are used by the demo client; it connects with its platform-local endpoint
and the development-only demo token, so the user does not configure connection details.

### Project API (default)

Project endpoints require a verified bearer token. A user must register a unique username before
creating projects. The creator is the owner and the first partner; only the owner can invite an
already registered username. Invitations are available as in-app messages through `GET
/api/v1/me/invitations`. Accepting an invitation adds the user to the project. `GET
/api/v1/me/projects` returns `defaultProjectId` only when the user has exactly one project.

| Method         | Endpoint                                            | Description                                                                   |
|----------------|-----------------------------------------------------|-------------------------------------------------------------------------------|
| `POST` / `GET` | `/api/v1/me/profile`                                | Register or view the authenticated user profile                               |
| `GET`          | `/api/v1/me/projects`                               | List only the caller's projects; select automatically if there is exactly one |
| `GET`          | `/api/v1/me/invitations`                            | List invitations addressed to the caller                                      |
| `POST`         | `/api/v1/projects`                                  | Create an arbitrary project; caller becomes owner and partner                 |
| `GET`          | `/api/v1/projects/{projectId}`                      | Read a project only if the caller is a member                                 |
| `GET`          | `/api/v1/projects/{projectId}/members`              | List partners only for a project the caller belongs to                        |
| `POST`         | `/api/v1/projects/{projectId}/invitations`          | Owner invites a registered user by username                                   |
| `POST`         | `/api/v1/invitations/{invitationId}/respond`        | Accept or decline the caller's invitation                                     |
| `GET` / `POST` | `/api/v1/projects/{projectId}/expenses`             | Read or add project-scoped expenses and member splits                         |
| `DELETE`       | `/api/v1/projects/{projectId}/expenses/{expenseId}` | Delete an expense within an accessible project                                |
| `GET` / `POST` | `/api/v1/projects/{projectId}/tranches`             | Read project tranches or record the authenticated partner's payment           |
| `GET`          | `/api/v1/projects/{projectId}/dashboard`            | Calculate project-only ledger and dashboard totals                            |
| `POST`         | `/api/v1/projects/{projectId}/revenue-split`        | Calculate the project's payout split                                          |

Projects may include an `expenseCategories` array when created. Category names are trimmed and
must be nonblank and unique without regard to case. Expenses in a project with configured
categories must include a matching `categoryName`; the server stores the configured spelling and
rejects names from other projects. Omit the array (or send an empty array) to keep using legacy
`ExpenseCategory` values. Historical expenses without `categoryName` remain valid, and dashboard
category totals are exposed by name in `categoryBreakdownsByName`.

```json
{
  "name": "Community orchard",
  "description": "Shared growing project",
  "expenseCategories": ["Soil testing", "Seedlings", "Tools"]
}
```

### Legacy Endpoints Overview

The following legacy routes are not registered unless `app.enableLegacyFinanceRoutes: true` is set
in YAML and do
not have project-level authorization:

| Method   | Endpoint                                  | Description                                                        |
|----------|-------------------------------------------|--------------------------------------------------------------------|
| `GET`    | `/api/v1/dashboard?asOfDate={YYYY-MM-DD}` | Aggregated financial metrics, category breakdown & partner ledgers |
| `GET`    | `/api/v1/expenses`                        | List all season expenses                                           |
| `POST`   | `/api/v1/expenses`                        | Create a new expense and auto-generate 1/3 equal partner splits    |
| `GET`    | `/api/v1/expenses/{id}`                   | Get expense details by ID                                          |
| `DELETE` | `/api/v1/expenses/{id}`                   | Delete an expense by ID                                            |
| `GET`    | `/api/v1/tranches?partnerId={id}`         | List tranche payments (optionally filtered by partner)             |
| `POST`   | `/api/v1/tranches`                        | Record a cash/M-Pesa tranche payment installment                   |
| `GET`    | `/api/v1/partners`                        | List all registered partners                                       |
| `GET`    | `/api/v1/partners/{id}/ledger`            | Get individual partner ledger summary and compliance status        |
| `POST`   | `/api/v1/revenue-split`                   | Compute end-of-season pro-rata revenue split and payout settlement |
| `GET`    | `/api/v1/notifications?partnerId={id}`    | List dispatched notifications and reminders                        |
| `POST`   | `/api/v1/notifications/trigger-check`     | Manually trigger overdue balance check and FCM push notifications  |
| `GET`    | `/api/v1/events/stream`                   | Server-Sent Events (SSE) real-time stream (`text/event-stream`)    |

### Example Payloads

#### 1. Record Expense (`POST /api/v1/expenses`)

```json
{
  "title": "DAP Fertilizer 50kg bags (x10)",
  "category": "FERTILIZER",
  "totalAmount": 60000.0,
  "currency": "KES",
  "transactionDate": "2026-05-15",
  "payingPartnerId": "HARRISON",
  "notes": "Purchased from AgriVet Supplies"
}
```

#### 2. Record Tranche (`POST /api/v1/tranches`)

```json
{
  "partnerId": "HERMAN",
  "amount": 20000.0,
  "paymentDate": "2026-06-05",
  "notes": "M-Pesa payment for June obligation",
  "referenceNumber": "QKH8912KLM"
}
```

#### 3. End-of-Season Revenue Split (`POST /api/v1/revenue-split`)

```json
{
  "grossFactoryPayout": 500000.0,
  "harvestOperationsExpenses": 80000.0
}
```
