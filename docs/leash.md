# StepCount – Engineering Leash & Quality Standards

**Document Version:** 1.0  
**Scope:** Android (Kotlin) + Backend (FastAPI / PostgreSQL / Firebase)  
**Status:** Mandatory Governance Document  

---

## 1. Core Philosophy & Golden Rules

1. **Zero Broken Builds:** Code that does not compile or fails automated tests must never be committed.
2. **Offline-First Architecture:** The Android app must function fully without an active internet connection. Room Database is the local Single Source of Truth (SSOT).
3. **Strict Separation of Concerns:** UI layers must never perform direct database queries, network calls, or business calculations.
4. **Zero Hardcoded Secrets:** API keys, database credentials, and Firebase service accounts must never be committed to source control.
5. **Enforce Type Safety:** Both Kotlin (Android) and Python (FastAPI via Pydantic v2 & mypy) must maintain strict type annotations without unchecked type casting.

---

## 2. Android Development Rules (Kotlin & Material 3)

### 2.1 Architectural Pattern
- Follow **MVVM (Model-View-ViewModel)** with **Clean Architecture** boundaries:
  - `presentation`: Activities, Composable/Views, ViewModels, UI State (`StateFlow`), UI Events (`SharedFlow`).
  - `domain`: UseCases / Interactors, Domain Models, Repository Interfaces (pure Kotlin, zero Android framework imports).
  - `data`: Room Entities, Room DAOs, Retrofit API Services, Data Mappers, Repository Implementations.
  - `di`: Dependency injection modules (Hilt / Koin / Manual DI).

### 2.2 Kotlin Code Quality & Concurrency
- **Immutability:** Default to `val`. Use `data class` with `copy()` for state updates.
- **UI State Modeling:** Use sealed interfaces/classes for screen states:
  ```kotlin
  sealed interface DashboardUiState {
      data object Loading : DashboardUiState
      data class Success(
          val stepsToday: Long,
          val goal: Int,
          val quote: String?,
          val isOffline: Boolean
      ) : DashboardUiState
      data class Error(val message: String) : DashboardUiState
  }
  ```
- **Coroutines & Dispatchers:**
  - Never use `GlobalScope`.
  - Always launch coroutines within `viewModelScope` or lifecycle-aware scopes.
  - Database and network calls must explicitly execute on `Dispatchers.IO`.
  - Flow collection in Activities must use `repeatOnLifecycle(Lifecycle.State.STARTED)`.

### 2.3 Room Database Standards
- **Entities & Relationships:**
  - Explicitly define table names, column names, foreign keys, and indices.
  - Cascade deletes appropriately (e.g., deleting a `UserProfile` cascades to `DailySteps`).
- **DAOs:**
  - Read queries returning live data must return `Flow<T>`.
  - Single-shot read/write queries must be `suspend` functions.
  - No database queries on the main thread (Room compile-time check enforced).

### 2.4 Sensor Management Rules
- **Lifecycle Awareness:**
  - Register `SensorEventListener` in `onStart()` or `onResume()`; unregister in `onStop()` or `onPause()` to prevent battery drain (unless tracking via dedicated Foreground Service).
- **Baseline Math:**
  - `TodaySteps = TotalSensorCount - DailyBaselineCount`. Baseline must reset at midnight (00:00).
- **Graceful Degradation:**
  - Check if `Sensor.TYPE_STEP_COUNTER` is present on the device.
  - If missing, fall back to accelerometer-based step detection (`Sensor.TYPE_ACCELEROMETER`) and display a non-blocking user alert.
- **Permissions:**
  - Request `android.permission.ACTIVITY_RECOGNITION` at runtime on Android 10+ (API 29+).

### 2.5 Retrofit & Networking Standards
- Use **OkHttp Interceptor** to attach `Authorization: Bearer <firebase_id_token>` dynamically to all authenticated endpoints.
- Configure sensible timeouts:
  - Connect Timeout: `15s`
  - Read Timeout: `30s`
  - Write Timeout: `30s`
