"""
Leaderboard API Endpoints.
Provides competitive rankings by step volume and daily goal percentage across time periods.
"""

from typing import List
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.leaderboard import LeaderboardItemDto, UserRankDto
from app.services.leaderboard_service import LeaderboardService

router = APIRouter()


@router.get("", response_model=List[LeaderboardItemDto], summary="Get competitive leaderboard rankings")
async def get_leaderboard(
    period: str = Query(default="today", regex="^(today|week|all_time)$", description="Ranking period"),
    type: str = Query(default="steps", regex="^(steps|goal)$", description="Ranking criteria"),
    limit: int = Query(default=50, ge=1, le=100, description="Max rankings returned"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> List[LeaderboardItemDto]:
    """
    Returns ranked users based on steps or goal achievement for today, this week, or all time.
    """
    return await LeaderboardService.get_leaderboard(
        db=db,
        period=period,
        ranking_type=type,
        limit=limit
    )


@router.get("/me", response_model=UserRankDto, summary="Get current user's personal rank")
async def get_my_rank(
    period: str = Query(default="today", regex="^(today|week|all_time)$", description="Ranking period"),
    type: str = Query(default="steps", regex="^(steps|goal)$", description="Ranking criteria"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
) -> UserRankDto:
    """
    Returns the current user's rank position and metrics within the specified period.
    """
    return await LeaderboardService.get_user_rank(
        db=db,
        user=current_user,
        period=period,
        ranking_type=type
    )
