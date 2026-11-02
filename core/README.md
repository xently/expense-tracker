# Core Domain & Calculations (`core`)

This module contains the domain models, business logic calculators, and shared utilities consumed by
both the Compose Multiplatform client apps and the Ktor backend server.

---

## Environment Configurations

### 1. Build & Module Configuration

- **Module Path:** `core`
- **Product Type:** `kmp/lib`
- **Target Platforms:** `jvm`, `android`, `iosArm64`, `iosSimulatorArm64`, `wasmJs`
- **Serialization:** `kotlinx.serialization` (JSON format enabled)
- **Time/Date Library:** `kotlinx-datetime`

### 2. Core Business Logic & Domain Calculators

- **`ExpenseCalculator`:**
    - Automatically splits expense costs equally (1/3 each) across Harrison, Herman, and Vin.
    - Due date calculation rule: 10th day of the month following the transaction date
      (`EOMONTH(date) + 10 days`).
- **`LedgerCalculator`:**
    - Evaluates individual partner obligations vs. contributions (direct out-of-pocket + cash
      tranches).
    - Assigns dynamic compliance status badges: `Paid in Full`, `Partial`, or `Overdue`.
- **`RevenueSplitCalculator`:**
    - Computes historical investment share percentage:
      `(Partner Actual Investment / Total Project Investment) * 100%`.
    - Distributes Net Factory Payout (`Gross Factory Payout - Harvest Operations Expenses`) pro-rata
      according to investment shares.
    - Accounts for out-of-pocket expense reimbursement, net profit, and return on investment (ROI).

---

## Testing

```bash
# Run unit tests for all domain calculators and business logic
./kotlin test -m core
```
