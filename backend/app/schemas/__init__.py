"""
Pydantic v2 validation schemas for StepCount API.
"""

from app.schemas.auth import FirebaseLoginRequestDto, LogoutResponseDto
from app.schemas.user import UserProfileDto, UserProfileUpdateDto
from app.schemas.steps import StepUploadRequestDto, StepResponseDto
from app.schemas.leaderboard import LeaderboardItemDto, UserRankDto
from app.schemas.motivation import MotivationDto

__all__ = [
    "FirebaseLoginRequestDto",
    "LogoutResponseDto",
    "UserProfileDto",
    "UserProfileUpdateDto",
    "StepUploadRequestDto",
    "StepResponseDto",
    "LeaderboardItemDto",
    "UserRankDto",
    "MotivationDto"
]
