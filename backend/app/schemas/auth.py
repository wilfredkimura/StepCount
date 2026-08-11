"""
Authentication Schemas.
Handles payload validation for Firebase login synchronization and logout.
"""

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class FirebaseLoginRequestDto(BaseModel):
    """Payload sent by the Android client to synchronize a Firebase authenticated user."""
    email: EmailStr = Field(..., description="User email address")
    name: str = Field(default="StepCount User", min_length=1, max_length=255, description="User display name")

    model_config = ConfigDict(from_attributes=True)


class LogoutResponseDto(BaseModel):
    """Response returned when user logs out."""
    message: str = "Successfully logged out"
    
    model_config = ConfigDict(from_attributes=True)
