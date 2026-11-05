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
- **Network Permission:** `android.permission.INTERNET` is declared for backend REST communication.
- **Network Security:** `res/xml/network_security_config.xml` denies cleartext traffic by default;
  `src/debug/res/xml/network_security_config.xml` permits cleartext traffic app-wide in debug builds
  for local development. Release builds deny cleartext traffic and require HTTPS.

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

The Android client currently uses `http://192.168.100.13:8081` as its development backend URL. For
a physical device, run the server on a host reachable at that address, bind it to a LAN-reachable
interface (the development YAML uses `0.0.0.0`), and ensure both devices can communicate over the
network. A firewall or Wi-Fi client isolation can still block access. For the standard Android
emulator, change `defaultDemoServiceUrl()` in
`app/shared/src@android/data/ClientStorage.android.kt` to `http://10.0.2.2:8081`. The API uses port
`8081` while the web development server uses port `8080`. These HTTP
endpoints are for debug builds only; production deployments must use HTTPS.
