"""
NeonDB PostgreSQL Connection Verification Script.
Tests direct asynchronous database connectivity, table initialization, and CRUD queries against remote NeonDB.
"""

import asyncio
import os
import sys

# Add current directory to path
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from app.core.config import settings
from app.db.base import Base
from app.db.session import engine, async_session_factory
from app.models.user import User
from app.models.daily_steps import DailySteps
from sqlalchemy import select, text


async def test_connection():
    print("=" * 60)
    print("Connecting to NeonDB PostgreSQL...")
    print(f"DATABASE_URL: {settings.DATABASE_URL.split('@')[-1] if '@' in settings.DATABASE_URL else 'Hidden'}")
    print("=" * 60)

    try:
        # 1. Test basic SQL execution
        async with engine.connect() as conn:
            result = await conn.execute(text("SELECT version();"))
            version = result.scalar()
            print(f"[SUCCESS] Connected to NeonDB!")
            print(f"PostgreSQL Version: {version}\n")

        # 2. Create tables if not exist
        print("Verifying database schema tables ('users', 'daily_steps')...")
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("[SUCCESS] Database tables verified.\n")

        # 3. Test insert & query
        print("Testing insert and query on NeonDB...")
        async with async_session_factory() as session:
            test_uid = "neondb_test_runner"
            
            # Check if exists
            stmt = select(User).where(User.id == test_uid)
            res = await session.execute(stmt)
            user = res.scalar_one_or_none()
            
            if not user:
                user = User(
                    id=test_uid,
                    email="testrunner@neondb.com",
                    name="NeonDB Test Runner",
                    daily_goal=10000
                )
                session.add(user)
                await session.commit()
                print(f"[SUCCESS] Inserted test user '{user.name}' into NeonDB.")
            else:
                print(f"[SUCCESS] Found existing test user '{user.name}' in NeonDB.")

        print("\n" + "=" * 60)
        print("ALL NEONDB TESTS PASSED! Your remote database is ready.")
        print("=" * 60)

    except Exception as e:
        print(f"\n[ERROR] Connection to NeonDB failed: {e}")
        print("\nPlease ensure your DATABASE_URL in backend/.env is correct and has '?ssl=require'.")
    finally:
        await engine.dispose()


if __name__ == "__main__":
    asyncio.run(test_connection())
