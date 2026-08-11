"""
Unified API v1 Router.
Aggregates all domain endpoint routers under standard subpaths.
"""

from fastapi import APIRouter

from app.api.v1.endpoints import auth, leaderboard, motivation, profile, steps

api_router = APIRouter()

# Mount feature routers under designated resource paths
api_router.include_router(auth.router, prefix="/auth", tags=["Authentication"])
api_router.include_router(steps.router, prefix="/steps", tags=["Steps"])
api_router.include_router(leaderboard.router, prefix="/leaderboard", tags=["Leaderboard"])
api_router.include_router(profile.router, prefix="/profile", tags=["Profile"])
api_router.include_router(motivation.router, prefix="/motivation", tags=["Motivation"])
