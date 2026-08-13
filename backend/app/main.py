"""
FastAPI Main Application Entry Point.
Configures middleware, lifecycle startup, CORS headers, API routers, and error handlers.
"""

from contextlib import asynccontextmanager
import logging
from typing import AsyncGenerator

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.v1.router import api_router
from app.core.config import settings
from app.core.exceptions import (
    AppException,
    app_exception_handler,
    generic_http_exception_handler,
    unhandled_exception_handler
)
from app.db.base import Base
from app.db.session import engine
from app.services.firebase_service import FirebaseService

# Configure root logger
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("stepcount.main")


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Application lifespan manager.
    Executes database table creation and Firebase initialization on startup,
    and cleans up database connections on shutdown.
    """
    logger.info(f"Starting {settings.PROJECT_NAME} [{settings.ENVIRONMENT}]...")
    
    # 1. Initialize Firebase Admin SDK
    FirebaseService.initialize()

    # 2. Asynchronously verify and initialize database tables in NeonDB
    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        logger.info("NeonDB database schema verified and initialized successfully.")
    except Exception as e:
        logger.error(f"Database initialization error (verify DATABASE_URL): {e}")

    yield

    # Cleanup resources on shutdown
    logger.info("Shutting down database engine...")
    await engine.dispose()


# Create FastAPI application instance
app = FastAPI(
    title=settings.PROJECT_NAME,
    version="1.0.0",
    description="Asynchronous REST API for StepCount Android Fitness Application",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    lifespan=lifespan
)

# Configure Cross-Origin Resource Sharing (CORS)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register custom exception handlers for uniform JSON error responses
app.add_exception_handler(AppException, app_exception_handler)
app.add_exception_handler(HTTPException, generic_http_exception_handler)
app.add_exception_handler(Exception, unhandled_exception_handler)

# Mount API routers (both /api for Android client and /api/v1 for versioned routing)
app.include_router(api_router, prefix=settings.API_V1_STR)
app.include_router(api_router, prefix="/api/v1")


@app.get("/", tags=["System"], summary="Root Health Check")
async def root() -> JSONResponse:
    """
    Basic service information and health check endpoint.
    """
    return JSONResponse(
        content={
            "service": settings.PROJECT_NAME,
            "status": "online",
            "environment": settings.ENVIRONMENT,
            "docs": "/docs"
        }
    )


@app.get("/health", tags=["System"], summary="Service Health")
async def health_check() -> JSONResponse:
    """
    Uptime health check endpoint.
    """
    return JSONResponse(content={"status": "healthy"})
