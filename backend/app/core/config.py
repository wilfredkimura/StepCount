"""
Application Configuration Settings.
Loads configuration from environment variables and .env file using Pydantic Settings.
"""

from typing import List, Optional
from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Application Info
    PROJECT_NAME: str = "StepCount Backend"
    ENVIRONMENT: str = "development"
    API_V1_STR: str = "/api"
    
    # NeonDB PostgreSQL Async Connection String
    DATABASE_URL: str = "postgresql+asyncpg://neondb_owner:password@localhost:5432/neondb"
    
    # Firebase Admin Credentials
    FIREBASE_CREDENTIALS_PATH: Optional[str] = "serviceAccountKey.json"
    FIREBASE_CREDENTIALS_JSON: Optional[str] = None
    FIREBASE_PROJECT_ID: Optional[str] = None
    
    # External Quotable API URL
    QUOTABLE_API_URL: str = "https://api.quotable.io/random?tags=fitness|inspirational"
    
    # CORS Allowed Origins
    CORS_ORIGINS: List[str] = ["*"]
    
    # Database connection pool settings (optimal for Neon serverless PostgreSQL)
    DB_POOL_SIZE: int = 10
    DB_MAX_OVERFLOW: int = 20
    DB_POOL_TIMEOUT: int = 30
    DB_POOL_RECYCLE: int = 1800

    @field_validator("DATABASE_URL", mode="before")
    @classmethod
    def assemble_db_connection(cls, v: Optional[str]) -> str:
        if not v:
            return "postgresql+asyncpg://neondb_owner:password@localhost:5432/neondb"
        # If the URL starts with postgres:// or postgresql:// without asyncpg, convert to postgresql+asyncpg://
        if v.startswith("postgres://"):
            v = v.replace("postgres://", "postgresql+asyncpg://", 1)
        elif v.startswith("postgresql://") and not v.startswith("postgresql+asyncpg://"):
            v = v.replace("postgresql://", "postgresql+asyncpg://", 1)
        return v

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="allow"
    )


# Global settings instance
settings = Settings()
