# ADR-001: Tech Stack

## Decision

Use Kotlin + Jetpack Compose for Android and Python + FastAPI for the backend.

## Rationale

- Compose is a modern declarative UI framework for Android.
- FastAPI is suitable for typed, documented APIs and AI/OCR-heavy Python workflows.
- PostgreSQL stores structured health data.
- Redis and Celery handle async processing jobs.
- MinIO/S3-compatible storage stores uploaded medical documents.
