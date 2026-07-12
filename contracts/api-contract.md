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
```

Document processing endpoints:

```text
POST /api/v1/documents/upload
GET  /api/v1/jobs/{job_id}
```

`POST /documents/upload` accepts authenticated multipart form data with a
`document_type` value (`prescription`, `lab_report`, or `unknown`) and a `file`
value containing a PDF, JPEG, or PNG. It returns the stored document metadata
and a queued processing job immediately.
