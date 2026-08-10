<p align="center">
  <img src="docs/logo/logo-no-background.svg" alt="StepCount Logo" width="220" />
</p>

# StepCount

**An offline-first physical activity tracking platform built with Android (Jetpack Compose & Material 3), FastAPI, PostgreSQL, and Firebase Authentication.**

---

## Overview

StepCount is a modern fitness tracking application designed to monitor daily walking activity in real time. It utilizes device hardware step sensors to capture movement with minimal battery consumption, maintains an offline-first local database via Room, and seamlessly synchronizes historical activity and competitive rankings with a centralized FastAPI backend when network connectivity is available.

---

## Key Features

- **Accurate Step Tracking:** Real-time hardware step counting using Android's `TYPE_STEP_COUNTER` API, with automatic fallback to accelerometer peak detection when dedicated hardware is unavailable.
- **Offline-First Architecture:** Local Room Database acts as the single source of truth, enabling full app functionality, history viewing, and cached leaderboard browsing without internet access.
- **Secure Authentication:** Firebase Authentication (Email/Password) paired with token verification on the FastAPI backend, including an offline Guest Mode for instant local tracking.
- **Historical Analysis & CRUD:** Full management of past daily records with progress metrics (calories, distance, active minutes, and goal completion status).
- **Social Leaderboards:** Dynamic ranking comparisons sorted by total steps or goal achievement percentage, featuring local caching for offline availability.
- **Daily Motivation:** Integrated daily inspirational quotes fetched via the backend service.

---

## Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Android Client** | Kotlin, Jetpack Compose, Material Design 3, Room Database, Retrofit, OkHttp, Coroutines, Flow |
| **Backend API** | Python 3, FastAPI (Async), SQLAlchemy 2.0 / asyncpg, Pydantic v2, Alembic |
| **Database** | PostgreSQL (Primary remote store), SQLite / Room (Local client store) |
| **Authentication** | Firebase Authentication (Client SDK + Firebase Admin SDK) |
| **Testing & Quality** | JUnit, Turbine, MockK, Android Lint, Pytest, Ruff, Mypy |

---

## Project Documentation

Detailed architectural guidelines, UI layouts, and quality standards are maintained in the `docs/` directory:

- [Technical Specifications (docs/docs.md)](docs/docs.md) – Complete architecture, entity models, endpoints, and requirements.
- [UI & Screen Layouts (docs/ui.md)](docs/ui.md) – Material Design 3 specifications, component hierarchy, and screen wireframes.
- [Engineering Leash (docs/leash.md)](docs/leash.md) – Mandatory coding rules, clean architecture patterns, testing thresholds, and pre-commit checks.
- [Material Icons Reference (docs/icons.md)](docs/icons.md) – Mapping of all vector symbols and Google Fonts references.
- [Vector Assets (docs/icons/svg/)](docs/icons/svg/) – Downloaded SVG icon assets for the Android application.
