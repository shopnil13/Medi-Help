# API Contract Notes

Initial endpoints:

```text
GET /health
GET /api/v1/health
```

Authentication endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
GET  /api/v1/users/me
```

Medication and reminder endpoints:

```text
GET    /api/v1/medications
POST   /api/v1/medications
GET    /api/v1/medications/{medication_id}
PATCH  /api/v1/medications/{medication_id}
DELETE /api/v1/medications/{medication_id}
GET    /api/v1/reminders
POST   /api/v1/reminders/log
GET    /api/v1/reminders/adherence-summary
POST   /api/v1/medications/confirm-extracted
GET    /api/v1/vitals
POST   /api/v1/vitals
POST   /api/v1/vitals/bulk-sync
GET    /api/v1/vitals/trends
```

Document processing endpoints:

```text
POST /api/v1/documents/upload
GET  /api/v1/jobs/{job_id}
POST /api/v1/jobs/{job_id}/confirm
```

`POST /documents/upload` accepts authenticated multipart form data with a
`document_type` value (`prescription`, `lab_report`, or `unknown`) and a `file`
value containing a PDF, JPEG, or PNG. It returns the stored document metadata
and a queued processing job immediately.

The worker updates jobs through `queued`, `processing`, and `needs_review`.
Job responses include a discriminated `structured_result` for either a
prescription or lab report. The Android client may edit and select entries, then
submit the validated result to `/jobs/{job_id}/confirm`. Confirmation stores a
separate immutable snapshot and changes the job to `completed`; it does not
create medications or vital records until the later routing phases.

`POST /medications/confirm-extracted` accepts a confirmed prescription `job_id`.
It idempotently creates active medications and schedules only for selected
entries, returns the created medication list, and retains source document/job
references. Only explicit extracted times become schedules.

Vital writes include a metric type, numeric value, unit, recorded date, and
source. Blood pressure is represented as paired systolic and diastolic records.
`GET /vitals` accepts optional `metric_type`, `start_date`, `end_date`, and
`source` filters. `GET /vitals/trends` applies the same filters and groups points
by metric name and unit with minimum, maximum, average, latest, and direction
summaries. Lab-sourced records must reference a document owned by the user.
