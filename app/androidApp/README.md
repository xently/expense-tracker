# Android Application Environment

This module contains the Android client application for the Sugarcane Expense Tracker, packaging the
shared Compose Multiplatform UI into an Android APK / AAB.

---

## Environment Configurations

### 1. Build & Module Configuration

- **Module Path:** `app/androidApp`
- **Product Type:** `android/app`
- **Main Activity:** `co.ke.xently.expense.tracker.android.MainActivity`
- **Dependency:** Links to `app/shared` (Compose Multiplatform UI) and Android Activity Compose.

### 2. Android Manifest (`src/AndroidManifest.xml`)

- **Package Name:** `co.ke.xently.expense.tracker.android`
- **Theme:** `@android:style/Theme.Material.NoActionBar`
- **Network Permissions:** Internet permission is inherited for backend REST communication and FCM
  notifications.

### 3. ProGuard & R8 Optimization (`proguard-rules.pro`)

- Retains Compose Multiplatform runtime annotations.
- Preserves `kotlinx.serialization` models and reflection-free serializers.

### 4. Firebase Cloud Messaging (FCM) Configuration

- Place your `google-services.json` configuration file in this directory
  (`app/androidApp/google-services.json`) for push notifications.

---

## Running the Android App

```bash
# Launch on a connected Android device or running emulator
./kotlin run -m androidApp
```
