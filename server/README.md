# Sugarcane Expense Tracker Server

The backend server is built with Kotlin and the Ktor framework. It provides REST endpoints and
Server-Sent Events (SSE) for expense management, partner tranches, overdue alerts, and revenue
settlements.

---

## Environment Configurations

### 1. Server Host & Port

- **Default Port:** `8080` (Configurable via `PORT` or `SERVER_PORT` environment variables)
- **Host Binding:** `0.0.0.0`
- **Execution Command:** `./kotlin run -m server`

### 2. Database Persistence

The server uses a pluggable Repository Pattern:

- **In-Memory Storage (Default / Development):** `InMemoryRepositoryProvider` provides fast,
  self-contained persistence initialized with seeded partner records (Harrison, Herman, Vin) and
  sample transactions.
- **Firebase Firestore (Production):** `FirestoreRepositoryProvider` integrates with Google Cloud
  Firestore via the Firebase Admin Java SDK.
- **Environment Variables for Firestore:**
    - `GOOGLE_APPLICATION_CREDENTIALS`: Path to your Firebase service account private key JSON file
      (e.g. `/etc/secrets/firebase-adminsdk.json`).
    - `FIREBASE_PROJECT_ID`: Google Cloud / Firebase project ID.

### 3. Authentication & Authorization

Pluggable token verification layer:

- **Firebase Authentication:** `FirebaseAuthTokenVerifier` verifies Firebase ID JWT tokens passed in
  the `Authorization: Bearer <token>` header.
- **Generic OIDC / OAuth2:** `OidcAuthTokenVerifier` supports Keycloak, Okta, or custom OpenID
  Connect providers:
    - `OIDC_ISSUER`: Issuer URL (e.g., `https://auth.farm.co.ke/realms/farm`)
    - `OIDC_CLIENT_ID`: OAuth2 Client ID (e.g., `expense-tracker-app`)
- **Development Mode:** `DevelopmentAuthTokenVerifier` recognizes dev partner tokens (e.g.
  `bearer harrison`, `bearer herman`, `bearer vin`, `dev-token`).

### 4. FCM Notifications & Scheduled Jobs

- **Overdue Check:** Runs automatically on the 10th day of every month to flag unpaid shares from
  the previous month.
- **Push Provider:** `FcmPushNotificationService` delivers push notifications to registered partner
  devices via Firebase Cloud Messaging.

---

## Swagger / OpenAPI API Documentation

The server exposes built-in OpenAPI 3.0 specifications and interactive Swagger UI:

- **Interactive Swagger UI:** `http://localhost:8080/api/v1/swagger`
- **OpenAPI 3.0 JSON Spec:** `http://localhost:8080/api/v1/openapi.json`

### Endpoints Overview

| Method   | Endpoint                                  | Description                                                        |
|----------|-------------------------------------------|--------------------------------------------------------------------|
| `GET`    | `/`                                       | Service name and version status                                    |
| `GET`    | `/api/v1/health`                          | Health check endpoint                                              |
| `GET`    | `/api/v1/openapi.json`                    | OpenAPI 3.0 specification in JSON format                           |
| `GET`    | `/api/v1/swagger`                         | Interactive Swagger UI HTML documentation                          |
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
