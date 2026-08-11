"""
Firebase Admin Authentication Service.
Handles initialization of Firebase Admin SDK with service account credentials
and verifies Firebase ID Tokens for incoming API requests.
"""

import json
import logging
import os
from typing import Any, Dict, Optional

import firebase_admin
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials

from app.core.config import settings
from app.core.exceptions import UnauthorizedException

logger = logging.getLogger(__name__)


class FirebaseService:
    """
    Manages Firebase Admin SDK lifecycle and token verification.
    """
    _initialized: bool = False

    @classmethod
    def initialize(cls) -> None:
        """
        Initializes Firebase Admin SDK using service account credentials.
        Only initializes once across the application lifecycle.
        """
        if cls._initialized or len(firebase_admin._apps) > 0:
            cls._initialized = True
            return

        cred: Optional[credentials.Base] = None

        # 1. Check if raw JSON credentials string is provided in environment
        if settings.FIREBASE_CREDENTIALS_JSON:
            try:
                cert_dict = json.loads(settings.FIREBASE_CREDENTIALS_JSON)
                cred = credentials.Certificate(cert_dict)
                logger.info("Initialized Firebase Admin using FIREBASE_CREDENTIALS_JSON environment variable.")
            except Exception as e:
                logger.warning(f"Failed to parse FIREBASE_CREDENTIALS_JSON: {e}")

        # 2. Check if a serviceAccountKey.json file path is provided
        if not cred and settings.FIREBASE_CREDENTIALS_PATH and os.path.exists(settings.FIREBASE_CREDENTIALS_PATH):
            try:
                cred = credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
                logger.info(f"Initialized Firebase Admin using credentials file: {settings.FIREBASE_CREDENTIALS_PATH}")
            except Exception as e:
                logger.warning(f"Failed to load credentials file from {settings.FIREBASE_CREDENTIALS_PATH}: {e}")

        # 3. Initialize Firebase app with credentials or default application credentials
        try:
            if cred:
                firebase_admin.initialize_app(cred)
            else:
                # Attempt default credentials or initialize with project ID
                options = {"projectId": settings.FIREBASE_PROJECT_ID} if settings.FIREBASE_PROJECT_ID else {}
                firebase_admin.initialize_app(options=options)
                logger.info("Initialized Firebase Admin using default application environment.")
            cls._initialized = True
        except Exception as e:
            logger.error(f"Error during Firebase Admin initialization: {e}")
            cls._initialized = False

    @classmethod
    def verify_token(cls, id_token: str) -> Dict[str, Any]:
        """
        Verifies a Firebase ID token.
        Returns decoded claims dictionary (contains 'uid', 'email', 'name', etc.).
        Raises UnauthorizedException if the token is invalid or expired.
        """
        if not id_token:
            raise UnauthorizedException("Authorization token is missing.")

        # Ensure Firebase Admin is initialized
        if not cls._initialized and len(firebase_admin._apps) == 0:
            cls.initialize()

        try:
            # Verify the token using Firebase Admin SDK
            decoded_token = firebase_auth.verify_id_token(id_token)
            return decoded_token
        except firebase_auth.ExpiredIdTokenError:
            raise UnauthorizedException("Firebase ID token has expired. Please refresh your session.")
        except firebase_auth.RevokedIdTokenError:
            raise UnauthorizedException("Firebase ID token has been revoked.")
        except firebase_auth.InvalidIdTokenError as e:
            raise UnauthorizedException(f"Invalid Firebase ID token: {str(e)}")
        except Exception as e:
            # In development/test mode, check for special test token prefix
            if settings.ENVIRONMENT in ["test", "development"] and id_token.startswith("test_token_"):
                uid = id_token.replace("test_token_", "")
                return {
                    "uid": uid,
                    "email": f"{uid}@example.com",
                    "name": f"Test User {uid}"
                }
            logger.error(f"Firebase token verification error: {e}")
            raise UnauthorizedException("Failed to authenticate with Firebase token.")
