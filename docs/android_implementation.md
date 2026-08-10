# StepCount Android Application – Implementation Plan & Progress Tracker

**Target Platform:** Android (Kotlin, Jetpack Compose, Material Design 3)  
**Architecture:** MVVM + Clean Architecture (`presentation`, `domain`, `data`, `sensor`)  
**Compliance Standard:** [docs/leash.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/leash.md) | [docs/ui.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/ui.md) | [docs/icons.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons.md) | [.agents/AGENTS.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/.agents/AGENTS.md)  
**Asset Sources:** Vector SVG Icons from [docs/icons/svg/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons/svg) and Logos from [docs/logo/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/logo)  
**Status:** Ready for Execution  

---

## Progress Tracking Overview

- [x] **Phase 1: Environment, Dependencies & Clean Architecture Foundation**
- [x] **Phase 2: Local Database Layer (Room DB – Offline SSOT)**
- [x] **Phase 3: Domain Layer (Models, Repositories & Use Cases)**
- [x] **Phase 4: Remote Data & Authentication Layer (Retrofit + Firebase Auth)**
- [x] **Phase 5: Hardware Sensor & Motion Tracking (`TYPE_STEP_COUNTER` + Fallback)**
- [x] **Phase 6: Presentation Layer & UI Screens (Jetpack Compose & MD3)**
- [x] **Phase 7: Background Synchronization (WorkManager)**
- [ ] **Phase 8: Comprehensive Unit Testing, Linting & Pre-Commit Verification**

---

## Phase 1: Environment, Dependencies & Clean Architecture Foundation

### Subphase 1.1: Gradle Build Configuration & Dependency Setup
- [x] Update `gradle/libs.versions.toml` with required dependencies:
  - [x] Room Database (`androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler` / KSP)
  - [x] Retrofit 2 + OkHttp 3 (`retrofit`, `converter-gson`, `logging-interceptor`)
  - [x] Firebase Auth SDK (`firebase-auth-ktx`, `play-services-auth`, Firebase BOM)
  - [x] Jetpack Compose Material Icons Extended & Navigation Compose
  - [x] AndroidX WorkManager & Testing libraries (`mockk`, `turbine`, `kotlinx-coroutines-test`)
- [x] Configure `app/build.gradle.kts` (apply KSP, add library dependencies).
- [x] Import SVG icons from `docs/icons/svg/` and logos from `docs/logo/` into `res/drawable/`.
- [x] Update `AndroidManifest.xml` with permissions (`ACTIVITY_RECOGNITION`, `INTERNET`, `ACCESS_NETWORK_STATE`).
- **Commit Target:**
  ```bash
  git commit -m "feat(setup): add required libraries, permissions, and app icon assets"
  ```

### Subphase 1.2: Directory & Package Architecture
- [x] Scaffold clean architecture package tree under `com.example.stepcount`:
  - [x] `core/` (Theme, Design Tokens, Common UI Elements, Constants)
  - [x] `data/` (Room Local DB, Retrofit Remote Services, Repository Implementations, Mappers)
  - [x] `domain/` (Pure Kotlin Domain Models, Repository Interfaces, Use Cases)
  - [x] `sensor/` (StepSensorManager, AccelerometerStepDetector)
  - [x] `presentation/` (Navigation, Screens, ViewModels, UI State Sealed Interfaces)
  - [x] `worker/` (StepSyncWorker)
- **Commit Target:**
  ```bash
  git commit -m "feat(architecture): set up clean architecture folder structure"
  ```

### Subphase 1.3: Material Design 3 Theme & Common UI Components
- [x] Implement `core/theme/Color.kt` matching energetic blues and greens from `docs/ui.md`.
- [x] Implement `core/theme/Type.kt` and `core/theme/Theme.kt` with dynamic & dark mode support.
- [x] Implement reusable UI components in `core/components/CommonComponents.kt`:
  - [x] Primary, Secondary, and Danger action buttons
  - [x] Custom Outlined text input fields with vector icons and error messages
  - [x] Metric display cards (calories, distance, active minutes)
  - [x] Status badges (`Goal Met`, `Pending Sync`, `Cached`)
  - [x] Top app bar with back navigation and settings triggers
  - [x] Bottom navigation bar with vector icons
  - [x] Confirmation alert dialogs
- **Commit Target:**
  ```bash
  git commit -m "feat(ui): create design system theme, colors, and reusable screen components"
  ```
