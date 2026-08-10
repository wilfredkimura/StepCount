# Project Rules & Guidelines for StepCount

## 1. Mandatory Engineering Leash
For all code generation, refactoring, testing, and modifications in this workspace:
- **Strict Compliance:** You MUST strictly follow all architectural patterns, code quality rules, testing standards, and pre-commit checks defined in [docs/leash.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/leash.md).
- **UI Design Compliance:** All user interface implementations must align with the layout wireframes and design system in [docs/ui.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/ui.md).
- **Icons & Visuals:** Use Material Design 3 symbols mapped in [docs/icons.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons.md) and assets from [docs/icons/svg/](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/icons/svg).

---

## 2. Summary of Non-Negotiable Architecture Rules
1. **Android:**
   - MVVM + Clean Architecture (`presentation`, `domain`, `data`, `di`).
   - Sealed interfaces for UI states (`Loading`, `Success`, `Error`).
   - Room Database as the local Single Source of Truth (offline-first).
   - Sensor lifecycle management & fallback to accelerometer if `TYPE_STEP_COUNTER` is missing.
   - Retrofit with Bearer token OkHttp interceptor.
2. **FastAPI Backend:**
   - Modular clean directory structure (`app/api/v1`, `app/core`, `app/db`, `app/models`, `app/schemas`, `app/services`).
   - Async endpoints (`async def`) and async DB sessions (`AsyncSession`).
   - Firebase Admin token verification via `get_current_user` dependency.
   - Pydantic v2 schemas separated from DB models with strict validation.
   - Standardized JSON error response envelope.
3. **Quality & Testing:**
   - Write unit tests for ViewModels, DAOs, repositories, and API endpoints (minimum 80% coverage).
   - Pass `./gradlew test`, `./gradlew lint`, `pytest`, `ruff check`, and `mypy app` before committing changes.

---

## 3. Code Commenting & Explanation Standards

### 3.1 Plain English & Simple Explanations (No Complex Jargon)
- **Thorough & Clear Comments:** Every class, function, database query, sensor calculation, and UI component must have comments explaining what it does.
- **Simple, Direct Language:** Use plain English. Avoid heavy academic jargon, overly complicated buzzwords, and dense technical phrasing.
- **Focus on Purpose and Logic:** Explain *why* a step is being done in a way that any developer or reviewer can immediately follow.

**Example Comparison:**
- ❌ *Avoid:* "Idempotent transactional mutation delegates to repository abstraction utilizing asynchronous reactive dispatchers."
- ✅ *Use:* "Saves today's step count to the local Room database. If there is no internet connection, it marks the record as not synced so we can upload it later when online."

---

## 4. Changelog Maintenance Rules
All modifications and version increments must be tracked in [CHANGELOG.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/CHANGELOG.md) adhering to the **Keep a Changelog** standard:

1. **Continuous Tracking:** Whenever implementing a new feature, bug fix, or refactor, append an entry under the `## [Unreleased]` section.
2. **Standardized Subsections:** Group entries strictly under these categories:
   - `### Added` — for new features, endpoints, or UI components.
   - `### Changed` — for changes in existing functionality or UI workflows.
   - `### Deprecated` — for features scheduled for removal.
   - `### Removed` — for deleted features or deprecated endpoints.
   - `### Fixed` — for bug fixes, crash resolutions, or logic corrections.
   - `### Security` — for security patches, token verification fixes, or credential protection.
3. **Release Tagging:** When releasing a new version:
   - Convert `[Unreleased]` to `[MAJOR.MINOR.PATCH] - YYYY-MM-DD`.
   - Create a fresh, empty `## [Unreleased]` section at the top of the file.

---

## 5. Version Control & Git Commit Rules

### 5.1 Commit Message Standards (Conventional Commits)
All commit messages must follow the format: `<type>(<scope>): <short description>`

Allowed types:
- `feat`: A new feature (e.g., `feat(android): implement live step sensor listener with baseline subtraction`)
- `fix`: A bug fix (e.g., `fix(backend): correct timezone offset in today step query`)
- `refactor`: Code change that neither fixes a bug nor adds a feature (e.g., `refactor(viewmodel): extract history state management to use case`)
- `test`: Adding or correcting tests (e.g., `test(dao): add unit tests for daily steps upsert logic`)
- `docs`: Documentation only changes (e.g., `docs(ui): add layout wireframes for leaderboard podium`)
- `chore`: Build process, dependency updates, or toolchain changes (e.g., `chore(deps): bump compose bom to 2026.02.01`)

### 5.2 Semantic Versioning (SemVer 2.0.0)
Version numbers follow `MAJOR.MINOR.PATCH`:
- **MAJOR (`+1.0.0`):** Incompatible API changes, breaking database migrations without fallback, or complete architectural shifts.
- **MINOR (`+0.1.0`):** Backward-compatible new features, new activities, new endpoints, or UI additions.
- **PATCH (`+0.0.1`):** Backward-compatible bug fixes, performance optimizations, or documentation updates.

### 5.3 Android App Version Synchronization
When releasing a new version:
- Update `versionName = "X.Y.Z"` in [android/StepCount/app/build.gradle.kts](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/android/StepCount/app/build.gradle.kts) to match the SemVer tag in `CHANGELOG.md`.
- Increment `versionCode` by `+1` for every release build.

### 5.4 Atomic Commits & Pre-Commit Gates
- **Atomic Principle:** Each commit must encapsulate a single logical unit of work. Never bundle unrelated changes.
- **Pre-Commit Verification:** Never commit changes if `./gradlew test`, `./gradlew lint`, `pytest`, `ruff check .`, or `mypy app` report any failures.
- **Zero Secrets:** Never commit `.env`, `google-services.json`, `serviceAccountKey.json`, or private credentials.
