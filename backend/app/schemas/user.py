"""
User Profile Schemas.
Handles profile retrieval and updates.
"""

from typing import Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field


class UserProfileDto(BaseModel):
    """Full user profile representation."""
    user_id: str = Field(..., description="Firebase User UID")
    email: EmailStr = Field(..., description="User email address")
    name: str = Field(..., description="User display name")
    daily_goal: int = Field(default=8000, ge=100, le=100000, description="Daily step target")

    model_config = ConfigDict(from_attributes=True, populate_by_name=True)


class UserProfileUpdateDto(BaseModel):
    """Payload to update user name and daily goal."""
    name: Optional[str] = Field(None, min_length=1, max_length=255, description="Updated name")
    daily_goal: Optional[int] = Field(None, ge=100, le=100000, description="Updated daily step goal")

    model_config = ConfigDict(from_attributes=True)
