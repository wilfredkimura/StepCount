"""
Standardized error exceptions and handlers for StepCount API.
Formats error responses in a clean, consistent JSON envelope.
"""

from typing import Any, Optional
from fastapi import HTTPException, Request, status
from fastapi.responses import JSONResponse


class AppException(HTTPException):
    """
    Base application exception with error code and friendly message.
    """
    def __init__(
        self,
        status_code: int,
        code: str,
        message: str,
        details: Optional[Any] = None
    ):
        super().__init__(status_code=status_code, detail=message)
        self.code = code
        self.message = message
        self.details = details


class UnauthorizedException(AppException):
    """Raised when authentication fails or token is invalid/expired."""
    def __init__(self, message: str = "Invalid or expired authentication token", details: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            code="UNAUTHORIZED",
            message=message,
            details=details
        )


class ResourceNotFoundException(AppException):
    """Raised when a requested resource is not found."""
    def __init__(self, message: str = "Resource not found", details: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            code="RESOURCE_NOT_FOUND",
            message=message,
            details=details
        )


class ValidationException(AppException):
    """Raised when request payload validation fails."""
    def __init__(self, message: str = "Invalid input data", details: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            code="VALIDATION_ERROR",
            message=message,
            details=details
        )


async def app_exception_handler(request: Request, exc: AppException) -> JSONResponse:
    """
    Standard exception handler converting AppException into the required JSON envelope.
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": {
                "code": exc.code,
                "message": exc.message,
                "details": exc.details
            }
        }
    )


async def generic_http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """
    Fallback exception handler for standard FastAPI HTTPExceptions.
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": {
                "code": "HTTP_ERROR",
                "message": str(exc.detail),
                "details": None
            }
        }
    )


async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Global catch-all exception handler to prevent leaking raw tracebacks.
    """
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": {
                "code": "INTERNAL_SERVER_ERROR",
                "message": "An unexpected internal server error occurred.",
                "details": str(exc)
            }
        }
    )
