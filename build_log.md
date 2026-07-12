# Medi-Help Build Log

Last reviewed: 2026-07-12

## Current Status

Medi-Help is implemented through **Phase 3: Medication Management MVP**. The repository currently contains a working FastAPI backend, an Android application with authentication, and local-first medication management with device-scheduled reminders.

Phase 4 is in progress. Its backend document upload and processing-job foundation is complete; the Android upload workflow remains under development.

## Completed Work

### Phase 0 - Project Foundation

- Created a monorepo with `android/`, `backend/`, `contracts/`, `docs/`, and design resource directories.
- Documented the product requirements, implementation phases, system architecture, initial API contract, and technology decision.
- Added Docker Compose services for PostgreSQL, Redis, and MinIO.
- Added backend and Android GitHub Actions workflows.
- Added environment examples and local setup instructions.
- Added the supplied Medi-Help visual references and design-system assets.

### Phase 1 - Backend Foundation

- Built a FastAPI application with versioned `/api/v1` routing and OpenAPI documentation.
- Added root and API health-check endpoints.
- Added async SQLAlchemy database configuration and Alembic migrations.
- Created `users` and `refresh_tokens` persistence models.
- Implemented password hashing and JWT bearer authentication.
- Implemented registration, login, token refresh with refresh-token rotation, logout/revocation, and current-user retrieval.
- Added authenticated dependency handling and user-scoped access control.
- Added backend linting, formatting, and test configuration with Ruff, Black, mypy, and pytest.
- Added nine authentication API tests covering successful flows and common rejection cases.

Implemented authentication endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/users/me
```

### Phase 2 - Android Foundation and Authentication

- Created a Kotlin Android app using Jetpack Compose and Material 3.
- Added Hilt dependency injection, Navigation Compose, Retrofit/OkHttp, Room, DataStore, WorkManager, and kotlinx serialization.
- Built onboarding, registration, login, and dashboard screens.
- Connected registration and login to the FastAPI backend.
- Added access/refresh token storage, bearer-token injection, and automatic token refresh handling.
- Added reusable buttons, top bars, dialogs, and loading, empty, and error states.
- Added the Medi-Help theme, typography, colors, shapes, launcher resources, and bottom navigation.
- Configured debug-only HTTP access to the emulator host while keeping release builds HTTPS-oriented.
- Added an Android CI workflow that assembles the debug app and runs unit tests.

### Phase 3 - Medication Management MVP

#### Backend

- Added medication, medication schedule, and reminder log models with a second Alembic migration.
- Implemented authenticated, user-scoped medication creation, listing, detail retrieval, updates, and deletion.
- Added schedule fields for time, frequency, meal relation, dose, and active date range.
- Added reminder action logging and adherence-summary calculation.
- Added seven medication/reminder API tests, including authentication, ownership isolation, CRUD, reminder logging, and adherence.

Implemented medication and reminder endpoints:

```text
GET    /api/v1/medications
POST   /api/v1/medications
GET    /api/v1/medications/{medication_id}
PATCH  /api/v1/medications/{medication_id}
DELETE /api/v1/medications/{medication_id}
GET    /api/v1/reminders
POST   /api/v1/reminders/log
GET    /api/v1/reminders/adherence-summary
```

#### Android

- Built medication list, add-medication, and medication-detail screens.
- Added local Room entities and DAOs for medications, schedules, and reminder logs.
- Implemented local-first reads and writes with best-effort backend refresh and WorkManager synchronization.
- Added daily medication scheduling with exact alarms when the OS permits them and an inexact fallback otherwise.
- Added medication notifications with **Taken**, **Skip**, and **Snooze** actions.
- Stored reminder actions locally and attempted backend synchronization.
- Added reminder rescheduling after device reboot or app replacement.
- Added alarm cancellation when a medication is paused or deleted.

## Developer Tooling

- GitHub Actions validates the backend and Android projects independently.
- Graphify has indexed the repository and generated `graphify-out/graph.json` and `graphify-out/GRAPH_TREE.html`.
- Graphify hooks and guidance are installed for both Claude Code and Codex through `.claude/`, `.codex/`, `CLAUDE.md`, and `AGENTS.md`.

### Phase 4 - Document Upload System (In Progress)

#### Backend completed

- Added `documents` and `processing_jobs` models and an Alembic migration.
- Added local filesystem and S3/MinIO object-storage providers behind a shared interface.
- Added authenticated multipart upload for PDF, JPEG, and PNG documents.
- Added file extension, MIME type, file signature, empty-file, and maximum-size validation.
- Added immediate queued-job creation and owner-only job status retrieval.
- Added API tests for upload, authentication, validation, ownership, and size limits, plus local-storage tests.
- Verified the complete Alembic chain upgrades to the Phase 4 schema and downgrades cleanly on the test database.

#### Incidents

- Initial verification found two misplaced model imports from a patch context mismatch; Ruff identified them and the imports were corrected.
- The project virtual environment did not yet contain the new `boto3` dependency; reinstalling `requirements.txt` resolved it.
- The full mypy run still reports pre-existing strict-typing issues in authentication, medication, and reminder modules. New Phase 4 endpoint return types were made explicit, while the older issues remain separate cleanup work.

## Verification Snapshot

Checks run on 2026-07-12:

| Check | Result |
|---|---|
| Backend pytest suite | Passed: 23 tests |
| Backend Ruff lint | Passed |
| Backend Black format check | Passed: 38 files unchanged |
| Android debug APK assembly | Passed |
| Android unit-test Gradle task | Passed, but reported `NO-SOURCE` |
| Alembic upgrade/downgrade chain | Passed through Phase 4 |

The backend tests required `DEBUG=false` to override the current local `.env` value `DEBUG=release`. `DEBUG` is a boolean setting, so the local value should be changed to `true` or `false` before running the backend normally.

## Known Gaps

- Android unit, repository, Room DAO, reminder, and Compose UI tests have not been added.
- The checked-in API contract notes are behind the implementation and do not list the medication/reminder endpoints.
- Backend CORS currently allows all origins and must be restricted before production.
- Document upload, object-storage integration, processing jobs, OCR, and AI extraction are not implemented.
- Vitals tracking, charts, lab processing, Health Connect, simplification, insights, and accessibility polish remain future phases.
- Production deployment, monitoring, privacy documents, signed Android release builds, and beta distribution remain outstanding.

## Next Planned Milestone

**Phase 4 - Document Upload System**

The next milestone is to add document and processing-job models, secure PDF/JPEG/PNG upload, MinIO/S3-backed storage, job-status APIs, and Android file/camera selection with upload progress and processing-status screens.

## Build Timeline

| Date | Milestone |
|---|---|
| 2026-06-23 | Repository structure and health-check foundation |
| 2026-06-24 | Initial backend authentication implementation |
| 2026-07-12 | Phase 1 backend foundation completed |
| 2026-07-12 | Phase 2 Android foundation and auth flow completed |
| 2026-07-12 | Phase 3 medication management and exact-alarm reminders completed |