- Map network errors and HTTP codes (401, 404, 500) into domain `Result.Failure` exceptions.

---

## 3. FastAPI Backend Rules (Python & PostgreSQL)

### 3.1 Directory Structure
```
backend/
├── app/
│   ├── api/
│   │   └── v1/
│   │       ├── endpoints/
│   │       │   ├── auth.py
│   │       │   ├── steps.py
│   │       │   ├── leaderboard.py
│   │       │   ├── profile.py
│   │       │   └── motivation.py
│   │       └── router.py
│   ├── core/
│   │   ├── config.py         # Settings & environment variables
│   │   ├── security.py       # Firebase token verification
│   │   └── exceptions.py     # Custom exceptions & handlers
│   ├── db/
│   │   ├── session.py        # Async engine & sessionmaker
│   │   └── base.py           # SQLAlchemy declarative base
│   ├── models/               # SQLAlchemy DB Models
│   │   ├── user.py
│   │   └── daily_steps.py
│   ├── schemas/              # Pydantic v2 Request/Response Schemas
│   │   ├── auth.py
│   │   ├── steps.py
│   │   └── leaderboard.py
│   ├── services/             # Business logic & external API clients
│   │   ├── firebase_service.py
│   │   ├── step_service.py
│   │   └── quotable_service.py
│   └── main.py               # Application factory & middleware
├── alembic/                  # Database migration scripts
├── tests/                    # Unit and integration test suite
├── .env.example
├── pyproject.toml / requirements.txt
└── Dockerfile
```

### 3.2 Async Endpoints & Database Sessions
- All I/O route handlers must be declared with `async def`.
- Database access must use asynchronous sessions (`AsyncSession` with `asyncpg`).
- Inject DB sessions using FastAPI's dependency injection:
  ```python
  @router.get("/steps/today", response_model=DailyStepResponse)
  async def get_today_steps(
      current_user: User = Depends(get_current_user),
      db: AsyncSession = Depends(get_db)
  ):
      return await step_service.get_user_today_steps(db, current_user.id)
  ```

### 3.3 Firebase Token Authentication Leash
- All protected endpoints must declare `Depends(get_current_user)`.
- `get_current_user` must:
  1. Extract the Bearer token from the `Authorization` header.
  2. Verify the token with `firebase_admin.auth.verify_id_token(token)`.
  3. Raise `HTTPException(status_code=401, detail="Invalid or expired authentication token")` if invalid.
  4. Lookup or synchronize the user record in PostgreSQL.

### 3.4 Pydantic v2 Schemas & Data Validation
- Separate DB models from API schemas:
  - Input: `StepCreate`, `StepUpdate`, `ProfileUpdate`
  - Output: `StepResponse`, `LeaderboardResponse`, `UserProfileResponse`
- Apply field constraints (`Field(ge=0, le=100000)`, `EmailStr`, etc.).
- Set `model_config = ConfigDict(from_attributes=True)` on output models.

### 3.5 Standardized Error Responses
All API error responses must adhere to a uniform structure:
```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Step record for date 2026-08-10 was not found",
    "details": null
  }
}
```

---

## 4. Offline-First & Data Sync Leash

1. **Write-To-Room-First:**
   - Every step update must be saved to Room immediately with `synced = false`.
   - The UI must reflect local state instantly without waiting for a server round-trip.
2. **Idempotent Sync Operations:**
   - The backend `POST /api/steps` and `PUT /api/steps/{date}` must support upsert logic (insert if new, update if date already exists for that user).
3. **Background Sync:**
   - Use `WorkManager` on Android with network constraints (`NetworkType.CONNECTED`) to sync pending records (`synced = false`) automatically in the background.
4. **Leaderboard Caching:**
   - The Android app fetches the leaderboard and caches the top 100 rows into `leaderboard_cache` in Room.
   - When offline, the app displays the cached leaderboard with an informative banner ("Showing cached data").

