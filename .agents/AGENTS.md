# Project Rules & Guidelines for StepCount

## Mandatory Engineering Leash
For all code generation, refactoring, testing, and modifications in this workspace:
- **Strict Compliance:** You MUST strictly follow all architectural patterns, code quality rules, testing standards, and pre-commit checks defined in [docs/leash.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/leash.md).
- **UI Design Compliance:** All user interface implementations must align with the layout wireframes and design system in [docs/ui.md](file:///c:/Users/kimushzyyy/Documents/SCHOOL%20PROJECTS%203.2/StepCount/docs/ui.md).

## Summary of Non-Negotiable Rules
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
