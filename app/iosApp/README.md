# iOS Application Environment

This module contains the iOS native shell and Xcode project wrapper for the Sugarcane Expense
Tracker, presenting the shared Compose Multiplatform UI through `ComposeUIViewController`.

---

## Environment Configurations

### 1. Build & Module Configuration

- **Module Path:** `app/iosApp`
- **Product Type:** `ios/app`
- **Xcode Project:** `module.xcodeproj` (Scheme: `iosApp`)
- **Main Swift Entry Point:** `src/iosApp.swift` (`@main struct iOSApp: App`)
- **Kotlin UI Controller:** `src/ViewController.kt` exports `MainViewController()` wrapping
  `ComposeUIViewController { App() }`.

### 2. iOS Target & System Configuration (`src/Info.plist`)

- **Bundle Identifier:** `co.ke.xently.expense.tracker.iosApp`
- **Executable Name:** `$(EXECUTABLE_NAME)`
- **Device Capabilities:** `arm64` architecture requirement.
- **Orientation Support:** Portrait and Landscape modes (`UIInterfaceOrientationPortrait`,
  `UIInterfaceOrientationLandscapeLeft`, etc.).

### 3. Assets & Icons

- App icons and assets are managed via the standard asset catalog in
  `src/Assets.xcassets/AppIcon.appiconset`.

### 4. Firebase Cloud Messaging (FCM / APNs) Configuration

- For iOS push notifications, place your `GoogleService-Info.plist` file in `app/iosApp/src/`.

---

## Running the iOS App

```bash
# Launch on an iOS Simulator or connected physical device (requires macOS with Xcode)
./kotlin run -m iosApp
```
