# StepCount Android Application – Implementation Plan & Progress Tracker

**Target Platform:** Android (Kotlin, Jetpack Compose, Material Design 3)  
**Architecture:** MVVM + Clean Architecture (`presentation`, `domain`, `data`, `sensor`)  
**Compliance Standard:** [docs/leash.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/leash.md) | [docs/ui.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/ui.md) | [docs/icons.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons.md) | [.agents/AGENTS.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/.agents/AGENTS.md)  
**Asset Sources:** Vector SVG Icons from [docs/icons/svg/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons/svg) and Logos from [docs/logo/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/logo)  
**Status:** Ready for Execution  

---

## Progress Tracking Overview

- [ ] **Phase 1: Environment, Dependencies & Clean Architecture Foundation**
- [ ] **Phase 2: Local Database Layer (Room DB – Offline SSOT)**
- [ ] **Phase 3: Domain Layer (Models, Repositories & Use Cases)**
- [ ] **Phase 4: Remote Data & Authentication Layer (Retrofit + Firebase Auth)**
- [ ] **Phase 5: Hardware Sensor & Motion Tracking (`TYPE_STEP_COUNTER` + Fallback)**
- [ ] **Phase 6: Presentation Layer & UI Screens (Jetpack Compose & MD3)**
- [ ] **Phase 7: Background Synchronization (WorkManager)**
- [ ] **Phase 8: Comprehensive Unit Testing, Linting & Pre-Commit Verification**

---

## Phase 1: Environment, Dependencies & Clean Architecture Foundation

### Subphase 1.1: Gradle Build Configuration & Dependency Setup
- [ ] Update `gradle/libs.versions.toml` with required dependencies:
  - [ ] Room Database (`androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler` / KSP)
  - [ ] Retrofit 2 + OkHttp 3 (`retrofit`, `converter-gson`, `logging-interceptor`)
  - [ ] Firebase Auth SDK (`firebase-auth-ktx`, `play-services-auth`, Firebase BOM)
  - [ ] Jetpack Compose Material Icons Extended & Navigation Compose
  - [ ] AndroidX WorkManager & Testing libraries (`mockk`, `turbine`, `kotlinx-coroutines-test`)
- [ ] Configure `app/build.gradle.kts` (apply KSP, add library dependencies).
- [ ] Import SVG icons from `docs/icons/svg/` and logos from `docs/logo/` into `res/drawable/`.
- [ ] Update `AndroidManifest.xml` with permissions (`ACTIVITY_RECOGNITION`, `INTERNET`, `ACCESS_NETWORK_STATE`).
- **Commit Target:**
  ```bash
  git commit -m "feat(setup): add required libraries, permissions, and app icon assets"
  ```

### Subphase 1.2: Directory & Package Architecture
- [ ] Scaffold clean architecture package tree under `com.example.stepcount`:
  - [ ] `core/` (Theme, Design Tokens, Common UI Elements, Constants)
  - [ ] `data/` (Room Local DB, Retrofit Remote Services, Repository Implementations, Mappers)
  - [ ] `domain/` (Pure Kotlin Domain Models, Repository Interfaces, Use Cases)
  - [ ] `sensor/` (StepSensorManager, AccelerometerStepDetector)
  - [ ] `presentation/` (Navigation, Screens, ViewModels, UI State Sealed Interfaces)
  - [ ] `worker/` (StepSyncWorker)
- **Commit Target:**
  ```bash
  git commit -m "feat(architecture): set up clean architecture folder structure"
  ```

### Subphase 1.3: Material Design 3 Theme & Common UI Components
- [ ] Implement `core/theme/Color.kt` matching energetic blues and greens from `docs/ui.md`.
- [ ] Implement `core/theme/Type.kt` and `core/theme/Theme.kt` with dynamic & dark mode support.
- [ ] Implement reusable UI components in `core/components/CommonComponents.kt`:
  - [ ] Primary, Secondary, and Danger action buttons
  - [ ] Custom Outlined text input fields with vector icons and error messages
  - [ ] Metric display cards (calories, distance, active minutes)
  - [ ] Status badges (`Goal Met`, `Pending Sync`, `Cached`)
  - [ ] Top app bar with back navigation and settings triggers
  - [ ] Bottom navigation bar with vector icons
  - [ ] Confirmation alert dialogs
- **Commit Target:**
  ```bash
  git commit -m "feat(ui): create design system theme, colors, and reusable screen components"
  ```
