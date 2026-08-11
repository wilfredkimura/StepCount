"""
User Profile Business Service.
Handles profile retrieval and goal updates.
"""

from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User
from app.schemas.user import UserProfileDto, UserProfileUpdateDto


class UserService:
    """
    Manages user profile operations in NeonDB.
    """

    @staticmethod
    async def get_profile(user: User) -> UserProfileDto:
        """
        Returns the user's profile information.
        """
        return UserProfileDto(
            user_id=user.id,
            email=user.email,
            name=user.name,
            daily_goal=user.daily_goal
        )

    @staticmethod
    async def update_profile(
        db: AsyncSession,
        user: User,
        payload: UserProfileUpdateDto
    ) -> UserProfileDto:
        """
        Updates the user's display name and/or daily step goal.
        """
        if payload.name is not None and payload.name.strip():
            user.name = payload.name.strip()
        if payload.daily_goal is not None:
            user.daily_goal = payload.daily_goal

        await db.flush()
        await db.refresh(user)

        return UserProfileDto(
            user_id=user.id,
            email=user.email,
            name=user.name,
            daily_goal=user.daily_goal
        )
