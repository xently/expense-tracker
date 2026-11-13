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

The Android client defaults to `http://10.0.2.2:8081`, which reaches the host machine from the
standard Android emulator. To configure a physical device or another local backend without changing
tracked source, copy `app/androidApp/local-demo-service.xml.example` to
`app/androidApp/res/values/local_demo_service.xml` and replace `YOUR_COMPUTER_LAN_IP` with the
server host's reachable address. This local resource is ignored by Git and is packaged by both CLI
and IDE builds. Run the server on a LAN-reachable interface (the development YAML uses `0.0.0.0`);
firewalls or Wi-Fi client isolation can still block access. The API uses port `8081` while the web
development server uses port `8080`. These HTTP endpoints are for debug builds only; production
deployments must use HTTPS.

After creating or editing the local resource, clean the build before launching so a cached APK does
not keep using the previous value:

```bash
./kotlin clean
./kotlin run -m androidApp
```
