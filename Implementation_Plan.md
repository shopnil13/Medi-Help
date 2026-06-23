# Medi-Help Android App — Implementation Plan

**File name:** `Implementation_Plan.md`  
**Target app:** Medi-Help Application  
**Frontend:** Android — Kotlin + Jetpack Compose  
**Backend:** Python — FastAPI  
**Architecture style:** Local-first Android app + async AI-processing backend  
**Primary users:** Elderly users, general public, and users with limited medical knowledge  

---

## 1. Product Goal

Build a simple, accessible Medi-Help Android app that helps users:

1. Upload prescriptions and lab reports.
2. Extract medicines, dosage schedules, and biomarker values automatically.
3. Translate medical jargon into easy language.
4. Track medicines, reminders, vitals, and health trends.
5. View personalized but non-diagnostic health tips based on their data.

The system must feel fast for daily actions such as opening the dashboard, checking medicines, receiving medication reminders, and manually adding vitals. Slower tasks such as OCR, AI document parsing, report analysis, and personalized insight generation can run on the backend asynchronously.

---

## 2. Revised Tech Stack

### 2.1 Android Frontend Stack

| Area | Recommended Technology | Reason |
|---|---|---|
| Language | Kotlin | Modern Android-first language with strong coroutine support. |
| UI | Jetpack Compose | Declarative UI, faster iteration, better for accessibility-focused interfaces. |
| UI System | Material 3 | Clean, accessible, modern Android UI components. |
| Architecture | MVVM + Clean Architecture | Separates UI, state, domain logic, data access, and API calls. |
| State Management | ViewModel + StateFlow | Lifecycle-aware, reactive, Compose-friendly state handling. |
| Dependency Injection | Hilt | Standard Android DI solution, good for scalable apps. |
| Networking | Retrofit + OkHttp | Reliable REST client, interceptors for auth/logging/retry. |
| JSON | kotlinx.serialization or Moshi | Type-safe API parsing. Prefer kotlinx.serialization if using Kotlin-first models. |
| Local Database | Room | Local-first cache for medicines, reminders, vitals, document status, and dashboards. |
| Preferences | Jetpack DataStore | Store lightweight settings such as onboarding status, theme, and sync flags. |
| Secure Storage | Android Keystore + EncryptedSharedPreferences/DataStore wrapper | Protect access tokens and sensitive local settings. |
| Background Sync | WorkManager | Reliable background sync, document status polling, wearable sync, upload retry. |
| Exact Medicine Alarms | AlarmManager + local notifications | Medication reminders should trigger on time even without network. |
| Notifications | Android Notifications API | Local medication reminders and processing status notifications. |
| Image/PDF Selection | Android Photo Picker + Storage Access Framework | Safe document upload from gallery/files. |
| Camera Capture | CameraX | Capture prescriptions/lab reports directly inside the app. |
| Image Loading | Coil | Compose-friendly image loading for uploaded document previews. |
| Charts | Vico or MPAndroidChart Compose wrapper | For vitals and biomarker trends. Prefer Vico for Compose-native UI. |
| Wearable/Vitals Integration | Health Connect SDK | Android-first health data layer for heart rate, blood pressure, glucose, etc. |
| Authentication UI | Custom Compose screens | Elderly-friendly login/signup with large text and simple forms. |
| Testing | JUnit, MockK, Turbine, Espresso/Compose UI Test | Unit, flow, and UI testing. |
| Build System | Gradle Kotlin DSL | Better Kotlin-based build configuration. |

### 2.2 Backend Stack

| Area | Recommended Technology | Reason |
|---|---|---|
| Language | Python | Strong AI/OCR ecosystem and rapid backend development. |
| API Framework | FastAPI | High-performance Python API framework with type hints and automatic OpenAPI docs. |
| Server | Uvicorn behind Gunicorn or Uvicorn workers | Production-ready ASGI serving. |
| Data Validation | Pydantic v2 | Strong request/response schemas and structured AI output validation. |
| ORM | SQLAlchemy 2.x Async ORM | Robust PostgreSQL support and clean async data access. |
| Migrations | Alembic | Database schema versioning. |
| Main Database | PostgreSQL | Reliable relational database for users, medications, vitals, documents, jobs, audit logs. |
| Cache / Queue Broker | Redis | Fast caching, rate limiting, temporary job state, Celery broker. |
| Background Jobs | Celery + Redis | Async OCR, AI parsing, report generation, reminder sync jobs. |
| File Storage | S3-compatible object storage: AWS S3, Cloudflare R2, or MinIO for local dev | Store uploaded PDFs/images securely outside the database. |
| OCR Pipeline | Provider-based architecture: Google Cloud Vision/Document AI, Azure Document Intelligence, PaddleOCR/Tesseract fallback | OCR quality matters; provider abstraction prevents lock-in. |
| PDF/Image Processing | PyMuPDF, pdf2image, Pillow, OpenCV | Convert PDFs/images, preprocess scans, rotate/crop/clean documents. |
| AI/LLM Layer | Provider adapter using structured JSON output | Extract medicines/lab markers and simplify language while validating response shape. |
| Medical Safety Layer | Rule-based guardrails + confidence scoring + user confirmation | Prevent unsafe automatic assumptions. |
| Auth | JWT access token + refresh token | Standard mobile API authentication. |
| Password Hashing | Argon2id or bcrypt | Secure password storage. Prefer Argon2id when possible. |
| API Documentation | OpenAPI generated by FastAPI | Android team can generate/request contracts from API docs. |
| Testing | pytest, pytest-asyncio, httpx, factory_boy | Backend unit/integration testing. |
| Code Quality | Ruff, Black, mypy | Formatting, linting, type checking. |
| Observability | structlog/loguru + Sentry + Prometheus/Grafana optional | Debugging production issues and AI job failures. |
| Deployment | Docker + Docker Compose for dev; VPS/Render/Fly.io/AWS later | Consistent local and production environments. |
| Reverse Proxy | Nginx or Caddy | HTTPS, routing, compression, file limits. |

