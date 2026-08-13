"""
User Database Model.
Stores profile and authentication details for registered users in NeonDB.
"""

from datetime import datetime
from typing import List, TYPE_CHECKING
from sqlalchemy import DateTime, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base

if TYPE_CHECKING:
    from app.models.daily_steps import DailySteps


class User(Base):
    __tablename__ = "users"

    # Firebase User ID (UID string from Firebase Authentication)
    id: Mapped[str] = mapped_column(String(128), primary_key=True, index=True)
    
    # User email address
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    
    # User display name
    name: Mapped[str] = mapped_column(String(255), nullable=False, default="StepCount User")
    
    # Target daily step goal
    daily_goal: Mapped[int] = mapped_column(Integer, default=8000, nullable=False)
    
    # Creation and update timestamps
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False
    )

    # One-to-many relationship with daily step records (deleting user removes all steps)
    steps: Mapped[List["DailySteps"]] = relationship(
        "DailySteps",
        back_populates="user",
        cascade="all, delete-orphan",
        passive_deletes=True
    )
