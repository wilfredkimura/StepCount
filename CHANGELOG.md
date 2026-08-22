# Changelog

All notable changes to the **StepCount** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Explicit User Registration Endpoint (`POST /api/v1/auth/register`):** FastAPI backend endpoint that creates and commits user profiles to NeonDB PostgreSQL upon registration, returning HTTP 201 Created with verified user profile data.
- **Guaranteed Registration Provisioning on Android:** Updated `AuthRepositoryImpl.register()` to explicitly call `POST /api/v1/auth/register` and await PostgreSQL database confirmation before completing registration, preventing orphaned Firebase accounts.
- **Lightweight Firebase-Only Login on Android:** Optimized `AuthRepositoryImpl.login()` to authenticate directly with Firebase Auth without blocking on remote database requests, ensuring instant logins even on cold backend instances.
- **Vercel Keep-Alive Cron Service (`vercel-cron/`):** Lightweight serverless cron project running every 10 minutes to keep the FastAPI backend on Render awake 24/7.

---

## [1.3.2] - 2026-08-22

### Added
- **Anomaly & Glitch Protection Filter (`StepDeltaTracker`):** Added velocity and plausibility validation using hardware reading timestamp tracking (`KEY_LAST_HARDWARE_TIMESTAMP`) to discard impossible step spikes (>15,000 steps jump within short windows or abnormal >12 steps/sec cadence) and safely re-baseline without corrupting daily history.
- **Dedicated Application Update Handler (`BootAndShutdownReceiver`):** Added `handleAppUpdated` to capture steps walked during APK update downtime, restart 24/7 background tracking services, and refresh the Home Screen widget while keeping the cumulative baseline intact.
- **Comprehensive Unit Tests (`StepDeltaTrackerTest`):** Added test coverage for app update baseline preservation, anomaly rejection, and concurrent reading synchronization.

### Fixed
- **Phantom Steps on App Updates / Package Replacement:** Resolved critical bug in `BootAndShutdownReceiver` where installing an updated version triggered `ACTION_MY_PACKAGE_REPLACED` and wiped the hardware counter baseline to `0L`, generating thousands of made-up steps.
- **Step Double-Counting from Concurrent Listeners:** Resolved race conditions between the background foreground service, UI listeners, and periodic workers by adding coroutine `Mutex` serialization in `StepDeltaTracker` and removing redundant database writes from `DashboardViewModel`.

### Changed
- **Version Increment:** Bumped Android application version to `1.3.2` (`versionCode = 8`).

---

## [1.3.1] - 2026-08-20

### Added
- **24/7 Persistent Background Step Tracking (`StepForegroundService`):** Background Foreground Service running with `START_STICKY` and `FOREGROUND_SERVICE_TYPE_HEALTH` on Android 14+ that keeps hardware sensors continuously active in the background with a low-priority, silent live step counter notification in the status bar.
- **Automated Daily Midnight Rollover Engine (`MidnightStepRolloverReceiver`):** Exact midnight alarm (`AlarmManager.setExactAndAllowWhileIdle`) firing daily at `00:00:00` to finalize each day's step tally into Room database and reset today's baseline to 0, ensuring that users who only open the app once a week have full, accurate day-by-day history recorded.
- **Dedicated 24x24dp Monochrome Notification Asset (`ic_notification.xml`):** Pure white vector drawable designed for system notifications and status bar icons.
- **Background Tracking & Battery Optimization Controls in Settings:** Toggle for 24/7 background tracking and shortcut button guiding users to exempt StepCount from aggressive OEM battery savers (Samsung, Xiaomi, etc.).

### Fixed
- **Notification Crash on Test Notification Dispatch:** Resolved app crash caused by passing multi-layered adaptive icon drawables to `NotificationCompat.Builder.setSmallIcon()` by replacing it with a compliant 24dp monochrome icon and adding global exception safety.
- **Unattended Multi-Day Step Loss:** Resolved issue where steps walked over multiple days without opening the app were missed or attributed only to the current day upon opening.

### Changed
- **Version Increment:** Bumped Android application version to `1.3.1` (`versionCode = 7`).

---

