"""
Authentication Endpoints.
Handles Firebase token synchronization and user session events.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.auth import FirebaseLoginRequestDto, LogoutResponseDto
from app.schemas.user import UserProfileDto

router = APIRouter()


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
    # If client passed updated name, update it in the database record
    if payload.name and payload.name != current_user.name:
        current_user.name = payload.name
        await db.flush()
        await db.refresh(current_user)

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
    return LogoutResponseDto(message="User logged out successfully.")
