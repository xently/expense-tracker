# Sugarcane Farming & Planting Season Expense Tracker

A full-stack Kotlin Multiplatform solution for managing sugarcane planting season expenses, tracking
partner cash tranche contributions, calculating end-of-season factory payout revenue splits among
three partners (**Harrison**, **Herman**, and **Vin**), and dispatching automated overdue
notifications.

---

## Architecture & Technology Stack

- **Compose Multiplatform (CMP) Client UI:** Shared Material 3 UI supporting Android, iOS, and Web
  (Kotlin/Wasm).
- **Kotlin / Ktor Backend Server:** High-performance asynchronous backend providing REST endpoints,
  Server-Sent Events (SSE), and pluggable persistence (In-Memory and Firebase Firestore).
- **Core Domain & Calculators:** Shared multiplatform business logic covering equal 1/3 splits,
  monthly due dates (`EOMONTH(date) + 10 days`), partner ledger balances, and pro-rata factory
  payout revenue distribution.
- **Push Notifications & Background Jobs:** Automated overdue checks past the 10th of the month with
  Firebase Cloud Messaging (FCM) dispatch.

---

## Environment Documentation & Configurations

Each environment in this repository contains a dedicated `README.md` describing only its
environment-specific configurations:

| Module / Environment  | Path                                                     | Description & Configuration Scope                                                                                    |
|-----------------------|----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| **Backend Server**    | [`server/README.md`](./server/README.md)                 | Ktor server port/host, Firestore/In-Memory persistence, Auth/OIDC, FCM scheduler, and Swagger/OpenAPI documentation. |
| **Android App**       | [`app/androidApp/README.md`](./app/androidApp/README.md) | Android application manifest, ProGuard/R8 rules, APK/AAB packaging, and `google-services.json` integration.          |
| **iOS App**           | [`app/iosApp/README.md`](./app/iosApp/README.md)         | iOS Xcode project, `Info.plist`, `ComposeUIViewController` wrapper, and `GoogleService-Info.plist` integration.      |
| **Web App (Wasm/JS)** | [`app/webApp/README.md`](./app/webApp/README.md)         | Kotlin/Wasm compiler settings, Wasm-GC browser requirements, HTML canvas container, and dev server.                  |
| **Shared UI Layer**   | [`app/shared/README.md`](./app/shared/README.md)         | Compose Multiplatform UI components, Material 3 Light/Dark sugarcane themes, and reactive ViewModels.                |
| **Core Domain**       | [`core/README.md`](./core/README.md)                     | Shared business logic calculators, multiplatform models, serialization, and date utilities.                          |

---

## Swagger & OpenAPI API Documentation

The Ktor backend provides interactive Swagger documentation and standard OpenAPI 3.0 specifications:

- **Interactive Swagger UI:** `http://localhost:8081/api/v1/swagger`
- **OpenAPI 3.0 Specification:** `http://localhost:8081/api/v1/openapi.json`
- **Detailed API Documentation:** Available in [
  `server/README.md`](./server/README.md#swagger--openapi-api-documentation).

### Primary REST & SSE Endpoints

- `GET /api/v1/health` - Health check status.
- `GET /api/v1/dashboard?asOfDate=YYYY-MM-DD` - Aggregated financial metrics, partner balances, and
  category breakdowns.
- `GET /api/v1/expenses` & `POST /api/v1/expenses` - List and log season expenses with auto-computed
  1/3 splits.
- `GET /api/v1/tranches` & `POST /api/v1/tranches` - View and record installment payments.
- `GET /api/v1/partners/{id}/ledger` - Partner ledger balance and compliance status (`Paid in Full`,
  `Partial`, `Overdue`).
- `POST /api/v1/revenue-split` - End-of-season pro-rata factory payout calculator.
- `POST /api/v1/notifications/trigger-check` - Overdue payment inspection & FCM alert trigger.
- `GET /api/v1/events/stream` - Server-Sent Events (SSE) stream for real-time ledger updates.

---

## Quickstart & Running

The `kotlin` (macOS/Linux) and `kotlin.bat` (Windows) scripts in the project root are
self-bootstrapping wrappers for the Kotlin Toolchain:

```bash
# Run the Ktor Backend Server (Port 8081; Web App uses 8080)
./kotlin run -m server -- -config=application-dev.yaml

# Run the Web App (Kotlin/Wasm)
./kotlin run -m webApp

# Run the Android App (Device/Emulator)
./kotlin run -m androidApp

# Run the iOS App (macOS & Xcode required)
./kotlin run -m iosApp
```

The web development server uses port `8080`; the API server uses port `8081`. The Android, iOS
Simulator, and Web clients use their platform's local development address and open the same seeded
demo workspace; users do not enter a service URL or bearer token. For a
physical device or a deployed shared backend, configure the service endpoint once in the relevant
platform build configuration rather than asking each user to supply it. The public demo token is
development-only and must never be enabled in a production deployment.

---

## Testing

```bash
# Run all project test suites
./kotlin test

# Run tests for a specific module
./kotlin test -m core
./kotlin test -m server
./kotlin test -m app/shared
```

The project is built with Kotlin Amper (`project.yaml` and module `module.yaml` files), while Kover
is currently distributed as a Gradle plugin. There is no Kover-compatible Amper integration, so a
coverage report cannot be configured without migrating or adding a separate Gradle build. Also,
Kover currently measures JVM/Android host tests only; it does not collect coverage from iOS or
Wasm tests.
