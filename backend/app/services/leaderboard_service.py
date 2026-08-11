"""
Leaderboard Business Service.
Calculates user rankings and competitive standings across time periods (today, week, all_time)
and ranking criteria (total steps, goal achievement percentage).
"""

from datetime import date, timedelta
from typing import List
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.daily_steps import DailySteps
from app.models.user import User
from app.schemas.leaderboard import LeaderboardItemDto, UserRankDto


class LeaderboardService:
    """
    Computes leaderboard standings from NeonDB daily step records.
    """

    @staticmethod
    async def get_leaderboard(
        db: AsyncSession,
        period: str = "today",
        ranking_type: str = "steps",
        limit: int = 50
    ) -> List[LeaderboardItemDto]:
        """
        Retrieves ranked users for the specified timeframe and sort criterion.
        Supported periods: 'today', 'week', 'all_time'
        Supported types: 'steps', 'goal'
        """
        today = date.today()
        today_str = today.isoformat()

        # Build base query joining User with DailySteps
        query = (
            select(
                User.id.label("user_id"),
                User.name.label("name"),
                func.coalesce(func.sum(DailySteps.steps), 0).label("total_steps"),
                func.coalesce(func.sum(DailySteps.goal), 1).label("total_goal")
            )
            .outerjoin(DailySteps, User.id == DailySteps.user_id)
            .group_by(User.id, User.name)
        )

        # Apply timeframe filter
        if period == "today":
            query = query.where(DailySteps.date == today_str)
        elif period == "week":
            week_ago_str = (today - timedelta(days=7)).isoformat()
            query = query.where(DailySteps.date >= week_ago_str)
        # For 'all_time', no date filter is applied

        result = await db.execute(query)
        rows = result.all()

        if not rows:
            # If no daily_steps matched period, fetch all users with 0 steps
            user_stmt = select(User.id, User.name).limit(limit)
            user_res = await db.execute(user_stmt)
            users = user_res.all()
            return [
                LeaderboardItemDto(
                    user_id=u.id,
                    name=u.name,
                    steps=0,
                    rank=idx + 1,
                    goal_percentage=0.0
                )
                for idx, u in enumerate(users)
            ]

        # Calculate goal percentage and prepare leaderboard items
        items = []
        for r in rows:
            steps_val = int(r.total_steps)
            goal_val = max(int(r.total_goal), 1)
            percentage = round((steps_val / goal_val) * 100.0, 1)
            items.append({
                "user_id": r.user_id,
                "name": r.name,
                "steps": steps_val,
                "goal_percentage": percentage
            })

        # Sort according to requested criteria
        if ranking_type == "goal":
            items.sort(key=lambda x: (x["goal_percentage"], x["steps"]), reverse=True)
        else:
            items.sort(key=lambda x: (x["steps"], x["goal_percentage"]), reverse=True)

        # Assign sequential 1-based ranks
        leaderboard = []
        for idx, item in enumerate(items[:limit]):
            leaderboard.append(
                LeaderboardItemDto(
                    user_id=item["user_id"],
                    name=item["name"],
                    steps=item["steps"],
                    rank=idx + 1,
                    goal_percentage=item["goal_percentage"]
                )
            )

        return leaderboard

    @staticmethod
    async def get_user_rank(
        db: AsyncSession,
        user: User,
        period: str = "today",
        ranking_type: str = "steps"
    ) -> UserRankDto:
        """
        Retrieves the personal rank position and standing for the specified user.
        """
        full_leaderboard = await LeaderboardService.get_leaderboard(
            db=db,
            period=period,
            ranking_type=ranking_type,
            limit=1000
        )

        user_entry = next((item for item in full_leaderboard if item.user_id == user.id), None)

        if user_entry is not None:
            return UserRankDto(
                user_id=user.id,
                name=user.name,
                steps=user_entry.steps,
                rank=user_entry.rank,
                goal_percentage=user_entry.goal_percentage,
                total_participants=len(full_leaderboard)
            )

        # User has no entries in the period yet; placed at bottom
        rank_pos = len(full_leaderboard) + 1
        return UserRankDto(
            user_id=user.id,
            name=user.name,
            steps=0,
            rank=rank_pos,
            goal_percentage=0.0,
            total_participants=max(rank_pos, 1)
        )
