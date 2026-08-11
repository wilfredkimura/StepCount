"""
Leaderboard Schemas.
Handles rankings by step volume and goal achievement percentage.
"""

from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class LeaderboardItemDto(BaseModel):
    """An individual entry in the competitive leaderboard."""
    user_id: str = Field(..., description="Firebase User UID")
    name: str = Field(..., description="User display name")
    steps: int = Field(..., description="Total accumulated steps in the period")
    rank: int = Field(..., ge=1, description="Rank position (1, 2, 3...)")
    goal_percentage: Optional[float] = Field(default=0.0, description="Percentage of goal achieved (0-100+)")

    model_config = ConfigDict(from_attributes=True)


class UserRankDto(BaseModel):
    """Current user's personal rank standing."""
    user_id: str = Field(..., description="Firebase User UID")
    name: str = Field(..., description="User display name")
    steps: int = Field(..., description="Total accumulated steps in the period")
    rank: int = Field(..., ge=1, description="Rank position")
    goal_percentage: Optional[float] = Field(default=0.0, description="Percentage of goal achieved")
    total_participants: int = Field(default=1, description="Total active users in leaderboard")

    model_config = ConfigDict(from_attributes=True)
