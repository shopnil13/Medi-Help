# Medi-Help Build Log

Last reviewed: 2026-07-13

## Current Status

Medi-Help is implemented through **Phase 5: OCR and AI Extraction Pipeline**. Uploaded prescriptions and lab reports can now be processed into validated, safety-flagged data and reviewed, edited, selected, and confirmed on Android before affecting any health records.

Phase 6 is in progress. Backend prescription-to-medication conversion is complete; Android routing and the success workflow remain under development.

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

### Phase 4 - Document Upload System

#### Backend completed

- Added `documents` and `processing_jobs` models and an Alembic migration.
- Added local filesystem and S3/MinIO object-storage providers behind a shared interface.
- Added authenticated multipart upload for PDF, JPEG, and PNG documents.
- Added file extension, MIME type, file signature, empty-file, and maximum-size validation.
- Added immediate queued-job creation and owner-only job status retrieval.
- Added API tests for upload, authentication, validation, ownership, and size limits, plus local-storage tests.
- Verified the complete Alembic chain upgrades to the Phase 4 schema and downgrades cleanly on the test database.

#### Android completed

- Added a Documents navigation destination and dashboard entry point.
- Added prescription, lab report, and unknown document-type selection.
- Added Android file selection for PDF, JPEG, and PNG files with persisted read permission.
- Added in-app CameraX capture, runtime camera permission handling, FileProvider sharing, and image preview.
- Added PDF/image metadata display and bounded 10 MB client-side reads.
- Added Retrofit multipart upload and authenticated job-status requests.
- Added a Room document cache with a version 1-to-2 migration.
- Added a processing-status screen with three-second polling that stops on review, completion, or failure.
- Added three Android unit tests for upload state and document mapping.

#### Incidents

- Initial verification found two misplaced model imports from a patch context mismatch; Ruff identified them and the imports were corrected.
- The project virtual environment did not yet contain the new `boto3` dependency; reinstalling `requirements.txt` resolved it.
- The full mypy run still reports pre-existing strict-typing issues in authentication, medication, and reminder modules. New Phase 4 endpoint return types were made explicit, while the older issues remain separate cleanup work.
- Running Gradle build, test, and lint concurrently caused Windows Kotlin incremental-cache contention. Gradle recovered for build/tests; stopping the daemons and rerunning checks sequentially produced clean results.
- Android lint found that camera permission implicitly required camera hardware. Declaring the camera feature optional preserved installation support on devices without a camera and cleared lint.

### Phase 5 - OCR and AI Extraction Pipeline

#### Backend completed

- Added replaceable OCR and extraction provider interfaces.
- Added local Tesseract OCR and Google Vision REST adapters.
- Added PDF page rendering plus EXIF orientation, deskewing, noise reduction, contrast enhancement, and thresholding.
- Added strict prescription and lab-report extraction schemas and provider prompts.
- Added local heuristic extraction and an OpenAI-compatible strict JSON-schema adapter.
- Added confidence scoring and safety warnings for uncertain medicines, missing dosage/frequency, and unknown lab units.
- Added Celery/Redis job dispatch with retry handling and API/worker Docker Compose services.
- Added raw OCR storage, validated structured-result storage, `needs_review` status, and separate user-confirmed result storage.
- Added owner-only extraction confirmation without automatically creating medications or vitals.
- Added processing, safety, PDF conversion, raw-text persistence, and confirmation tests.

#### Android completed

- Added typed prescription and lab extraction models with polymorphic JSON handling.
- Added Room persistence for extracted and confirmed results with a version 2-to-3 migration.
- Added separate prescription and lab-report review layouts.
- Added editable medicine, dosage, frequency, biomarker, value, unit, and reference-range fields.
- Added selection checkboxes, confidence percentages, and extraction safety warnings.
- Added confirmation submission and local cache refresh without creating medications or vitals.
- Added review navigation from `needs_review` job status and unit coverage for mapping/editing/confirmation state.

#### Incidents

- A migration downgrade initially saw an Alembic version marker left behind after pytest dropped test tables. Resetting only the disposable test marker allowed the complete upgrade/downgrade chain to pass.
- Docker package downloads failed twice over plain HTTP and then stalled over HTTPS on the current network. Retry and HTTPS handling were added to the Dockerfile, but the OCR image build remains unverified locally.
- No system Tesseract executable is installed on the Windows host. OCR orchestration is covered with provider doubles; image/PDF preprocessing and structured extraction run locally in tests.

### Phase 6 - Prescription-to-Medication Automation (In Progress)

#### Backend completed

- Added `POST /api/v1/medications/confirm-extracted` for owner-scoped confirmed jobs.
- Added idempotent conversion of selected prescription entries into active medication records.
- Added schedule conversion for explicit valid times without inventing reminder times from frequency text.
- Added source document/job references, confidence values, confirmed instructions, and review-state clearing.
- Added migration coverage and end-to-end API assertions for conversion, schedules, provenance, and repeat requests.

#### Incidents

- The first explicit-time conversion test found a missing `datetime` import. Ruff and the failing endpoint test identified it; adding the import restored the full green suite.

## Verification Snapshot

Checks run on 2026-07-13:

| Check | Result |
|---|---|
| Backend pytest suite | Passed: 27 tests |
| Backend Ruff lint | Passed |
| Backend Black format check | Passed: 66 files unchanged |
| Android debug APK assembly | Passed |
| Android unit-test Gradle task | Passed: 4 tests |
| Android lint | Passed |
| Alembic upgrade/downgrade chain | Passed through Phase 6 |
| Docker Compose configuration | Passed |
| Backend OCR Docker image | Not verified: package mirror timed out |

The backend tests required `DEBUG=false` to override the current local `.env` value `DEBUG=release`. `DEBUG` is a boolean setting, so the local value should be changed to `true` or `false` before running the backend normally.

## Known Gaps

- Android repository, Room DAO, reminder, camera, and Compose UI coverage remains to be added.
- Camera capture and multipart upload still need end-to-end verification on an emulator or physical device with the backend running.
- Backend CORS currently allows all origins and must be restricted before production.
- Production OCR/LLM providers still need credentialed end-to-end verification with representative medical documents.
- Vitals tracking, charts, lab processing, Health Connect, simplification, insights, and accessibility polish remain future phases.
- Production deployment, monitoring, privacy documents, signed Android release builds, and beta distribution remain outstanding.

## Next Planned Milestone

**Phase 6 - Prescription-to-Medication Automation**

The next milestone is to convert selected, confirmed prescription medicines into backend medication/schedule records, insert them into Android Room, schedule local reminders, and update the dashboard immediately.

## Build Timeline

| Date | Milestone |
|---|---|
| 2026-06-23 | Repository structure and health-check foundation |
| 2026-06-24 | Initial backend authentication implementation |
| 2026-07-12 | Phase 1 backend foundation completed |
| 2026-07-12 | Phase 2 Android foundation and auth flow completed |
| 2026-07-12 | Phase 3 medication management and exact-alarm reminders completed |
| 2026-07-13 | Phase 4 document upload, storage, camera capture, and job tracking completed |
| 2026-07-13 | Phase 5 OCR/extraction pipeline and editable Android review completed |
