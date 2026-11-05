# Shared Compose Multiplatform UI (`app/shared`)

This module houses the shared Material 3 UI layer, viewmodels, reactive state management, and
platform-specific bridges across Android, iOS, and Web (Wasm/JS).

---

## Environment Configurations

### 1. Build & Module Configuration

- **Module Path:** `app/shared`
- **Product Type:** `kmp/lib`
- **Target Platforms:** `android`, `iosArm64`, `iosSimulatorArm64`, `wasmJs`
- **Serialization Format:** `json` (via `kotlinx.serialization`)

### 2. Compose Resources Configuration (`module.yaml`)

- **Package Name:** `co.ke.xently.expense.tracker.resources`
- **Exposed Accessors:** `true` (enables multiplatform resource access via `Res.drawable.*`)

### 3. Theme & UI Styling (`src/theme/Theme.kt`)

- **Material 3 Theming:** Sugarcane agricultural color palette with dedicated dynamic Light
  (`SugarcaneLightColorScheme`) and Dark (`SugarcaneDarkColorScheme`) schemes.
- **Adaptive Layout:** Responsive navigation bar switching cleanly across mobile and widescreen
  desktop/web viewports.

### 4. Reactive State Management & ViewModels

- **`ProjectWorkspaceViewModel`:** Centralized project-scoped state and event processing using
  `StateFlow`; requests use the server's shared API and project membership for authorization.
- **Demo connection:** The UI has no per-user service URL or token fields. Android's
  `defaultDemoServiceUrl()` currently uses `http://192.168.100.13:8081` for a physical device; to
  use the Android Emulator, change it to `http://10.0.2.2:8081` in
  `src@android/data/ClientStorage.android.kt`. iOS Simulator uses `http://127.0.0.1:8081`, and Web
  uses `http://localhost:8081`. These development endpoints match the server's local
  `application-dev.yaml`; the web development server uses port `8080`, and Android permits HTTP
  only in debug builds.
- **Screens:**
    - `DashboardScreen`: Project expense totals, category breakdowns and partner balances.
    - `ExpensesScreen`: Project-scoped expense list and logging dialog.
    - `PartnerLedgersScreen`: Member balances and tranche recording.
    - `RevenueSplitScreen`: End-of-season settlement calculator.
    - `InvitationsScreen` and `ProjectsScreen`: Project creation, selection and invite responses.
- **Previews:** Shared preview functions are colocated with `ProjectApp` and each screen, with
  light/dark, font scale, dynamic color and screen-size variants where applicable. Preview-only
  sample models are in `ui/PreviewFixtures.kt`.

---

## Testing

```bash
# Run unit tests for shared ViewModels and UI state logic
./kotlin test -m app/shared
```

Kover is not available in the current Amper build; it is a Gradle plugin, and no coverage task is
provided until the project adopts a Kover-compatible Gradle build.
