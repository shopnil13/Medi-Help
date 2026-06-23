# Health Assistant

A local-first Android Health Assistant app with a FastAPI backend.

## Stack

- Android: Kotlin, Jetpack Compose, Material 3, Room, Retrofit, Hilt, WorkManager, AlarmManager, Health Connect
- Backend: Python, FastAPI, SQLAlchemy async, PostgreSQL, Redis, Celery, Alembic
- Storage: MinIO/S3-compatible object storage for uploaded prescriptions and lab reports

## Ground Zero Run

### 1. Start infrastructure

```bash
docker compose up -d postgres redis minio
```

### 2. Run backend locally

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

On Windows PowerShell:

```powershell
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
copy .env.example .env
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 3. Test backend

Open:

```text
http://localhost:8000/health
http://localhost:8000/docs
```

Expected `/health` response:

```json
{"status":"ok","service":"health-assistant-api"}
```
