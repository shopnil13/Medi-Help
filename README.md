# Medi-Help

A local-first Android Medi-Help app with a FastAPI backend.

## Stack

- Android: Kotlin, Jetpack Compose, Material 3, Room, Retrofit, Hilt, WorkManager, AlarmManager, Health Connect
- Backend: Python, FastAPI, SQLAlchemy async, PostgreSQL, Redis, Celery, Alembic
- Storage: MinIO/S3-compatible object storage for uploaded prescriptions and lab reports

## Ground Zero Run

### 1. Start infrastructure

```bash
docker compose up -d postgres redis minio
```

To run the containerized API and OCR worker as well:

```bash
docker compose up -d --build
docker compose exec backend alembic upgrade head
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
{"status":"ok","service":"medi-help-api"}
```

### 4. Run the Android app

The app talks to the backend at `http://10.0.2.2:8000/` by default, which is
the Android emulator's alias for the host machine's `localhost` — so start
the backend (steps 1-2) first.

```bash
cd android
./gradlew :app:assembleDebug
```

Or open `android/` in Android Studio and run the `app` configuration on an
emulator or device. Debug builds allow plaintext HTTP to `10.0.2.2` and
`localhost` only (see `app/src/debug/res/xml/network_security_config_debug.xml`);
release builds require HTTPS.
