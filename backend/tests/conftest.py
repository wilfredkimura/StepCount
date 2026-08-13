"""
Pytest Test Fixtures and Harness.
Provides in-memory async database sessions, dependency overrides, and authenticated HTTP clients for tests.
"""

from typing import AsyncGenerator
import pytest
from httpx import ASGITransport, AsyncClient
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.core.config import settings
from app.db.base import Base
from app.db.session import get_db
from app.main import app
from app.models.user import User

# Set test environment
settings.ENVIRONMENT = "test"

# In-memory async SQLite engine for isolated, fast unit and integration tests
TEST_DB_URL = "sqlite+aiosqlite:///:memory:"
test_engine = create_async_engine(TEST_DB_URL, echo=False)
test_session_factory = async_sessionmaker(
    bind=test_engine,
    class_=AsyncSession,
    autocommit=False,
    autoflush=False,
    expire_on_commit=False
)


@pytest.fixture(autouse=True)
async def prepare_database() -> AsyncGenerator[None, None]:
    """
    Creates fresh database schema before each test and drops it afterwards.
    """
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


@pytest.fixture
async def db_session() -> AsyncGenerator[AsyncSession, None]:
    """
    Yields an isolated database session for testing.
    """
    async with test_session_factory() as session:
        yield session


@pytest.fixture
async def test_user(db_session: AsyncSession) -> User:
    """
    Creates and returns a default test user in the database.
    """
    user = User(
        id="test_uid_123",
        email="testuser@example.com",
        name="Test User",
        daily_goal=8000
    )
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)
    return user


@pytest.fixture
async def client(db_session: AsyncSession) -> AsyncGenerator[AsyncClient, None]:
    """
    Provides an asynchronous HTTP test client with database dependency override.
    """
    async def override_get_db() -> AsyncGenerator[AsyncSession, None]:
        yield db_session

    app.dependency_overrides[get_db] = override_get_db

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac

    app.dependency_overrides.clear()
