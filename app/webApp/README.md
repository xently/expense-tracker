# Web (Wasm/JS) Application Environment

This module compiles the Compose Multiplatform UI into WebAssembly (Kotlin/Wasm) for modern web
browsers.

---

## Environment Configurations

### 1. Build & Module Configuration

- **Module Path:** `app/webApp`
- **Product Type:** `wasmJs/app`
- **Entry Point:** `src/main.kt` initializes `ComposeViewport("ComposeTarget") { App() }`.

### 2. HTML Canvas Host (`resources/index.html`)

- Hosts the full-screen `<canvas id="ComposeTarget">` element.
- Loads the WebAssembly script loader `webApp.uninstantiated.mjs`.

### 3. Styling & Layout (`resources/styles.css`)

- Enforces zero margin and 100% viewport dimensions
  (`width: 100vw; height: 100vh; overflow: hidden;`).

### 4. Browser Compatibility Requirements

- Requires browsers supporting **WebAssembly Garbage Collection (Wasm-GC)**:
    - Google Chrome 119+
    - Mozilla Firefox 120+
    - Apple Safari 18.2+
    - Microsoft Edge 119+

---

## Running the Web App

```bash
# Start local development web server with live reloading
./kotlin run -m webApp
```

The web development server uses port `8080`; its demo API is served separately by the backend on
port `8081`.
