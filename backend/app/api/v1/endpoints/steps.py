"""
Daily Steps API Endpoints.
Provides REST endpoints for step synchronization, history retrieval, updates, and deletion.
"""

from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.steps import StepResponseDto, StepUploadRequestDto, StreakResponseDto
from app.services.step_service import StepService

router = APIRouter()


@router.get("/streak", response_model=StreakResponseDto, summary="Get user streak stats")
async def get_user_streak(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> StreakResponseDto:
    """
    Retrieves the user's current streak, best streak, and total completed goal days.
    """
    return await StepService.get_user_streak(db, current_user)



@router.post("", response_model=StepResponseDto, status_code=status.HTTP_200_OK, summary="Upload or update daily steps")
async def upload_daily_steps(
    payload: StepUploadRequestDto,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> StepResponseDto:
    """
    Uploads or idempotently updates the daily step count for a given date.
    """
    return await StepService.upsert_daily_steps(db, current_user, payload)


@router.get("/today", response_model=StepResponseDto, summary="Get today's step count")
async def get_today_steps(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> StepResponseDto:
    """
    Retrieves the current user's recorded steps for today.
    """
    return await StepService.get_today_steps(db, current_user)


@router.get("/history", response_model=List[StepResponseDto], summary="Get full step history")
async def get_step_history(
    limit: int = 100,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> List[StepResponseDto]:
    """
    Retrieves the user's chronological step history.
    """
    return await StepService.get_step_history(db, current_user, limit=limit)


@router.put("/{date}", response_model=StepResponseDto, summary="Update steps for a specific date")
async def update_steps_for_date(
    date: str,
    payload: StepUploadRequestDto,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> StepResponseDto:
    """
    Updates the step count and goal for a specific date.
    """
    return await StepService.update_steps_for_date(db, current_user, date, payload)


@router.delete("/{date}", status_code=status.HTTP_204_NO_CONTENT, summary="Delete steps for a specific date")
async def delete_steps_for_date(
    date: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> None:
    """
    Deletes the step record for a specific date.
    """
    await StepService.delete_steps_for_date(db, current_user, date)
