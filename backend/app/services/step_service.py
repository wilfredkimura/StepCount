import logging
from datetime import date
from typing import List, Optional
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import ResourceNotFoundException
from app.models.daily_steps import DailySteps
from app.models.user import User
from app.schemas.steps import StepResponseDto, StepUploadRequestDto, StreakResponseDto

logger = logging.getLogger("stepcount.steps")


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

        logger.info(f"[DATA SYNC] User '{user.email}' ({user.id}) synced steps: {record.steps} for date {record.date} (Goal: {record.goal})")

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

    @staticmethod
    async def get_user_streak(
        db: AsyncSession,
        user: User,
        reference_date_str: Optional[str] = None
    ) -> StreakResponseDto:
        """
        Computes current streak, best streak, and total goal met days for the user.
        """
        from datetime import datetime, timedelta

        stmt = select(DailySteps).where(DailySteps.user_id == user.id).order_by(DailySteps.date.asc())
        result = await db.execute(stmt)
        records = result.scalars().all()

        if not records:
            return StreakResponseDto(current_streak=0, best_streak=0, total_goal_days=0)

        record_map = {r.date: r for r in records}
        total_goal_days = sum(1 for r in records if r.steps >= r.goal)

        ref_date = date.today()
        if reference_date_str:
            try:
                ref_date = datetime.strptime(reference_date_str, "%Y-%m-%d").date()
            except ValueError:
                pass

        today_str = ref_date.isoformat()
        yesterday_str = (ref_date - timedelta(days=1)).isoformat()

        today_rec = record_map.get(today_str)
        yesterday_rec = record_map.get(yesterday_str)

        # Current streak calculation
        current_streak = 0
        if today_rec and today_rec.steps >= today_rec.goal:
            current_streak = 1
            check_date = ref_date - timedelta(days=1)
            while check_date.isoformat() in record_map and record_map[check_date.isoformat()].steps >= record_map[check_date.isoformat()].goal:
                current_streak += 1
                check_date -= timedelta(days=1)
        elif yesterday_rec and yesterday_rec.steps >= yesterday_rec.goal:
            current_streak = 1
            check_date = ref_date - timedelta(days=2)
            while check_date.isoformat() in record_map and record_map[check_date.isoformat()].steps >= record_map[check_date.isoformat()].goal:
                current_streak += 1
                check_date -= timedelta(days=1)

        # Best streak calculation
        met_dates = sorted([datetime.strptime(r.date, "%Y-%m-%d").date() for r in records if r.steps >= r.goal])
        best_streak = 0
        current_seq = 0
        prev_date = None

        for d in met_dates:
            if prev_date is None:
                current_seq = 1
            else:
                if d == prev_date + timedelta(days=1):
                    current_seq += 1
                else:
                    current_seq = 1
            if current_seq > best_streak:
                best_streak = current_seq
            prev_date = d

        if current_streak > best_streak:
            best_streak = current_streak

        return StreakResponseDto(
            current_streak=current_streak,
            best_streak=best_streak,
            total_goal_days=total_goal_days
        )

