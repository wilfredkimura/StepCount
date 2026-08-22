"""
Authentication Endpoints.
Handles Firebase token synchronization and user session events.
"""

import logging
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.auth import FirebaseLoginRequestDto, LogoutResponseDto, RegisterRequestDto
from app.schemas.user import UserProfileDto

logger = logging.getLogger("stepcount.auth")

router = APIRouter()


@router.post("/register", response_model=UserProfileDto, status_code=status.HTTP_201_CREATED, summary="Register User & Commit Profile to NeonDB")
async def register_user(
    payload: RegisterRequestDto,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> UserProfileDto:
    """
    Registers a new user in NeonDB PostgreSQL.
    Ensures that the user profile is explicitly committed and synchronized with Firebase Auth.
    """
    if payload.name:
        current_user.name = payload.name
    if payload.email:
        current_user.email = str(payload.email)
    if payload.daily_goal:
        current_user.daily_goal = payload.daily_goal

    await db.flush()
    await db.refresh(current_user)

    logger.info(f"[USER REGISTERED] User '{current_user.email}' ({current_user.id}) committed to NeonDB with goal {current_user.daily_goal}.")

    return UserProfileDto(
        user_id=current_user.id,
        email=current_user.email,
        name=current_user.name,
        daily_goal=current_user.daily_goal
    )


@router.post("/firebase-login", response_model=UserProfileDto, summary="Synchronize Firebase User with NeonDB")
async def firebase_login(
    payload: FirebaseLoginRequestDto,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> UserProfileDto:
    """
    Verifies the Firebase ID token in the Authorization header.
    Creates or updates the user profile in NeonDB, then returns the user profile.
    """
    updated = False
    if payload.name and payload.name != current_user.name:
        current_user.name = payload.name
        updated = True
    if payload.email and str(payload.email) != current_user.email:
        current_user.email = str(payload.email)
        updated = True

    if updated:
        await db.flush()
        await db.refresh(current_user)

    logger.info(f"[AUTH SYNC] User '{current_user.email}' ({current_user.id}) synchronized session.")

    return UserProfileDto(
        user_id=current_user.id,
        email=current_user.email,
        name=current_user.name,
        daily_goal=current_user.daily_goal
    )


@router.post("/logout", response_model=LogoutResponseDto, summary="User Logout")
async def logout(
    current_user: User = Depends(get_current_user)
) -> LogoutResponseDto:
    """
    Logs out the user and acknowledges session termination.
    """
    logger.info(f"[USER LOGOUT] User '{current_user.email}' ({current_user.id}) logged out.")
    return LogoutResponseDto(message="User logged out successfully.")

