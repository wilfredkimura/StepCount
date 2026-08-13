"""
DailySteps Database Model.
Stores historical daily step counts and active step goals for each user in NeonDB.
"""

from datetime import datetime
from typing import TYPE_CHECKING
from sqlalchemy import DateTime, ForeignKey, Integer, String, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base

if TYPE_CHECKING:
    from app.models.user import User


class DailySteps(Base):
    __tablename__ = "daily_steps"

    # Unique record identifier (autoincrements in PostgreSQL and SQLite)
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    
    # Foreign key referencing the user who logged these steps
    user_id: Mapped[str] = mapped_column(
        String(128),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    
    # Date of the step count in YYYY-MM-DD format
    date: Mapped[str] = mapped_column(String(10), nullable=False, index=True)
    
    # Total recorded steps for that day
    steps: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    
    # Goal that was active on that specific day (for historical accuracy)
    goal: Mapped[int] = mapped_column(Integer, default=8000, nullable=False)
    
    # Creation and modification timestamps
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

    # Relationship back to the parent user
    user: Mapped["User"] = relationship("User", back_populates="steps")

    # Ensure each user has at most one record per date for clean, idempotent synchronization
    __table_args__ = (
        UniqueConstraint("user_id", "date", name="uq_user_daily_step"),
    )
