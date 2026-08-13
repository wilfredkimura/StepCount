"""
Security dependencies and authentication middleware.
Provides get_current_user dependency for protected FastAPI route handlers.
"""

import logging
from typing import Optional
from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import UnauthorizedException
from app.db.session import get_db
from app.models.user import User
from app.services.firebase_service import FirebaseService

logger = logging.getLogger("stepcount.auth")

# HTTP Bearer scheme for extracting Authorization header
http_bearer = HTTPBearer(auto_error=False)


async def get_current_user(
    auth_header: Optional[HTTPAuthorizationCredentials] = Depends(http_bearer),
    db: AsyncSession = Depends(get_db)
) -> User:
    """
    FastAPI dependency that extracts the Bearer token, verifies it against Firebase,
    and synchronizes/fetches the user record from NeonDB.
    """
    if not auth_header or not auth_header.credentials:
        raise UnauthorizedException("Missing Authorization Bearer token header.")

    id_token = auth_header.credentials
    
    # 1. Verify token with Firebase Admin SDK
    claims = FirebaseService.verify_token(id_token)
    user_id = claims.get("uid")
    if not user_id:
        raise UnauthorizedException("Firebase token does not contain a valid user ID (uid).")

    email = claims.get("email", f"{user_id}@example.com")
    name = claims.get("name") or claims.get("display_name") or "StepCount User"

    # 2. Lookup user in NeonDB
    stmt = select(User).where(User.id == user_id)
    result = await db.execute(stmt)
    user = result.scalar_one_or_none()

    # 3. Auto-provision user in database if not existing
    if user is None:
        user = User(
            id=user_id,
            email=email,
            name=name,
            daily_goal=8000
        )
        db.add(user)
        await db.flush()
        await db.refresh(user)
        logger.info(f"[NEW USER CREATED] ID: {user.id} | Email: {user.email} | Name: {user.name} | Goal: {user.daily_goal}")
    else:
        logger.info(f"[USER CONNECTED] ID: {user.id} | Email: {user.email} | Name: {user.name}")

    return user