### 2.3 Database & Storage Stack

| Component | Choice |
|---|---|
| Relational DB | PostgreSQL |
| Local Android DB | Room SQLite |
| Cache | Redis |
| File Storage | S3-compatible storage |
| Search/Analytics Later | PostgreSQL full-text search first; OpenSearch only if needed later |
| Backups | Automated PostgreSQL backups + object storage lifecycle policy |

### 2.4 AI/OCR Stack Recommendation

For MVP, do **not** hard-code one AI provider throughout the whole app. Create an internal backend interface like:

```python
class DocumentAIProvider:
    async def extract_prescription(...): ...
    async def extract_lab_report(...): ...
    async def simplify_medical_text(...): ...
```

Recommended strategy:

1. **OCR Provider Layer**
   - First choice for production quality: Google Cloud Vision / Document AI or Azure Document Intelligence.
   - Local/dev fallback: PaddleOCR or Tesseract.

2. **LLM Provider Layer**
   - Use an adapter-based design.
   - The backend should only depend on your own `LLMClient` interface.
   - Use strict JSON schemas for medicine extraction, biomarker extraction, and simplification.

3. **Validation Layer**
   - Never directly trust OCR/LLM output.
   - Validate medicine names, dosage, frequency, dates, units, and confidence scores.
   - Require user confirmation before automatically activating medication reminders.

---

## 3. Core Architecture Decision

Use a **local-first Android app** with a **backend-driven AI processing pipeline**.

### 3.1 Features That Must Feel Instant

These should work locally from Android Room database whenever possible:

- Home dashboard loading.
- Current medication list.
- Medication reminders.
- Manual vital entry.
- Vitals chart rendering from cached data.
- Previously simplified medicine/lab explanations.
- User settings and accessibility preferences.

### 3.2 Features Where Backend Latency Is Acceptable

These can run through FastAPI and background workers:

- Prescription upload.
- Lab report upload.
- OCR processing.
- Medicine extraction.
- Biomarker extraction.
- Medical jargon simplification.
- Personalized health tips.
- Historical analysis reports.
- Cloud backup/sync.

### 3.3 Async Processing Pattern

The backend should not keep the Android app waiting during heavy document processing.

Recommended flow:

```text
Android uploads document
        ↓
FastAPI stores file and creates processing_job
        ↓
FastAPI immediately returns job_id
        ↓
Celery worker processes OCR + AI extraction
        ↓
Worker stores extracted medicines/vitals/explanations
        ↓
Android polls job status using WorkManager or foreground screen refresh
        ↓
User reviews extracted data
        ↓
Confirmed medicines/vitals are saved locally and synced with backend
```

---

## 4. Complete System Path

### 4.1 Prescription Processing Path

```text
User opens app
  → Upload prescription image/PDF
  → Android sends file to backend
  → Backend creates document record
  → Backend stores file in object storage
  → Backend creates OCR/AI job
  → Worker extracts raw text
  → Worker extracts medicine name, strength, dose, time, duration
  → Worker simplifies each medicine instruction
  → Backend stores extracted result as pending_review
  → Android receives job completion
  → User reviews and confirms
  → App creates medicine records
  → App schedules local reminders
  → Backend syncs confirmed medication plan
```

### 4.2 Lab Report Processing Path

```text
User uploads lab report
  → Backend creates document processing job
  → OCR extracts test names, values, units, reference ranges, dates
  → AI normalizes biomarker names
  → Backend stores biomarkers with source = Lab Report
  → Android shows review screen
  → User confirms extracted biomarkers
  → App updates Health Chart
  → Backend stores historical biomarker records
```

### 4.3 Manual Vitals Path

```text
User opens Vitals screen
  → Adds BP / HR / glucose / custom marker
  → Android saves immediately to Room
  → Android updates chart instantly
  → WorkManager syncs data to backend
  → Backend stores source = Manual
```

### 4.4 Wearable Vitals Path

```text
User grants Health Connect permission
  → Android reads allowed health records
  → Android stores readings in Room
  → Android displays chart
  → WorkManager periodically syncs selected readings to backend
  → Backend stores source = Health Connect
```

### 4.5 Personalized Health Tips Path

```text
Backend receives latest vitals + medicines + lab history
  → Analysis worker creates non-diagnostic summary
  → AI simplifies advice in friendly language
  → Safety layer removes diagnosis/urgent medical claims
  → App displays tips with disclaimer
```

---

## 5. Repository Structure

Recommended repository style: **monorepo**.