---

## 5. Unit Testing & Verification Standards

### 5.1 Android Test Requirements
- **ViewModel Tests:**
  - Must test all state transitions (`Loading` -> `Success` / `Error`).
  - Use `kotlinx-coroutines-test` (`StandardTestDispatcher` / `TestScope`) and `Turbine` for Flow assertion.
- **DAO Tests:**
  - Test all CRUD methods using `Room.inMemoryDatabaseBuilder`.
- **Repository Tests:**
  - Mock remote Retrofit services and Room DAOs using `MockK` or `Mockito`.
  - Verify offline fallback logic and sync flag updates.

### 5.2 FastAPI Test Requirements
- **Test Runner:** `pytest` with `pytest-asyncio`.
- **Test Client:** `httpx.AsyncClient(app=app, base_url="http://test")`.
- **Database Isolation:** Use a separate PostgreSQL test database or an in-memory SQLite database with async driver for tests.
- **Mocking:**
  - Mock Firebase Admin token verification so tests run without external Firebase network calls.
  - Mock Quotable API calls in `test_motivation.py`.
- **Test Coverage:** All route handlers, auth dependencies, and CRUD services must have at least 80% test branch coverage.

---

## 6. Code Quality & Static Analysis Checks

### 6.1 Android Quality Checks
| Tool | Purpose | Command |
| :--- | :--- | :--- |
| **Android Lint** | Detects layout bugs, security flaws, performance issues | `./gradlew lint` |
| **Kotlin Unit Tests** | Executes all local JVM unit tests | `./gradlew test` |
| **ktlint / Detekt** | Enforces Kotlin code formatting and style guidelines | `./gradlew ktlintCheck` |

### 6.2 FastAPI Quality Checks
| Tool | Purpose | Command |
| :--- | :--- | :--- |
| **Ruff / Flake8** | Fast linter detecting syntax errors and code smells | `ruff check .` |
| **Ruff / Black** | Auto-formatting for PEP 8 compliance | `ruff format --check .` |
| **Mypy** | Strict type-checking validation | `mypy app` |
| **Pytest** | Executes asynchronous API and service unit tests | `pytest -v --cov=app` |

---

## 7. Pre-Commit Gatekeeper Protocol (Mandatory Checklist)

Before creating a git commit or merging changes into the repository, developers and AI agents **MUST** execute and pass the following checklist:

```
                      PRE-COMMIT QUALITY GATE
  ┌─────────────────────────────────────────────────────────────┐
  │ 1. Android Code:                                            │
  │    [ ] ./gradlew test              (All unit tests PASS)    │
  │    [ ] ./gradlew lint              (0 Errors, 0 Warnings)   │
  │                                                             │
  │ 2. Backend Code:                                            │
  │    [ ] pytest                      (All test cases PASS)    │
  │    [ ] ruff check .                (No linting errors)      │
  │    [ ] mypy app                    (Strict typing PASS)     │
  │                                                             │
  │ 3. Security & Cleanliness:                                  │
  │    [ ] No secrets / tokens / passwords in code or git diff   │
  │    [ ] No debug print statements or commented dead code     │
  │    [ ] Room & Alembic migrations updated if models changed  │
  └─────────────────────────────────────────────────────────────┘
                               │
                       [ ALL CHECKS PASS ]
                               │
                               ▼
                       GIT COMMIT ALLOWED
```

### Git Commit Message Standards
Follow the **Conventional Commits** specification:
- `feat(android): add accelerometer fallback for missing step sensor`
- `feat(backend): add leaderboard endpoint with goal percentage sorting`
- `fix(room): resolve foreign key cascade on user profile deletion`
- `test(auth): add unit test for expired firebase token rejection`
- `refactor(viewmodel): migrate dashboard state flow to sealed interface`
- `docs(ui): update layout sketches for leaderboard podium standings`
