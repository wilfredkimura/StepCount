"""
Motivation Quote Schemas.
Represents inspirational fitness quotes provided to the Android client.
"""

from pydantic import BaseModel, ConfigDict, Field


class MotivationDto(BaseModel):
    """Daily inspirational quote model."""
    quote: str = Field(..., description="Inspirational quote text")
    author: str = Field(default="Unknown", description="Quote author or attribution")

    model_config = ConfigDict(from_attributes=True)
