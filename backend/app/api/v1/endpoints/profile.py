"""
User Profile Endpoints.
Provides routes to retrieve and modify user account profile and step goals.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.user import UserProfileDto, UserProfileUpdateDto
from app.services.user_service import UserService

router = APIRouter()


@router.get("", response_model=UserProfileDto, summary="Get user profile")
async def get_user_profile(
    current_user: User = Depends(get_current_user)
) -> UserProfileDto:
    """
    Returns the currently logged-in user's profile details.
    """
    return await UserService.get_profile(current_user)


@router.put("", response_model=UserProfileDto, summary="Update user profile")
async def update_user_profile(
    payload: UserProfileUpdateDto,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> UserProfileDto:
    """
    Updates the user's name and/or daily step target.
    """
    return await UserService.update_profile(db, current_user, payload)
