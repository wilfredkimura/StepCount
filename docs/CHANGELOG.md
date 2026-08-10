# Changelog

All notable changes to the **StepCount** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Clean MVVM Architecture Foundation:** Modular package layers (`core/`, `data/`, `domain/`, `sensor/`, `presentation/`, `worker/`, `di/`).
- **Material Design 3 Theme & Reusable UI:** Athletic blue/green color schemes, typography hierarchy, custom buttons, outlined text fields, metric cards, status badges, and dialogs.
- **Room Database Layer (Offline SSOT):** Local tables for user profile, daily step records with cascade delete, and cached leaderboard standings.
- **Pure Kotlin Domain Layer:** Domain models, repository interfaces, and business use cases with live flow calculations (calories, km distance, active minutes, goal percentage).
- **Remote Networking & Auth:** Retrofit 2 client with OkHttp bearer token interceptor, DTO schemas, and Firebase Authentication service with guest mode support.
- **Hardware Sensor & Motion Tracking:** Lifecycle-aware listener for `Sensor.TYPE_STEP_COUNTER` with midnight baseline reset and 3-axis accelerometer peak detector fallback.
- **7 Jetpack Compose Screens:**
  1. Splash Screen with branding and session verification.
  2. Auth Screen with Login, Register, validation, and Guest Mode.
  3. Dashboard Screen with live animated step progress ring, fitness metrics, and quotes.
  4. History Screen with timeframe filters (All Time, Week, Month) and deletion confirmation.
  5. Leaderboard Screen with 3 podium standings (Gold, Silver, Bronze) and user highlights.
  6. Profile Screen with daily goal editor, preset chips, and lifetime stats.
  7. Settings Screen with dark theme toggle, units switcher, manual sync trigger, and account management.
- **Background Synchronization:** AndroidX WorkManager `StepSyncWorker` with network constraints and automatic retries.
- **Automated Unit Testing & Linting:** 100% test pass rate across DAOs, repositories, and ViewModels with 0 Android Lint errors.

---

## [0.1.0] - 2026-08-10

### Added
- **Technical Specification (`docs/docs.md`):** Complete architectural breakdown covering 7 Android activities, Room database schemas, FastAPI asynchronous endpoints, and Firebase Authentication integration.
- **UI Design System (`docs/ui.md`):** Material Design 3 layout specifications, typography hierarchy, energetic color tokens, and ASCII wireframe sketches with vector icon mappings for all screens.
- **Engineering Leash (`docs/leash.md`):** Strict development constraints, MVVM Clean Architecture boundaries, offline-first Room rules, FastAPI async guidelines, testing thresholds (80%+ coverage), and pre-commit gatekeeper protocols.
- **Material Icons Reference (`docs/icons.md`):** Comprehensive catalog mapping all app components to Google Material Symbols with direct Google Fonts links.
- **Vector Assets (`docs/icons/svg/`):** 49 downloaded Material Symbol SVG icons for Android drawable integration.
- **Project Governance (`.agents/AGENTS.md`):** Permanent workspace rules enforcing engineering leash, testing standards, and version control guidelines.
- **Project Documentation (`README.md`):** Clean, professional overview with brand logo and quick navigation.
