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

- **`ExpenseTrackerViewModel`:** Centralized state management using `StateFlow<DashboardUiState>`
  with live calculation of expense splits, ledger balances, and notifications.
- **Screens:**
    - `DashboardScreen`: Financial KPI cards, category expense progress bars, and recent activity.
    - `ExpensesScreen`: Expense logging dialog with live 1/3 split preview and category selectors.
    - `PartnerLedgersScreen`: Partner compliance status badges and cash tranche recording dialog.
    - `RevenueSplitScreen`: End-of-season factory payout settlement simulator.
    - `NotificationsScreen`: Push notification logs and overdue alert triggers.

---

## Testing

```bash
# Run unit tests for shared ViewModels and UI state logic
./kotlin test -m app/shared
```