```text
medi-help/
│
├── README.md
├── Implementation_Plan.md
├── .gitignore
├── .env.example
├── docker-compose.yml
├── Makefile
│
├── docs/
│   ├── requirements/
│   │   └── Medi-Help_Requirement_Analysis.md
│   ├── api/
│   │   └── openapi-notes.md
│   ├── architecture/
│   │   ├── system-overview.md
│   │   ├── data-flow.md
│   │   └── security-model.md
│   └── decisions/
│       ├── ADR-001-tech-stack.md
│       ├── ADR-002-local-first-android.md
│       └── ADR-003-ai-provider-abstraction.md
│
├── android/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml
│   │       │   ├── java/com/example/medihelp/
│   │       │   │   ├── MediHelpApp.kt
│   │       │   │   ├── MainActivity.kt
│   │       │   │   │
│   │       │   │   ├── core/
│   │       │   │   │   ├── common/
│   │       │   │   │   │   ├── Result.kt
│   │       │   │   │   │   ├── ErrorMapper.kt
│   │       │   │   │   │   └── DateTimeUtils.kt
│   │       │   │   │   ├── designsystem/
│   │       │   │   │   │   ├── theme/
│   │       │   │   │   │   ├── components/
│   │       │   │   │   │   └── accessibility/
│   │       │   │   │   ├── navigation/
│   │       │   │   │   │   ├── AppNavGraph.kt
│   │       │   │   │   │   └── Routes.kt
│   │       │   │   │   ├── network/
│   │       │   │   │   │   ├── ApiClient.kt
│   │       │   │   │   │   ├── AuthInterceptor.kt
│   │       │   │   │   │   └── NetworkMonitor.kt
│   │       │   │   │   ├── database/
│   │       │   │   │   │   ├── AppDatabase.kt
│   │       │   │   │   │   ├── converters/
│   │       │   │   │   │   └── dao/
│   │       │   │   │   ├── datastore/
│   │       │   │   │   │   └── UserPreferencesDataStore.kt
│   │       │   │   │   ├── security/
│   │       │   │   │   │   ├── TokenStorage.kt
│   │       │   │   │   │   └── BiometricAuthManager.kt
│   │       │   │   │   ├── notifications/
│   │       │   │   │   │   ├── MedicationNotificationManager.kt
│   │       │   │   │   │   └── NotificationChannels.kt
│   │       │   │   │   ├── reminders/
│   │       │   │   │   │   ├── MedicationAlarmScheduler.kt
│   │       │   │   │   │   └── ReminderReceiver.kt
│   │       │   │   │   ├── sync/
│   │       │   │   │   │   ├── SyncWorker.kt
│   │       │   │   │   │   └── DocumentStatusWorker.kt
│   │       │   │   │   └── healthconnect/
│   │       │   │   │       ├── HealthConnectManager.kt
│   │       │   │   │       └── HealthPermissionManager.kt
│   │       │   │   │
│   │       │   │   ├── feature_auth/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   ├── feature_onboarding/
│   │       │   │   │   ├── presentation/
│   │       │   │   │   └── domain/
│   │       │   │   ├── feature_dashboard/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   ├── feature_documents/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   ├── feature_medications/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   ├── feature_vitals/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   ├── feature_insights/
│   │       │   │   │   ├── data/
│   │       │   │   │   ├── domain/
│   │       │   │   │   └── presentation/
│   │       │   │   └── feature_settings/
│   │       │   │       ├── data/
│   │       │   │       ├── domain/
│   │       │   │       └── presentation/
│   │       │   │
│   │       │   └── res/
│   │       │       ├── drawable/
│   │       │       ├── mipmap-hdpi/
│   │       │       ├── values/
│   │       │       └── xml/
│   │       ├── test/
│   │       └── androidTest/
│   │
│   └── buildSrc/
│       └── src/main/kotlin/
│           └── Dependencies.kt
│
├── backend/
│   ├── pyproject.toml
│   ├── requirements.txt
│   ├── requirements-dev.txt
│   ├── alembic.ini
│   ├── Dockerfile
│   ├── .env.example
│   ├── scripts/
│   │   ├── create_superuser.py
│   │   ├── seed_dev_data.py
│   │   └── run_worker.py
│   │
│   ├── alembic/
│   │   ├── env.py
│   │   └── versions/
│   │
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py
│   │   ├── api/
│   │   │   ├── __init__.py
│   │   │   └── v1/
│   │   │       ├── router.py
│   │   │       └── endpoints/
│   │   │           ├── auth.py
│   │   │           ├── users.py
│   │   │           ├── documents.py
│   │   │           ├── medications.py
│   │   │           ├── reminders.py
│   │   │           ├── vitals.py
│   │   │           ├── health_connect.py
│   │   │           ├── insights.py
│   │   │           └── jobs.py
│   │   │
│   │   ├── core/
│   │   │   ├── config.py
│   │   │   ├── security.py
│   │   │   ├── exceptions.py
│   │   │   ├── logging.py
│   │   │   └── permissions.py
│   │   │
│   │   ├── db/
│   │   │   ├── session.py
│   │   │   ├── base.py
│   │   │   └── init_db.py
│   │   │
│   │   ├── models/
│   │   │   ├── user.py
│   │   │   ├── document.py
│   │   │   ├── processing_job.py
│   │   │   ├── medication.py
│   │   │   ├── medication_schedule.py
│   │   │   ├── reminder_log.py
│   │   │   ├── vital_record.py
│   │   │   ├── biomarker.py
│   │   │   ├── insight.py
│   │   │   └── audit_log.py
│   │   │
│   │   ├── schemas/
│   │   │   ├── auth.py
│   │   │   ├── user.py
│   │   │   ├── document.py
│   │   │   ├── medication.py
│   │   │   ├── vital.py
│   │   │   ├── insight.py
│   │   │   └── job.py
│   │   │
│   │   ├── repositories/
│   │   │   ├── user_repository.py
│   │   │   ├── document_repository.py
│   │   │   ├── medication_repository.py
│   │   │   ├── vital_repository.py
│   │   │   └── insight_repository.py
│   │   │
│   │   ├── services/
│   │   │   ├── auth_service.py
│   │   │   ├── document_service.py
│   │   │   ├── medication_service.py
│   │   │   ├── reminder_service.py
│   │   │   ├── vital_service.py
│   │   │   ├── insight_service.py
│   │   │   └── notification_service.py
│   │   │
│   │   ├── ai/
│   │   │   ├── providers/
│   │   │   │   ├── base.py
│   │   │   │   ├── openai_provider.py
│   │   │   │   ├── gemini_provider.py
│   │   │   │   └── local_provider.py
│   │   │   ├── prompts/
│   │   │   │   ├── prescription_extraction.md
│   │   │   │   ├── lab_report_extraction.md
│   │   │   │   └── simplification.md
│   │   │   ├── validators/
│   │   │   │   ├── medication_validator.py
│   │   │   │   ├── biomarker_validator.py
│   │   │   │   └── safety_validator.py
│   │   │   └── schemas/
│   │   │       ├── extracted_prescription.py
│   │   │       ├── extracted_lab_report.py
│   │   │       └── simplified_text.py
│   │   │
│   │   ├── ocr/
│   │   │   ├── providers/
│   │   │   │   ├── base.py
│   │   │   │   ├── google_vision_provider.py
│   │   │   │   ├── azure_document_provider.py
│   │   │   │   └── tesseract_provider.py
│   │   │   ├── preprocessing.py
│   │   │   ├── pdf_converter.py
│   │   │   └── text_cleaner.py
│   │   │
│   │   ├── storage/
│   │   │   ├── base.py
│   │   │   ├── s3_storage.py
│   │   │   └── local_storage.py
│   │   │
│   │   ├── workers/
│   │   │   ├── celery_app.py
│   │   │   ├── document_tasks.py
│   │   │   ├── insight_tasks.py
│   │   │   └── cleanup_tasks.py
│   │   │
│   │   └── utils/
│   │       ├── time.py
│   │       ├── units.py
│   │       └── pagination.py
│   │
│   └── tests/
│       ├── conftest.py
│       ├── api/
│       ├── services/
│       ├── ai/
│       └── ocr/
│
├── infra/
│   ├── docker/
│   │   ├── postgres/
│   │   ├── redis/
│   │   ├── minio/
│   │   └── nginx/
│   ├── nginx/
│   │   └── nginx.conf
│   └── deployment/
│       ├── staging.md
│       └── production.md
│
├── contracts/
│   ├── openapi.json
│   ├── api-contract.md
│   └── postman_collection.json
│
└── .github/
    └── workflows/
        ├── backend-ci.yml
        └── android-ci.yml
```

