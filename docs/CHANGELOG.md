# Changelog

All notable changes to the **StepCount** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **FastAPI Asynchronous Backend Architecture:** Modular clean backend package layer (`app/api/v1`, `app/core`, `app/db`, `app/models`, `app/schemas`, `app/services`).
- **NeonDB PostgreSQL Database Layer:** Asynchronous engine with `asyncpg`, connection pooling, serverless SSL mode, and SQLAlchemy 2.0 ORM models (`User` and `DailySteps` with foreign key cascade and unique date constraints).
- **Live Firebase Admin SDK Authentication:** Production-ready token verification via `get_current_user` dependency, auto-provisioning, and secure `serviceAccountKey.json` / environment variable configuration.
- **5 Core REST API Resource Sets:**
  1. `POST /api/auth/firebase-login` and `POST /api/auth/logout`.
  2. `POST /api/steps` (idempotent upsert), `GET /api/steps/today`, `GET /api/steps/history`, `PUT /api/steps/{date}`, `DELETE /api/steps/{date}`.
  3. `GET /api/leaderboard` (filtered by `today`, `week`, `all_time` and ranked by `steps` or `goal` percentage) and `GET /api/leaderboard/me`.
  4. `GET /api/profile` and `PUT /api/profile` (updating display name and daily target).
  5. `GET /api/motivation` (Quotable API client with resilient curated local fallback).
- **Render Free Tier Deployment Guide:** Clean documentation in [backend/README.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/backend/README.md) for 1-click cloud deployment without Docker.
- **Automated Backend Pytest Suite:** 100% test pass rate with **87% overall test coverage** across all routes and services.

---

## [1.0.0] - 2026-08-11

### Added
- **Official Brand Logo Integration:** Converted `docs/logo/logo.svg` into scalable Android Vector Drawables (`app_logo.xml`, `ic_launcher_foreground.xml`, `ic_launcher_background.xml`) and multi-density mipmaps (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`), cleanly displayed on the Splash Screen, Auth Screen, and Android App Drawer.
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

### Fixed
- **Dashboard String Format Crash:** Resolved `UnknownFormatConversionException` on the Dashboard screen caused by an unescaped `%` character in the daily goal and percentage progress text.
- **Firebase Initialization Crash:** Added defensive initialization and fallback in `FirebaseAuthService` preventing startup crashes when `google-services.json` is not yet configured, allowing seamless offline and Guest Mode operation.

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
