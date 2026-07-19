from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "Medi-Help API"
    app_env: str = "local"
    debug: bool = True
    api_v1_prefix: str = "/api/v1"

    database_url: str = Field(
        default="postgresql+asyncpg://health_user:health_password@localhost:5433/medi_help"
    )
    redis_url: str = "redis://localhost:6379/0"

    jwt_secret_key: str = "change-this-secret-in-development"
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    refresh_token_expire_days: int = 30

    local_storage_path: str = "./storage/uploads"
    max_upload_size_mb: int = 10
    storage_backend: str = "local"
    s3_endpoint_url: str = "http://localhost:9000"
    s3_access_key: str = "minioadmin"
    s3_secret_key: str = "minioadmin"
    s3_bucket: str = "medi-help-documents"
    s3_region: str = "us-east-1"

    ocr_backend: str = "tesseract"
    tesseract_command: str | None = None
    google_vision_api_key: str | None = None
    extraction_backend: str = "heuristic"
    simplification_backend: str = "heuristic"
    openai_api_key: str | None = None
    openai_api_url: str = "https://api.openai.com/v1"
    openai_model: str = "gpt-4.1-mini"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


@lru_cache
def get_settings() -> Settings:
    return Settings()