---

## 6. Backend Module Structure

### 6.1 Auth Module

Responsibilities:

- User registration.
- Login.
- Token refresh.
- Logout/revoke refresh token.
- Password hashing.
- Optional biometric unlock on Android should remain local only.

Core endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/users/me
```

### 6.2 Documents Module

Responsibilities:

- Upload prescription/lab report.
- Store metadata.
- Store file in object storage.
- Create async processing job.
- Return job ID immediately.

Core endpoints:

```text
POST /api/v1/documents/upload
GET  /api/v1/documents
GET  /api/v1/documents/{document_id}
DELETE /api/v1/documents/{document_id}
```

### 6.3 Processing Jobs Module

Responsibilities:

- Track OCR/AI processing state.
- Allow Android to poll job status.
- Store error messages and confidence scores.

Core endpoints:

```text
GET /api/v1/jobs/{job_id}
GET /api/v1/jobs/{job_id}/result
```

Job states:

```text
queued
processing_ocr
processing_ai
needs_review
completed
failed
cancelled
```

### 6.4 Medication Module

Responsibilities:

- Store confirmed medicine list.
- Store dosage and timing.
- Store simplified instructions.
- Keep active/inactive medication state.
- Sync reminder plan with Android.

Core endpoints:

```text
GET    /api/v1/medications
POST   /api/v1/medications
GET    /api/v1/medications/{medication_id}
PATCH  /api/v1/medications/{medication_id}
DELETE /api/v1/medications/{medication_id}
POST   /api/v1/medications/confirm-extracted
```

### 6.5 Reminder Module

Responsibilities:

- Backend stores reminder schedule.
- Android schedules exact local reminders.
- Android sends adherence logs back to backend.

Core endpoints:

```text
GET  /api/v1/reminders
POST /api/v1/reminders/log
GET  /api/v1/reminders/adherence-summary
```

### 6.6 Vitals Module

Responsibilities:

- Store manual vitals.
- Store lab-extracted biomarker values.
- Store Health Connect synced records.
- Preserve data source/provenance.

Core endpoints:

```text
GET  /api/v1/vitals
POST /api/v1/vitals
POST /api/v1/vitals/bulk-sync
GET  /api/v1/vitals/trends
```

### 6.7 Insights Module

Responsibilities:

- Generate simplified health summaries.
- Generate non-diagnostic lifestyle tips.
- Generate timeframe-based progress reports.

Core endpoints:

```text
POST /api/v1/insights/generate
GET  /api/v1/insights
GET  /api/v1/insights/{insight_id}
```

---

## 7. Initial Database Tables

### 7.1 Core Tables

```text
users
refresh_tokens
documents
processing_jobs
medications
medication_schedules
reminder_logs
vital_records
biomarkers
insights
audit_logs
```

### 7.2 Suggested Table Details

#### users

```text
id
full_name
email
password_hash
date_of_birth nullable
phone nullable
emergency_contact_name nullable
emergency_contact_phone nullable
created_at
updated_at
```

#### documents

```text
id
user_id
document_type prescription|lab_report|unknown
original_filename
storage_key
mime_type
file_size
status uploaded|processing|needs_review|completed|failed
created_at
updated_at
```

#### processing_jobs

```text
id
document_id
user_id
job_type prescription_extraction|lab_report_extraction|insight_generation
status queued|processing_ocr|processing_ai|needs_review|completed|failed
confidence_score nullable
error_message nullable
raw_ocr_text nullable
structured_result_json jsonb
created_at
updated_at
completed_at nullable
```

#### medications

```text
id
user_id
source_document_id nullable
name
normalized_name nullable
strength nullable
dosage_instruction
simplified_instruction
purpose_simplified nullable
start_date nullable
end_date nullable
status active|paused|completed|cancelled
confidence_score nullable
requires_review boolean
created_at
updated_at
```

#### medication_schedules

```text
id
medication_id
user_id
time_of_day
frequency_type daily|weekly|custom
days_of_week nullable
meal_relation before_food|after_food|with_food|no_relation|unknown
dose_amount nullable
notes nullable
created_at
updated_at
```

#### reminder_logs

```text
id
user_id
medication_id
scheduled_at
action taken|skipped|missed|snoozed
action_at nullable
created_at
```

#### vital_records

```text
id
user_id
metric_type heart_rate|blood_pressure_systolic|blood_pressure_diastolic|blood_glucose|custom
metric_name
value_numeric
unit
recorded_at
source manual|lab_report|health_connect|device|backend_import
source_document_id nullable
notes nullable
created_at
```

#### biomarkers

```text
id
user_id
source_document_id
name
normalized_name
value_numeric nullable
value_text nullable
unit nullable
reference_range_text nullable
status low|normal|high|unknown
recorded_at
confidence_score nullable
created_at
```

#### insights

```text
id
user_id
insight_type trend_report|health_tip|medication_summary|lab_summary
content_simple
content_structured_json jsonb
source_range_start nullable
source_range_end nullable
created_at
```

#### audit_logs

```text
id
user_id nullable
action
entity_type
entity_id nullable
metadata jsonb
created_at
```

---

## 8. Android Module Structure

### 8.1 Core Layer

Contains shared infrastructure:

- Network client.
- Database.
- Design system.
- Notification manager.
- Alarm scheduler.
- DataStore.
- Security/token storage.
- Health Connect manager.
- Common error/result classes.

### 8.2 Feature Modules

Each feature should follow:

```text
feature_x/
├── data/
│   ├── local/
│   ├── remote/
│   ├── mapper/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation/
    ├── screen/
    ├── component/
    ├── state/
    └── viewmodel/
