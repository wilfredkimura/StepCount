"""
Firebase to NeonDB PostgreSQL User Migration Script.

Why this script exists:
Earlier versions of the application created user accounts in Firebase Auth without
guaranteeing an immediate write to the PostgreSQL database. This migration script connects
to Firebase Admin SDK, retrieves all registered Firebase users, and inserts any missing
user records into PostgreSQL so their steps, streaks, and leaderboard rankings work properly.
"""

import asyncio
import logging
import os
import sys
from pathlib import Path

# Add backend directory to Python system path so app modules can be imported cleanly
CURRENT_DIR = Path(__file__).resolve().parent
BACKEND_DIR = CURRENT_DIR.parent
sys.path.insert(0, str(BACKEND_DIR))

from firebase_admin import auth as firebase_auth
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.db.session import async_session_factory
from app.models.user import User
from app.services.firebase_service import FirebaseService

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("stepcount.migration")


async def migrate_firebase_users() -> None:
    """
    Scans Firebase Authentication for all user accounts and ensures each user
    has a corresponding record in the NeonDB PostgreSQL database.
    """
    logger.info("Initializing Firebase Admin SDK connection...")
    FirebaseService.initialize()

    total_firebase_users = 0
    migrated_count = 0
    already_existing_count = 0

    logger.info("Connecting to NeonDB PostgreSQL database...")
    async with async_session_factory() as db:
        logger.info("Fetching registered user list from Firebase Auth...")
        
        # list_users() returns pages of users up to 1000 at a time
        page = firebase_auth.list_users()
        
        while page:
            for fb_user in page.users:
                total_firebase_users += 1
                uid = fb_user.uid
                email = fb_user.email or f"{uid}@firebase.local"
                name = fb_user.display_name or email.split("@")[0] or "StepCount User"

                # Check if this user ID already exists in PostgreSQL
                stmt = select(User).where(User.id == uid)
                result = await db.execute(stmt)
                existing_user = result.scalar_one_or_none()

                if existing_user is None:
                    # Create new user record
                    new_user = User(
                        id=uid,
                        email=email,
                        name=name,
                        daily_goal=8000
                    )
                    db.add(new_user)
                    migrated_count += 1
                    logger.info(f" [+] Provisioning new user: {email} (UID: {uid})")
                else:
                    already_existing_count += 1

            # Get next page if there are more users
            page = page.get_next_page()

        # Commit all newly inserted users to PostgreSQL
        if migrated_count > 0:
            await db.commit()
            logger.info(f"Database transaction committed successfully! Added {migrated_count} users.")
        else:
            logger.info("Database is already up to date. All Firebase users exist in PostgreSQL.")

    print("\n" + "=" * 55)
    print(" FIREBASE TO POSTGRESQL USER MIGRATION SUMMARY")
    print("=" * 55)
    print(f" Total Firebase Users Found:    {total_firebase_users}")
    print(f" Already Synced in PostgreSQL:  {already_existing_count}")
    print(f" Newly Migrated to PostgreSQL:  {migrated_count}")
    print("=" * 55 + "\n")


if __name__ == "__main__":
    asyncio.run(migrate_firebase_users())
