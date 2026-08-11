"""
Database session management for NeonDB PostgreSQL.
Provides an asynchronous database engine and session dependency for FastAPI routes.
"""

from typing import AsyncGenerator
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession, async_sessionmaker, create_async_engine

from app.core.config import settings

# Configure engine arguments.
# For SQLite (in-memory tests), connect_args and pool settings differ from PostgreSQL.
is_sqlite = settings.DATABASE_URL.startswith("sqlite")

engine_kwargs = {
    "echo": settings.ENVIRONMENT == "development",
}

if not is_sqlite:
    # Optimized settings for NeonDB Serverless PostgreSQL
    engine_kwargs.update({
        "pool_size": settings.DB_POOL_SIZE,
        "max_overflow": settings.DB_MAX_OVERFLOW,
        "pool_timeout": settings.DB_POOL_TIMEOUT,
        "pool_recycle": settings.DB_POOL_RECYCLE,
        "pool_pre_ping": True,
    })

# Create asynchronous engine
engine: AsyncEngine = create_async_engine(settings.DATABASE_URL, **engine_kwargs)

# Create session maker for generating asynchronous sessions
async_session_factory = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    autocommit=False,
    autoflush=False,
    expire_on_commit=False,
)


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    FastAPI dependency that yields a database session per request.
    Automatically closes the session when the request finishes.
    """
    async with async_session_factory() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