- [ ] **Phase 1 Build & Test Verification:** Run `./gradlew compileDebugKotlin` and verify 0 compilation errors.

---

## Phase 2: Local Database Layer (Room DB – Offline SSOT)

### Subphase 2.1: Room Database Entities
- [ ] Implement `data/local/entity/UserProfileEntity.kt` (`userId`, `email`, `name`, `dailyGoal`).
- [ ] Implement `data/local/entity/DailyStepsEntity.kt` (`userId` foreign key cascade, `date`, `steps`, `goal`, `synced`). Composite unique index on `(userId, date)`.
- [ ] Implement `data/local/entity/LeaderboardCacheEntity.kt` (`userId`, `name`, `steps`, `rank`, `period`, `lastUpdated`).
- **Commit Target:**
  ```bash
  git commit -m "feat(database): create database tables for user profile, daily steps, and leaderboard cache"
  ```

### Subphase 2.2: Data Access Objects (DAOs)
- [ ] Implement `data/local/dao/UserProfileDao.kt` (read profile flow, upsert, update goal).
- [ ] Implement `data/local/dao/DailyStepsDao.kt` (flow for today's steps, full history, unsynced records query, upsert, delete record by date).
- [ ] Implement `data/local/dao/LeaderboardCacheDao.kt` (get cached rankings, replace cache, clear cache).
- **Commit Target:**
  ```bash
  git commit -m "feat(database): create data access queries for local database operations"
  ```

### Subphase 2.3: Room Database Singleton & In-Memory Unit Tests
- [ ] Implement `data/local/StepCountDatabase.kt` with thread-safe singleton instance.
- [ ] Implement data mappers converting Room Entities to Domain Models.
- [ ] Write unit tests in `data/local/DailyStepsDaoTest.kt` verifying all CRUD operations using in-memory SQLite database.
- **Commit Target:**
  ```bash
  git commit -m "test(database): build database singleton and add automated tests for data queries"
  ```
- [ ] **Phase 2 Build & Test Verification:** Run `./gradlew test` to verify all Room DAO tests pass and database compiles.

---

## Phase 3: Domain Layer (Models, Repositories & Use Cases)

### Subphase 3.1: Domain Models
- [ ] Create `domain/model/UserProfile.kt`.
- [ ] Create `domain/model/DailyStepRecord.kt` (calculates calories, distance in km, active minutes, and goal percentage).
- [ ] Create `domain/model/LeaderboardEntry.kt` and `domain/model/MotivationalQuote.kt`.
- [ ] Create `domain/model/Resource.kt` (sealed result type for `Success`, `Error`, `Loading`).
- **Commit Target:**
  ```bash
  git commit -m "feat(domain): create core domain models and result helpers"
  ```

### Subphase 3.2: Domain Repository Interfaces
- [ ] Create `domain/repository/AuthRepository.kt`.
- [ ] Create `domain/repository/StepRepository.kt`.
- [ ] Create `domain/repository/LeaderboardRepository.kt`.
- [ ] Create `domain/repository/MotivationRepository.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(domain): define repository interfaces for app features"
  ```

### Subphase 3.3: Business Use Cases
- [ ] Implement `GetTodayStepsUseCase.kt` and `RecordStepDeltaUseCase.kt`.
- [ ] Implement `GetStepHistoryUseCase.kt` and `DeleteStepRecordUseCase.kt`.
- [ ] Implement `GetLeaderboardUseCase.kt` (serves cache first, refreshes from remote when online).
- [ ] Implement `GetMotivationalQuoteUseCase.kt` and `SyncPendingStepsUseCase.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(usecases): implement business logic use cases for steps, leaderboard, and history"
  ```
- [ ] **Phase 3 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify domain models and use cases compile cleanly.

---

## Phase 4: Remote Data & Authentication Layer (Retrofit + Firebase Auth)

### Subphase 4.1: Firebase Authentication Service
- [ ] Implement `data/remote/auth/FirebaseAuthService.kt`:
  - [ ] Email/Password registration and login
  - [ ] Firebase ID token retrieval and refresh
  - [ ] Guest mode local session tracking
  - [ ] Sign out handling
- **Commit Target:**
  ```bash
  git commit -m "feat(auth): set up Firebase authentication service and guest mode support"
  ```

### Subphase 4.2: Retrofit API Client & Network Interceptors
- [ ] Implement `data/remote/interceptor/AuthTokenInterceptor.kt` injecting `Authorization: Bearer <token>`.
- [ ] Implement `data/remote/api/StepCountApiService.kt` defining endpoints for `/auth/firebase-login`, `/steps`, `/leaderboard`, `/profile`, and `/motivation`.
- [ ] Create DTO schemas matching backend request/response payloads.
- **Commit Target:**
  ```bash
  git commit -m "feat(network): build Retrofit api service and token interceptor for backend communication"
  ```

### Subphase 4.3: Repository Implementations
- [ ] Implement `data/repository/AuthRepositoryImpl.kt`.
- [ ] Implement `data/repository/StepRepositoryImpl.kt` (Room-first, sync-flag dirty tracking).
- [ ] Implement `data/repository/LeaderboardRepositoryImpl.kt` (Cache-first with remote fallback).
- [ ] Implement `data/repository/MotivationRepositoryImpl.kt`.
- **Commit Target:**
  ```bash
  git commit -m "feat(repository): connect local database and network api in repository implementations"
  ```
- [ ] **Phase 4 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify repository and network layers compile without errors.

---

## Phase 5: Hardware Sensor & Motion Tracking

### Subphase 5.1: Hardware Step Sensor Manager
- [ ] Implement `sensor/StepSensorManager.kt`:
  - [ ] Lifecycle-aware listener for `Sensor.TYPE_STEP_COUNTER`
  - [ ] Daily baseline calculation (`TodaySteps = CurrentSensorCount - DailyBaseline`)
  - [ ] Midnight baseline reset logic persisted in SharedPreferences
- [ ] Implement fallback `sensor/AccelerometerStepDetector.kt` using 3-axis motion peak detection.
- **Commit Target:**
  ```bash
  git commit -m "feat(sensor): implement live step counter listener with accelerometer fallback"
  ```

### Subphase 5.2: Sensor Permissions & Runtime Flow
- [ ] Implement runtime permission handler for `android.permission.ACTIVITY_RECOGNITION` with educational dialog.
- **Commit Target:**
  ```bash
  git commit -m "feat(permissions): add activity recognition permission handling dialog"
  ```
- [ ] **Phase 5 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify sensor listeners and permissions compile cleanly.

---

## Phase 6: Presentation Layer & UI Screens (Jetpack Compose & MD3)

### Subphase 6.1: Navigation Setup
- [ ] Implement `presentation/navigation/Screen.kt` routes.
- [ ] Implement `presentation/navigation/AppNavGraph.kt` managing transitions across all 7 activities.
- **Commit Target:**
  ```bash
  git commit -m "feat(navigation): build screen navigation graph and route definitions"
  ```

### Subphase 6.2: Screen 1 – Splash Screen (`SplashScreen`)
- [ ] Implement `presentation/splash/SplashViewModel.kt` checking authentication status.
- [ ] Implement `presentation/splash/SplashScreen.kt` with logo from `docs/logo/`, app title, and loading indicator.
- **Commit Target:**
  ```bash
  git commit -m "feat(splash): build splash screen with branding and login status check"
  ```

### Subphase 6.3: Screen 2 – Authentication Screen (`AuthScreen`)
- [ ] Implement `presentation/auth/AuthViewModel.kt` and `AuthUiState.kt`.
- [ ] Implement `presentation/auth/AuthScreen.kt`:
  - [ ] Login and Register tabs
  - [ ] Form validation with clear error messages
  - [ ] "Continue as Guest" offline button
  - [ ] Logo from `docs/logo/` and vector icons from `docs/icons/svg/`
- **Commit Target:**
  ```bash
  git commit -m "feat(auth-screen): build login and register screen with validation and guest mode"
  ```

### Subphase 6.4: Screen 3 – Dashboard Screen (`DashboardScreen`)
- [ ] Implement `presentation/dashboard/DashboardViewModel.kt` and `DashboardUiState.kt`.
- [ ] Implement `presentation/dashboard/DashboardScreen.kt`:
  - [ ] Top bar with settings and notification buttons
  - [ ] Live circular step progress ring with goal percentage
  - [ ] 3 metric cards for calories burned, walking distance, and active minutes
  - [ ] Motivational quote card with refresh button
  - [ ] Offline mode banner and goal reached celebration banner
  - [ ] Bottom navigation bar
- **Commit Target:**
  ```bash
  git commit -m "feat(dashboard): build dashboard screen with live step ring and fitness metrics"
  ```

### Subphase 6.5: Screen 4 – History Screen (`HistoryScreen`)
- [ ] Implement `presentation/history/HistoryViewModel.kt` and `HistoryUiState.kt`.
- [ ] Implement `presentation/history/HistoryScreen.kt`:
  - [ ] Timeframe filter chips (All Time / This Week / This Month)
  - [ ] Daily step record cards showing goal met status and sync tags
  - [ ] Swipe-to-delete with confirmation dialog
  - [ ] Empty history state illustration
- **Commit Target:**
  ```bash
  git commit -m "feat(history): build history screen with date filters and step record management"
  ```

### Subphase 6.6: Screen 5 – Leaderboard Screen (`LeaderboardScreen`)
- [ ] Implement `presentation/leaderboard/LeaderboardViewModel.kt` and `LeaderboardUiState.kt`.
- [ ] Implement `presentation/leaderboard/LeaderboardScreen.kt`:
  - [ ] Period selector (Today / This Week / All Time)
  - [ ] Ranking criteria toggle (Total Steps vs Goal %)
  - [ ] Top 3 podium display (Gold, Silver, Bronze)
  - [ ] Ranked user list with highlighted user card
  - [ ] Offline cache notice banner
- **Commit Target:**
  ```bash
  git commit -m "feat(leaderboard): build leaderboard screen with podium rankings and offline cache"
  ```

### Subphase 6.7: Screen 6 – Profile Screen (`ProfileScreen`)
- [ ] Implement `presentation/profile/ProfileViewModel.kt` and `ProfileUiState.kt`.
- [ ] Implement `presentation/profile/ProfileScreen.kt`:
  - [ ] Profile avatar, name, and email display
  - [ ] Lifetime statistics summary cards
  - [ ] Daily goal target card with edit trigger
  - [ ] Settings and Logout action buttons
- **Commit Target:**
  ```bash
  git commit -m "feat(profile): build profile screen with user stats and daily goal overview"
  ```

### Subphase 6.8: Screen 7 – Settings Screen (`SettingsScreen`)
- [ ] Implement `presentation/settings/SettingsViewModel.kt` and `SettingsUiState.kt`.
- [ ] Implement `presentation/settings/SettingsScreen.kt`:
  - [ ] Daily goal adjustment slider
  - [ ] Dark theme toggle (Light / Dark / System)
  - [ ] Unit switcher (Steps only vs Steps + Km)
  - [ ] Manual sync trigger button
  - [ ] Clear history confirmation dialog
  - [ ] Delete account confirmation modal
- **Commit Target:**
  ```bash
  git commit -m "feat(settings): build settings screen with preferences, data sync, and account deletion"
  ```
- [ ] **Phase 6 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify all 7 Compose screens and ViewModels compile without errors.

---

## Phase 7: Background Synchronization (WorkManager)

### Subphase 7.1: Step Sync Worker & Scheduler
- [ ] Implement `worker/StepSyncWorker.kt` extending `CoroutineWorker` to upload unsynced Room records when online.
- [ ] Configure periodic background sync constraints (`NetworkType.CONNECTED`).
- **Commit Target:**
  ```bash
  git commit -m "feat(sync): set up background worker to automatically sync offline records when online"
  ```
- [ ] **Phase 7 Build & Test Verification:** Run `./gradlew compileDebugKotlin` to verify WorkManager worker compiles cleanly.

---

## Phase 8: Comprehensive Unit Testing, Linting & Pre-Commit Verification

### Subphase 8.1: ViewModel & State Flow Unit Tests
- [ ] Implement `SplashViewModelTest.kt`, `AuthViewModelTest.kt`, `DashboardViewModelTest.kt`, `HistoryViewModelTest.kt`, and `LeaderboardViewModelTest.kt` using Turbine and StandardTestDispatcher.
- **Commit Target:**
  ```bash
  git commit -m "test(viewmodel): add unit tests for all screen viewmodels and ui state flows"
  ```

### Subphase 8.2: Repository & Use Case Unit Tests
- [ ] Implement `StepRepositoryTest.kt` and `GetLeaderboardUseCaseTest.kt` using MockK.
- **Commit Target:**
  ```bash
  git commit -m "test(domain): add unit tests for repositories and business use cases"
  ```

### Subphase 8.3: Quality Gate Verification & Release Staging
- [ ] Run `./gradlew test` (Verify 100% test pass rate with minimum 80% coverage).
- [ ] Run `./gradlew lint` (Verify 0 compilation errors and 0 critical warnings).
- [ ] Update `CHANGELOG.md` under `## [Unreleased]` with all completed components.
- **Commit Target:**
  ```bash
  git commit -m "chore(release): complete Android app implementation, pass tests, and update changelog"
  ```