## [1.3.0] - 2026-08-20

### Added
- **Goal Completion & Custom Milestone Notifications (`StepNotificationHelper`):** High-priority Android notification channel (`stepcount_goals_channel`) alerting users upon reaching customizable milestone progress points (such as 50%, 65%, 75%, 80%) and celebrating 100% daily goal completion with dynamic motivational quotes from Quotable API.
- **Custom Milestone Configuration in Settings:** Dedicated "Goal Notifications" card with master toggle, preset milestone chips (50%, 65%, 75%, 80%), continuous milestone percentage slider (25% to 90%), and an instant test notification trigger with Android 13+ runtime permission handling.
- **Offline-First Streak Calculation Engine (`GetStreakUseCase`):** Pure domain use case computing active consecutive daily streaks, longest historical best streak, and total goal met days directly from Room database without requiring network access.
- **Active Streak Badge & Celebration on Dashboard:** Animated flame streak badge (`🔥 X Day Streak`) on top bar and celebratory goal achieved banner highlighting current streak upon hitting daily step targets.
- **Lifetime Best Streak on Profile:** 2x2 Lifetime Statistics grid displaying Total Steps, Distance, Calories Burned, and Best Streak (`🔥 X Days`).
- **Walking History Streak Summary Header:** Overview card in History screen tracking Current Streak, Best Streak, and Total Goals Hit with "Goal Met" checkmarks on daily log cards.
- **Backend Streak Calculation Service & API (`GET /api/steps/streak`):** Async FastAPI endpoint and service method computing user streak records from PostgreSQL database.
- **Automated Test Suite Expansion:** Added unit test suites for streak computation (`GetStreakUseCaseTest`), view models (`DashboardViewModelTest`, `HistoryViewModelTest`), and backend endpoint (`test_get_user_streak`).

### Changed
- **Sensor Delta Hook:** Wired `StepDeltaTracker` to trigger milestone and goal completion checks on every live step reading.
- **Version Increment:** Bumped Android application version to `1.3.0` (`versionCode = 6`).

---

## [1.2.2] - 2026-08-15

### Added
- **24/7 Delta Accumulation Engine (`StepDeltaTracker`):** Centralized step tracking component that computes incremental step differences from `Sensor.TYPE_STEP_COUNTER` hardware register, eliminating morning step data loss across midnight transitions.
- **Battery-Preserving Periodic Checkpoint Worker (`StepPeriodicCheckWorker`):** AndroidX `WorkManager` background worker that executes rapid (<500ms) periodic sensor samples, records deltas to Room database, and automatically refreshes the Home Screen Widget without waking the CPU or draining battery.
- **Reboot & Power-Off Resilience (`BootAndShutdownReceiver`):** BroadcastReceiver handling `ACTION_SHUTDOWN` to capture the final step snapshot before power-off and `ACTION_BOOT_COMPLETED` to recalibrate the hardware baseline to 0.
- **Unit Test Suite Expansion (`StepDeltaTrackerTest`):** Added comprehensive automated unit tests covering intra-day increments, midnight crossovers, device reboots, and zero-delta edge cases with 100% test pass rate.

### Changed
- **Sensor Delegation in `StepSensorManager`:** Refactored `StepSensorManager` to delegate hardware readings to `StepDeltaTracker`, ensuring instant catch-up when opening the app and live real-time counting while in foreground.
- **Manifest Permissions:** Added `RECEIVE_BOOT_COMPLETED` permission in `AndroidManifest.xml` for post-boot recovery.
- **Version Increment:** Bumped Android application version to `1.2.2` (`versionCode = 5`).

---

## [1.2.1] - 2026-08-15

### Changed
- **Release Signing Configuration:** Configured release builds to use the signing configuration in `build.gradle.kts` so that production and release APKs are properly signed, eliminating `INSTALL_PARSE_FAILED_NO_CERTIFICATES` installation errors.
- **Version Increment:** Bumped Android application version to `1.2.1` (`versionCode = 4`).

### Fixed
- **Unsigned APK Installation Failure:** Resolved Android installation rejection caused by unsigned release builds when sideloading or installing via ADB.

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
