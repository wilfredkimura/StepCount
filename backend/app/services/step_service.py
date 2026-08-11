"""
Daily Steps Business Service.
Handles database operations for logging, updating, retrieving, and deleting daily step records.
"""

from datetime import date
from typing import List, Optional
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ResourceNotFoundException
from app.models.daily_steps import DailySteps
from app.models.user import User
from app.schemas.steps import StepResponseDto, StepUploadRequestDto


class StepService:
    """
    Manages daily step count database transactions.
    """

    @staticmethod
    async def upsert_daily_steps(
        db: AsyncSession,
        user: User,
        payload: StepUploadRequestDto
    ) -> StepResponseDto:
        """
        Idempotently inserts or updates a daily step record for a user.
        If a record already exists for the given date, it updates the steps and goal.
        """
        stmt = select(DailySteps).where(
            DailySteps.user_id == user.id,
            DailySteps.date == payload.date
        )
        result = await db.execute(stmt)
        record = result.scalar_one_or_none()

        if record is None:
            record = DailySteps(
                user_id=user.id,
                date=payload.date,
                steps=payload.steps,
                goal=payload.goal
            )
            db.add(record)
        else:
            record.steps = payload.steps
            record.goal = payload.goal

        await db.flush()
        await db.refresh(record)

        return StepResponseDto(
            id=record.id,
            user_id=record.user_id,
            date=record.date,
            steps=record.steps,
            goal=record.goal
        )

    @staticmethod
    async def get_today_steps(
        db: AsyncSession,
        user: User
    ) -> StepResponseDto:
        """
        Retrieves today's step count record for the current user.
        If no steps have been recorded yet today, returns a default 0-step response.
        """
        today_str = date.today().isoformat()
        stmt = select(DailySteps).where(
            DailySteps.user_id == user.id,
            DailySteps.date == today_str
        )
        result = await db.execute(stmt)
        record = result.scalar_one_or_none()

        if record is None:
            # Return virtual today record with 0 steps
            return StepResponseDto(
                id=0,
                user_id=user.id,
                date=today_str,
                steps=0,
                goal=user.daily_goal
            )

        return StepResponseDto(
            id=record.id,
            user_id=record.user_id,
            date=record.date,
            steps=record.steps,
            goal=record.goal
        )

    @staticmethod
    async def get_step_history(
        db: AsyncSession,
        user: User,
        limit: int = 100
    ) -> List[StepResponseDto]:
        """
        Retrieves user's complete chronological step history, sorted by newest date first.
        """
        stmt = (
            select(DailySteps)
            .where(DailySteps.user_id == user.id)
            .order_by(DailySteps.date.desc())
            .limit(limit)
        )
        result = await db.execute(stmt)
        records = result.scalars().all()

        return [
            StepResponseDto(
                id=r.id,
                user_id=r.user_id,
                date=r.date,
                steps=r.steps,
                goal=r.goal
            )
            for r in records
        ]

    @staticmethod
    async def update_steps_for_date(
        db: AsyncSession,
        user: User,
        date_str: str,
        payload: StepUploadRequestDto
    ) -> StepResponseDto:
        """
        Updates step counts and goal for a specific date.
        """
        stmt = select(DailySteps).where(
            DailySteps.user_id == user.id,
            DailySteps.date == date_str
        )
        result = await db.execute(stmt)
        record = result.scalar_one_or_none()

        if record is None:
            record = DailySteps(
                user_id=user.id,
                date=date_str,
                steps=payload.steps,
                goal=payload.goal
            )
            db.add(record)
        else:
            record.steps = payload.steps
            record.goal = payload.goal

        await db.flush()
        await db.refresh(record)

        return StepResponseDto(
            id=record.id,
            user_id=record.user_id,
            date=record.date,
            steps=record.steps,
            goal=record.goal
        )

    @staticmethod
    async def delete_steps_for_date(
        db: AsyncSession,
        user: User,
        date_str: str
    ) -> None:
        """
        Deletes the step record for a specified date.
        """
        stmt = delete(DailySteps).where(
            DailySteps.user_id == user.id,
            DailySteps.date == date_str
        )
        result = await db.execute(stmt)
        if result.rowcount == 0:
            raise ResourceNotFoundException(f"No step record found for date {date_str}")