- [x] **Phase 1 Build & Test Verification:** Run `./gradlew compileDebugKotlin` and verify 0 compilation errors.

---

## Phase 2: Local Database Layer (Room DB – Offline SSOT)

### Subphase 2.1: Room Database Entities
- [x] Implement `data/local/entity/UserProfileEntity.kt` (`userId`, `email`, `name`, `dailyGoal`).
- [x] Implement `data/local/entity/DailyStepsEntity.kt` (`userId` foreign key cascade, `date`, `steps`, `goal`, `synced`). Composite unique index on `(userId, date)`.
- [x] Implement `data/local/entity/LeaderboardCacheEntity.kt` (`userId`, `name`, `steps`, `rank`, `period`, `lastUpdated`).
- **Commit Target:**
  ```bash
  git commit -m "feat(database): create database tables for user profile, daily steps, and leaderboard cache"
  ```

### Subphase 2.2: Data Access Objects (DAOs)
- [x] Implement `data/local/dao/UserProfileDao.kt` (read profile flow, upsert, update goal).
- [x] Implement `data/local/dao/DailyStepsDao.kt` (flow for today's steps, full history, unsynced records query, upsert, delete record by date).
- [x] Implement `data/local/dao/LeaderboardCacheDao.kt` (get cached rankings, replace cache, clear cache).
- **Commit Target:**
  ```bash
  git commit -m "feat(database): create data access queries for local database operations"
  ```

### Subphase 2.3: Room Database Singleton & In-Memory Unit Tests
- [x] Implement `data/local/StepCountDatabase.kt` with thread-safe singleton instance.
- [x] Implement data mappers converting Room Entities to Domain Models.
- [x] Write unit tests in `data/local/DailyStepsDaoTest.kt` verifying all CRUD operations using in-memory SQLite database.
- **Commit Target:**
  ```bash
  git commit -m "test(database): build database singleton and add automated tests for data queries"
  ```
- [x] **Phase 2 Build & Test Verification:** Run `./gradlew test` to verify all Room DAO tests pass and database compiles.

---

## Phase 3: Domain Layer (Models, Repositories & Use Cases)

### Subphase 3.1: Domain Models
- [x] Create `domain/model/UserProfile.kt`.
- [x] Create `domain/model/DailyStepRecord.kt` (calculates calories, distance in km, active minutes, and goal percentage).
- [x] Create `domain/model/LeaderboardEntry.kt` and `domain/model/MotivationalQuote.kt`.
- [x] Create `domain/model/Resource.kt` (sealed result type for `Success`, `Error`, `Loading`).
- **Commit Target:**
  ```bash
  git commit -m "feat(domain): create core domain models and result helpers"
  ```

### Subphase 3.2: Domain Repository Interfaces
- [x] Create `domain/repository/AuthRepository.kt`.
- [x] Create `domain/repository/StepRepository.kt`.
- [x] Create `domain/repository/LeaderboardRepository.kt`.
- [x] Create `domain/repository/MotivationRepository.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(domain): define repository interfaces for app features"
  ```

### Subphase 3.3: Business Use Cases
- [x] Implement `GetTodayStepsUseCase.kt` and `RecordStepDeltaUseCase.kt`.
- [x] Implement `GetStepHistoryUseCase.kt` and `DeleteStepRecordUseCase.kt`.
- [x] Implement `GetLeaderboardUseCase.kt` (serves cache first, refreshes from remote when online).
- [x] Implement `GetMotivationalQuoteUseCase.kt` and `SyncPendingStepsUseCase.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(usecases): implement business logic use cases for steps, leaderboard, and history"
  ```
- [x] **Phase 3 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify domain models and use cases compile cleanly.

---

## Phase 4: Remote Data & Authentication Layer (Retrofit + Firebase Auth)

### Subphase 4.1: Firebase Authentication Service
- [x] Implement `data/remote/auth/FirebaseAuthService.kt`:
  - [x] Email/Password registration and login
  - [x] Firebase ID token retrieval and refresh
  - [x] Guest mode local session tracking
  - [x] Sign out handling
- **Commit Target:**
  ```bash
  git commit -m "feat(auth): set up Firebase authentication service and guest mode support"
  ```

### Subphase 4.2: Retrofit API Client & Network Interceptors
- [x] Implement `data/remote/interceptor/AuthTokenInterceptor.kt` injecting `Authorization: Bearer <token>`.
- [x] Implement `data/remote/api/StepCountApiService.kt` defining endpoints for `/auth/firebase-login`, `/steps`, `/leaderboard`, `/profile`, and `/motivation`.
- [x] Create DTO schemas matching backend request/response payloads.
- **Commit Target:**
  ```bash
  git commit -m "feat(network): build Retrofit api service and token interceptor for backend communication"
  ```

### Subphase 4.3: Repository Implementations
- [x] Implement `data/repository/AuthRepositoryImpl.kt`.
- [x] Implement `data/repository/StepRepositoryImpl.kt` (Room-first, sync-flag dirty tracking).
- [x] Implement `data/repository/LeaderboardRepositoryImpl.kt` (Cache-first with remote fallback).
- [x] Implement `data/repository/MotivationRepositoryImpl.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(repository): connect local database and network api in repository implementations"
  ```
- [x] **Phase 4 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify repository and network layers compile without errors.

---

## Phase 5: Hardware Sensor & Motion Tracking

### Subphase 5.1: Hardware Step Sensor Manager
- [x] Implement `sensor/StepSensorManager.kt`:
  - [x] Lifecycle-aware listener for `Sensor.TYPE_STEP_COUNTER`
  - [x] Daily baseline calculation (`TodaySteps = CurrentSensorCount - DailyBaseline`)
  - [x] Midnight baseline reset logic persisted in SharedPreferences
- [x] Implement fallback `sensor/AccelerometerStepDetector.kt` using 3-axis motion peak detection.
- **Commit Target:**
  ```bash
  git commit -m "feat(sensor): implement live step counter listener with accelerometer fallback"
  ```

### Subphase 5.2: Sensor Permissions & Runtime Flow
- [x] Implement runtime permission handler for `android.permission.ACTIVITY_RECOGNITION` with educational dialog.
- **Commit Target:**
  ```bash
  git commit -m "feat(permissions): add activity recognition permission handling dialog"
  ```
- [x] **Phase 5 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify sensor listeners and permissions compile cleanly.

---

## Phase 6: Presentation Layer & UI Screens (Jetpack Compose & MD3)

### Subphase 6.1: Navigation Setup
- [x] Implement `presentation/navigation/Screen.kt` routes.
- [x] Implement `presentation/navigation/AppNavGraph.kt` managing transitions across all 7 activities.
- **Commit Target:**
  ```bash
  git commit -m "feat(navigation): build screen navigation graph and route definitions"
  ```

### Subphase 6.2: Screen 1 – Splash Screen (`SplashScreen`)
- [x] Implement `presentation/splash/SplashViewModel.kt` checking authentication status.
- [x] Implement `presentation/splash/SplashScreen.kt` with logo from `docs/logo/`, app title, and loading indicator.
- **Commit Target:**
  ```bash
  git commit -m "feat(splash): build splash screen with branding and login status check"
  ```

### Subphase 6.3: Screen 2 – Authentication Screen (`AuthScreen`)
- [x] Implement `presentation/auth/AuthViewModel.kt` and `AuthUiState.kt`.
- [x] Implement `presentation/auth/AuthScreen.kt`:
  - [x] Login and Register tabs
  - [x] Form validation with clear error messages
  - [x] "Continue as Guest" offline button
  - [x] Logo from `docs/logo/` and vector icons from `docs/icons/svg/`
- **Commit Target:**
  ```bash
  git commit -m "feat(auth-screen): build login and register screen with validation and guest mode"
  ```

### Subphase 6.4: Screen 3 – Dashboard Screen (`DashboardScreen`)
- [x] Implement `presentation/dashboard/DashboardViewModel.kt` and `DashboardUiState.kt`.
- [x] Implement `presentation/dashboard/DashboardScreen.kt`:
  - [x] Top bar with settings and notification buttons
  - [x] Live circular step progress ring with goal percentage
  - [x] 3 metric cards for calories burned, walking distance, and active minutes
  - [x] Motivational quote card with refresh button
  - [x] Offline mode banner and goal reached celebration banner
  - [x] Bottom navigation bar
- **Commit Target:**
  ```bash
  git commit -m "feat(dashboard): build dashboard screen with live step ring and fitness metrics"
  ```

### Subphase 6.5: Screen 4 – History Screen (`HistoryScreen`)
- [x] Implement `presentation/history/HistoryViewModel.kt` and `HistoryUiState.kt`.
- [x] Implement `presentation/history/HistoryScreen.kt`:
  - [x] Timeframe filter chips (All Time / This Week / This Month)
  - [x] Daily step record cards showing goal met status and sync tags
  - [x] Swipe-to-delete with confirmation dialog
  - [x] Empty history state illustration
- **Commit Target:**
  ```bash
  git commit -m "feat(history): build history screen with date filters and step record management"
  ```

### Subphase 6.6: Screen 5 – Leaderboard Screen (`LeaderboardScreen`)
- [x] Implement `presentation/leaderboard/LeaderboardViewModel.kt` and `LeaderboardUiState.kt`.
- [x] Implement `presentation/leaderboard/LeaderboardScreen.kt`:
  - [x] Period tabs (Today / This Week / All Time)
  - [x] Top 3 Podium rankings with gold/silver/bronze highlights
  - [x] Scrollable ranking list for 4th and beyond with user highlight
  - [x] Offline cache banner if showing cached data
- **Commit Target:**
  ```bash
  git commit -m "feat(leaderboard): build leaderboard screen with podium and rankings list"
  ```

### Subphase 6.7: Screen 6 – Profile Screen (`ProfileScreen`)
- [x] Implement `presentation/profile/ProfileViewModel.kt` and `ProfileUiState.kt`.
- [x] Implement `presentation/profile/ProfileScreen.kt`:
  - [x] User details header with initial avatar
  - [x] Daily step goal editor with quick presets (5k, 8k, 10k, 12k)
  - [x] Lifetime statistics summary (total steps, distance, calories)
- **Commit Target:**
  ```bash
  git commit -m "feat(profile): build profile screen with goal editing and user stats"
  ```

### Subphase 6.8: Screen 7 – Settings Screen (`SettingsScreen`)
- [x] Implement `presentation/settings/SettingsViewModel.kt` and `SettingsUiState.kt`.
- [x] Implement `presentation/settings/SettingsScreen.kt`:
  - [x] Dark theme toggle switch
  - [x] Units toggle (km / miles)
  - [x] "Sync Now" button triggering sync worker
  - [x] Clear local history with confirmation dialog
  - [x] Account deletion with confirmation dialog
  - [x] App version information
- **Commit Target:**
  ```bash
  git commit -m "feat(settings): build settings screen with preferences, data sync, and account deletion"
  ```
- [x] **Phase 6 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify all 7 Compose screens and ViewModels compile without errors.

---

## Phase 7: Background Synchronization (WorkManager)

### Subphase 7.1: Step Sync Worker & Scheduler
- [x] Implement `worker/StepSyncWorker.kt` extending `CoroutineWorker` to upload unsynced Room records when online.
- [x] Configure periodic background sync constraints (`NetworkType.CONNECTED`).
- **Commit Target:**
  ```bash
  git commit -m "feat(sync): set up background worker to automatically sync offline records when online"
  ```
- [x] **Phase 7 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify WorkManager worker compiles cleanly.

---

## Phase 8: Comprehensive Unit Testing, Linting & Pre-Commit Verification

### Subphase 8.1: ViewModel & State Flow Unit Tests
- [x] Implement `SplashViewModelTest.kt`, `AuthViewModelTest.kt`, `DashboardViewModelTest.kt`, `HistoryViewModelTest.kt`, and `LeaderboardViewModelTest.kt` using Turbine and StandardTestDispatcher.
- **Commit Target:**
  ```bash
  git commit -m "test(viewmodel): add unit tests for all screen viewmodels and ui state flows"
  ```

### Subphase 8.2: Repository & Use Case Unit Tests
- [x] Implement `StepRepositoryTest.kt` and `GetLeaderboardUseCaseTest.kt` using MockK.
- **Commit Target:**
  ```bash
  git commit -m "test(domain): add unit tests for repositories and business use cases"
  ```

### Subphase 8.3: Quality Gates & Lint Verification
- [x] Run `./gradlew test` (verifying 100% unit tests pass).
- [x] Run `./gradlew lint` (0 lint errors).
- **Commit Target:**
  ```bash
  git commit -m "chore(release): verify test suite and lint quality gates for android release v1.0.0"
  ```
- [x] Update `CHANGELOG.md` under `## [Unreleased]` with all completed components.
- **Commit Target:**
  ```bash
  git commit -m "chore(release): complete Android app implementation, pass tests, and update changelog"
  ```