```

### 8.3 Android Feature List

```text
feature_auth
feature_onboarding
feature_dashboard
feature_documents
feature_medications
feature_vitals
feature_insights
feature_settings
```

---

## 9. API Contract Strategy

Use FastAPI OpenAPI as the source of truth.

Recommended process:

1. Backend defines Pydantic request/response schemas.
2. FastAPI generates OpenAPI.
3. Save OpenAPI to `contracts/openapi.json`.
4. Android manually implements Retrofit interfaces first.
5. Later, consider generating API clients if the contract becomes large.

Example Android API style:

```kotlin
interface DocumentApi {
    @Multipart
    @POST("/api/v1/documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("document_type") documentType: RequestBody
    ): UploadDocumentResponse

    @GET("/api/v1/jobs/{jobId}")
    suspend fun getJobStatus(
        @Path("jobId") jobId: String
    ): ProcessingJobDto
}
```

---

## 10. Security & Privacy Requirements

Health data is sensitive. Treat the app as a privacy-first application from day one.

### 10.1 Backend Security

- Use HTTPS only in production.
- Hash passwords using Argon2id or bcrypt.
- Store refresh tokens securely and allow revocation.
- Use short-lived access tokens.
- Validate file type and size before upload processing.
- Scan uploaded files for suspicious content if possible.
- Store files privately in object storage.
- Never expose raw storage URLs publicly.
- Add audit logs for sensitive actions.
- Add rate limiting for login and upload endpoints.
- Do not log raw medical documents or extracted medical text in production logs.

### 10.2 Android Security

- Store tokens using Android Keystore-backed secure storage.
- Cache only necessary health data.
- Add optional biometric app lock.
- Clear sensitive temporary files after upload.
- Do not expose documents through public file paths.
- Use certificate pinning later if required.

### 10.3 AI Safety Rules

- The app must not claim to diagnose diseases.
- The app must not replace a doctor.
- Every AI explanation should be simple and cautious.
- Show confidence score where useful.
- Require user confirmation before activating extracted medicines/reminders.
- For urgent dangerous values, say: “This may need medical attention. Please contact a doctor.”
- Avoid medication interaction claims unless you integrate a verified medical database later.

---

## 11. Phase-by-Phase Development Plan

## Phase 0 — Project Setup & Architecture Lock

### Goal

Create a clean foundation before feature coding starts.

### Tasks

#### Repository

1. Create `medi-help/` monorepo.
2. Add `android/`, `backend/`, `docs/`, `infra/`, and `contracts/` folders.
3. Add root `.gitignore`.
4. Add root `README.md`.
5. Add `.env.example` for backend.
6. Add `docker-compose.yml` with PostgreSQL, Redis, MinIO, and backend placeholders.

#### Documentation

1. Add requirement analysis file under `docs/requirements/`.
2. Add `Implementation_Plan.md`.
3. Add architecture decision records under `docs/decisions/`.

#### Deliverables

- Repo created.
- Basic folder structure committed.
- Docker services start successfully.

---

## Phase 1 — Backend Foundation

### Goal

Build the FastAPI backend skeleton with database, auth, and health checks.

### Tasks

#### Backend Setup

1. Create Python virtual environment.
2. Install FastAPI, Uvicorn, SQLAlchemy, Alembic, Pydantic settings, asyncpg, Redis, Celery, pytest, Ruff, Black.
3. Create `backend/app/main.py`.
4. Add `/health` endpoint.
5. Add `/api/v1/router.py`.
6. Configure environment variables using `pydantic-settings`.
7. Configure async database session.
8. Configure Alembic migrations.

#### Auth

1. Create `users` table.
2. Create `refresh_tokens` table.
3. Implement password hashing.
4. Implement JWT access tokens.
5. Implement refresh token flow.
6. Add endpoints:
   - `POST /auth/register`
   - `POST /auth/login`
   - `POST /auth/refresh`
   - `GET /users/me`

#### Testing

1. Add API tests for register/login.
2. Add test database configuration.
3. Add CI lint/test command.

### Deliverables

- FastAPI runs locally.
- PostgreSQL connected.
- Alembic migrations working.
- Auth endpoints working.
- OpenAPI visible.

---

## Phase 2 — Android Foundation

### Goal

Build the Android app shell with Compose, navigation, auth screens, local DB, and API connectivity.

### Tasks

#### Project Setup

1. Create Android Studio project in `android/`.
2. Use Kotlin + Jetpack Compose.
3. Add Gradle Kotlin DSL.
4. Add dependencies:
   - Compose Material 3
   - Navigation Compose
   - Hilt
   - Retrofit
   - OkHttp
   - Room
   - DataStore
   - WorkManager
   - Coil

#### App Foundation

1. Create `MediHelpApp.kt`.
2. Create `MainActivity.kt`.
3. Add app theme.
4. Add navigation graph.
5. Add common UI components:
   - Large primary button
   - Simple top bar
   - Loading state
   - Error state
   - Empty state
   - Confirmation dialog

#### Auth UI

1. Create onboarding screen.
2. Create login screen.
3. Create register screen.
4. Connect login/register to backend.
5. Store token securely.
6. Navigate to dashboard after login.

#### Local Storage

1. Add Room database.
2. Add DataStore preferences.
3. Add token storage wrapper.

### Deliverables

- Android app launches.
- User can register/login.
- Token persists securely.
- Dashboard placeholder screen is visible.

---

## Phase 3 — Medication Management MVP

### Goal

Implement manual medication management and reliable local reminders before AI automation.

### Why This Comes Before OCR

Medication reminders are one of the highest-value features. They must work even before document extraction is ready.

### Backend Tasks

1. Create `medications` table.
2. Create `medication_schedules` table.
3. Create medication CRUD endpoints.
4. Create reminder schedule endpoints.
5. Add validation for medication status.

### Android Tasks

1. Create Medication Dashboard screen.
2. Create Add Medication screen.
3. Create Medication Detail screen.
4. Create schedule selector:
   - Daily
   - Specific time
   - Before/after food
   - Start/end date
5. Save medication locally first.
6. Sync medication to backend.
7. Schedule exact local reminders using AlarmManager.
8. Show notification when reminder triggers.
9. Add actions:
   - Taken
   - Skip
   - Snooze
10. Store reminder logs locally.
11. Sync reminder logs to backend.

### Deliverables

- User can manually add medicines.
- User receives local medicine reminders.
- User can mark medicine as taken/skipped.
- Backend stores medication records.

---

## Phase 4 — Document Upload System

### Goal

Allow users to upload prescriptions and lab reports and track processing status.

### Backend Tasks

1. Create `documents` table.
2. Create `processing_jobs` table.
3. Create object storage service interface.
4. Add local storage for development.
5. Add S3/MinIO storage implementation.
6. Add `POST /documents/upload` endpoint.
7. Validate file type:
   - PDF
   - JPEG
   - PNG
8. Validate file size.
9. Store file in object storage.
10. Create processing job.
11. Return job ID immediately.
12. Add `GET /jobs/{job_id}` endpoint.

### Android Tasks

1. Create Upload Document screen.
2. Add document type selector:
   - Prescription
   - Lab report
   - Not sure
3. Add file picker.
4. Add camera capture with CameraX.
5. Add document preview.
6. Upload file using multipart request.
7. Show processing status screen.
8. Poll job status using WorkManager or screen-level refresh.
9. Cache uploaded document metadata in Room.

### Deliverables

- User can upload PDF/image.
- Backend stores file.
- Processing job is created.
- Android shows job status.

---

## Phase 5 — OCR & AI Extraction Pipeline

### Goal

Extract structured medicine and lab data from uploaded documents.

### Backend Tasks

#### OCR

1. Implement OCR provider interface.
2. Implement local fallback OCR provider.
3. Implement production OCR provider adapter.
4. Add PDF-to-image conversion.
5. Add image preprocessing:
   - Rotation correction
   - Contrast improvement
   - Noise reduction
6. Store raw OCR text in processing job.

#### AI Extraction

1. Create Pydantic schema for prescription extraction.
2. Create Pydantic schema for lab report extraction.
3. Create prompt for prescription extraction.
4. Create prompt for lab report extraction.
5. Force structured JSON output.
6. Validate AI output.
7. Add confidence scoring.
8. Store structured result in `processing_jobs.structured_result_json`.
9. Set job status to `needs_review`.

#### Safety

1. Flag uncertain medicine names.
2. Flag missing dosage/frequency.
3. Flag unknown lab units.
4. Prevent automatic activation of medicines before user confirmation.

### Android Tasks

1. Create Extracted Prescription Review screen.
2. Create Extracted Lab Report Review screen.
3. Show confidence warnings.
4. Allow user to edit extracted data.
5. Allow user to confirm selected medicines/biomarkers.
6. Send confirmation to backend.

### Deliverables

- Backend extracts structured data.
- Android shows editable review screen.
- User confirms data before it affects dashboard/reminders.

---

## Phase 6 — Prescription-to-Medication Automation

### Goal

Route confirmed prescription extraction results into the Medication Dashboard and Reminder System.

### Backend Tasks

1. Add endpoint `POST /medications/confirm-extracted`.
2. Convert extracted medicines into medication records.
3. Convert extracted schedules into medication schedules.
4. Store source document reference.
5. Store simplified instructions.
6. Return created medication list to Android.

### Android Tasks

1. After user confirms prescription results, insert medicines into Room.
2. Schedule local reminders for each medicine.
3. Show success screen:
   - Medicines added
   - Reminders scheduled
4. Allow user to disable any reminder.
5. Update dashboard instantly.

### Deliverables

- Prescription upload can create medicines automatically.
- Reminders are scheduled after user confirmation.
- Medication dashboard updates immediately.

---

## Phase 7 — Health Chart & Manual Vitals Tracker

### Goal

Implement manual vitals tracking and charts with data provenance.

### Backend Tasks

1. Create `vital_records` table.
2. Create `biomarkers` table.
3. Add `POST /vitals` endpoint.
4. Add `POST /vitals/bulk-sync` endpoint.
5. Add `GET /vitals` endpoint with filters:
   - metric type
   - date range
   - source
6. Add `GET /vitals/trends` endpoint.

### Android Tasks

1. Create Vitals Dashboard screen.
2. Create Add Vital screen.
3. Add supported manual metrics:
   - Blood pressure
   - Heart rate
   - Blood glucose
   - Weight optional
   - Custom biomarker
4. Store manual values in Room immediately.
5. Display charts.
6. Show source label for every point:
   - Manual
   - Lab Report
   - Health Connect
7. Sync local vitals to backend.

### Deliverables

- User can manually log vitals.
- Vitals chart works locally.
- Data source is visible.
- Backend stores vital history.

---

## Phase 8 — Lab Report-to-Health Chart Automation

### Goal

Route confirmed lab report extraction results into the Health Chart.

### Backend Tasks

1. Add lab result confirmation endpoint.
2. Convert extracted biomarkers into `biomarkers` and/or `vital_records`.
3. Normalize common names:
   - HbA1c
   - Fasting glucose
   - LDL
   - HDL
   - Triglycerides
   - Hemoglobin
   - Creatinine
4. Store units and reference ranges.
5. Store source document reference.

### Android Tasks

1. Show extracted lab values in review screen.
2. Allow edit before confirmation.
3. Add confirmed values to local Room.
4. Update Health Chart.
5. Show Lab Report as source.

### Deliverables

- Lab report upload can populate Health Chart.
- User can review before saving.
- Biomarker history is visible.

---

## Phase 9 — Health Connect Integration

### Goal

Sync supported health data from Android Health Connect.

### Backend Tasks

1. Add source type `health_connect`.
2. Add bulk sync endpoint for wearable records.
3. Deduplicate records using timestamp + source + metric type.

### Android Tasks

1. Add Health Connect availability check.
2. Add permission request screen.
3. Request only required permissions.
4. Read supported data:
   - Heart rate
   - Blood pressure if available
   - Blood glucose if available
5. Store readings in Room.
6. Display readings on Vitals screen.
7. Sync readings to backend.
8. Add settings toggle to disable wearable sync.

### Deliverables

- User can connect Health Connect.
- App reads permitted health records.
- Wearable data appears in charts.
- Data source is shown as Health Connect.

---

## Phase 10 — Medical Simplification Engine

### Goal

Convert extracted medical terms into simple, friendly language.

### Backend Tasks

1. Create simplification prompt.
2. Use target reading style: understandable by a 10-year-old.
3. Add safety validator to remove diagnosis claims.
4. Add outputs for:
   - Medicine purpose
   - How to take medicine
   - Lab marker explanation
   - General meaning of high/low result
5. Cache simplified text in database.

### Android Tasks

1. Add simplified medicine explanation on Medicine Detail screen.
2. Add simplified lab marker explanation on Biomarker Detail screen.
3. Add “More details” expandable section.
4. Add “Ask your doctor” warning for uncertain/serious explanations.

### Deliverables

- Medicines have simple explanations.
- Lab values have simple explanations.
- Explanations are safe and non-diagnostic.

---

## Phase 11 — Medical History Analyzer & Health Tips

### Goal

Generate personalized, safe, easy-to-understand health summaries.

### Backend Tasks

1. Create `insights` table.
2. Implement insight generation job.
3. Analyze user data by timeframe:
   - Last 7 days
   - Last 30 days
   - Since last lab report
4. Generate summaries:
   - Medication adherence
   - Blood pressure trend
   - Glucose trend
   - Lab marker change
5. Generate lifestyle tips.
6. Add safety filter.
7. Store insight with source data range.

### Android Tasks

1. Create Insights screen.
2. Show cards:
   - Progress summary
   - Medication adherence
   - Health tips
   - Lab trend summary
3. Add refresh/generate button.
4. Show generated date and data range.
5. Add disclaimer.

### Deliverables

- User sees simple health summaries.
- Tips are personalized but safe.
- Insights are generated from stored user data.

---

## Phase 12 — Accessibility & Elderly-Friendly UX Polish

### Goal

Make the app comfortable for elderly and low-health-literacy users.

### Android Tasks

1. Add large text mode.
2. Add high-contrast mode.
3. Use big touch targets.
4. Avoid crowded screens.
5. Add clear icons and labels.
6. Add simple language everywhere.
7. Add voice-read option later if possible.
8. Add confirmation dialogs for destructive actions.
9. Add emergency contact display option.

### Deliverables

- App is easier for elderly users.
- Important actions are clear.
- UI is readable and accessible.

---

## Phase 13 — QA, Testing, and Safety Validation

### Goal

Stabilize the app before beta release.

### Backend Testing

1. Auth endpoint tests.
2. Medication CRUD tests.
3. Vitals CRUD tests.
4. Document upload tests.
5. OCR mock tests.
6. AI structured output validation tests.
7. Job failure/retry tests.
8. Security tests for unauthorized access.

### Android Testing

1. ViewModel unit tests.
2. Repository tests.
3. Room DAO tests.
4. Reminder scheduling tests.
5. Compose UI tests.
6. Offline mode tests.
7. Token expiration tests.
8. Upload retry tests.

### Manual QA

1. Upload clear prescription.
2. Upload blurry prescription.
3. Upload lab report with tables.
4. Add manual BP.
5. Add manual glucose.
6. Confirm extracted medicine.
7. Confirm extracted biomarker.
8. Receive reminder.
9. Mark reminder as taken.
10. Disable Health Connect sync.

### Deliverables

- Test suite passing.
- Major flows manually verified.
- AI extraction failure cases handled gracefully.

---

## Phase 14 — Deployment & Beta Release

### Goal

Deploy backend and release Android beta.

### Backend Deployment Tasks

1. Create production Docker image.
2. Configure production PostgreSQL.
3. Configure Redis.
4. Configure object storage.
5. Add HTTPS reverse proxy.
6. Configure environment variables.
7. Run migrations.
8. Enable logging and error tracking.
9. Configure backups.
10. Add admin-only monitoring endpoints.

### Android Release Tasks

1. Create release build variant.
2. Add app icon.
3. Add privacy policy.
4. Add terms/disclaimer screen.
5. Configure ProGuard/R8.
6. Test signed APK/AAB.
7. Release to internal testing.
8. Collect feedback.

### Deliverables

- Backend deployed.
- Android internal testing build ready.
- Privacy policy and disclaimer included.

---

## 12. MVP Scope Recommendation

To avoid overbuilding, the first MVP should include only:

1. User auth.
2. Manual medication management.
3. Local medication reminders.
4. Manual vitals logging.
5. Vitals chart.
6. Document upload.
7. OCR/AI extraction for prescriptions.
8. User review before saving extracted medicines.
9. Basic lab report extraction.
10. Basic simplified explanation.

Defer these until after MVP:

1. Advanced personalized tips.
2. Medication interaction checking.
3. Telehealth.
4. Sleep tracking.
5. Multi-caregiver accounts.
6. Doctor portal.
7. Insurance/clinic integrations.

---

## 13. Recommended Build Order

Use this exact order to reduce complexity:

```text
1. Backend health check + auth
2. Android shell + auth
3. Medication CRUD
4. Local reminders
5. Manual vitals
6. Vitals chart
7. Document upload
8. Async job status
9. OCR extraction
10. AI structured extraction
11. Review and confirm extracted data
12. Prescription → medications
13. Lab report → vitals/biomarkers
14. Health Connect integration
15. Insights and tips
16. Accessibility polish
17. QA and beta release
```

---

## 14. Important Engineering Rules

1. **Never let AI directly change medication reminders without user confirmation.**
2. **Medication reminders must work offline.**
3. **Dashboard should load from local Room cache first.**
4. **Every vital/biomarker record must have source and date.**
5. **Backend document processing must be async.**
6. **Do not store raw medical text in logs.**
7. **Use strict JSON schemas for AI output.**
8. **Make OCR/AI providers replaceable.**
9. **Use simple language in all user-facing health explanations.**
10. **Do not make diagnosis claims.**

---

## 15. First Sprint Checklist

### Backend

- [ ] Create FastAPI project.
- [ ] Add `/health` endpoint.
- [ ] Add PostgreSQL connection.
- [ ] Add Alembic.
- [ ] Add users table.
- [ ] Add register/login endpoints.
- [ ] Add Docker Compose services.

### Android

- [ ] Create Android project.
- [ ] Add Compose Material 3.
- [ ] Add navigation.
- [ ] Add Hilt.
- [ ] Add Retrofit.
- [ ] Add Room.
- [ ] Add DataStore.
- [ ] Create onboarding/login/register screens.
- [ ] Connect login to backend.

### Documentation

- [ ] Add API contract notes.
- [ ] Add architecture decision records.
- [ ] Add `.env.example`.
- [ ] Add local setup instructions.

---

## 16. Final Stack Summary

### Frontend

```text
Kotlin
Jetpack Compose
Material 3
MVVM + Clean Architecture
ViewModel + StateFlow
Hilt
Retrofit + OkHttp
Room
DataStore
WorkManager
AlarmManager
Health Connect SDK
CameraX
Coil
Vico charts
```

### Backend

```text
Python
FastAPI
Pydantic v2
SQLAlchemy 2.x Async ORM
Alembic
PostgreSQL
Redis
Celery
S3-compatible object storage
OCR provider abstraction
LLM provider abstraction
Docker
pytest
Ruff + Black + mypy
```

### Infrastructure

```text
Docker Compose for local development
PostgreSQL database
Redis queue/cache
MinIO local object storage
Nginx/Caddy reverse proxy later
GitHub Actions CI
Sentry/logging optional
```

---

## 17. Next Action

Start with **Phase 0** and **Phase 1**:

1. Create the repository.
2. Create backend FastAPI skeleton.
3. Add Docker Compose with PostgreSQL, Redis, and MinIO.
4. Add auth tables and endpoints.
5. Then create Android Compose shell and connect auth.

After this foundation is stable, build the medication reminder module before AI document extraction.
