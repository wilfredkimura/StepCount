"""
Daily Steps Schemas.
Handles step upload, update, and history records.
"""

from pydantic import BaseModel, ConfigDict, Field, field_validator
import re


class StepUploadRequestDto(BaseModel):
    """Payload to upload or update daily step counts."""
    date: str = Field(..., description="Date formatted as YYYY-MM-DD")
    steps: int = Field(..., ge=0, le=1000000, description="Total steps recorded on this date")
    goal: int = Field(default=8000, ge=100, le=100000, description="Step goal active for this date")

    @field_validator("date")
    @classmethod
    def validate_date_format(cls, v: str) -> str:
        if not re.match(r"^\d{4}-\d{2}-\d{2}$", v):
            raise ValueError("Date must follow the format YYYY-MM-DD")
        return v

    model_config = ConfigDict(from_attributes=True)


class StepResponseDto(BaseModel):
    """Step record response returned to client."""
    id: int = Field(..., description="Unique record identifier")
    user_id: str = Field(..., description="Firebase User UID")
    date: str = Field(..., description="Date formatted as YYYY-MM-DD")
    steps: int = Field(..., description="Recorded steps count")
    goal: int = Field(..., description="Active step goal")

    model_config = ConfigDict(from_attributes=True)
