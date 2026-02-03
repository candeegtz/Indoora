from app.models.models import UserType
from fastapi import HTTPException
from sqlmodel import Session
from app.core.security import (
    verify_password,
    create_access_token,
    create_refresh_token,
    REFRESH_TOKEN_EXPIRE_DAYS_DEFAULT,
    REFRESH_TOKEN_EXPIRE_DAYS_SUBJECT,
)
from app.repositories.user_repository import UserRepository

class AuthService:
    def __init__(self, session: Session):
        self.user_repo = UserRepository(session)

    def login(self, email: str, password: str):
        user = self.user_repo.get_user_by_email(email)
        if not user:
            raise HTTPException(400, "Invalid credentials")

        if not verify_password(password, user.password_hash):
            raise HTTPException(400, "Invalid credentials")

        access = create_access_token({"sub": str(user.id)})

        if user.user_type == UserType.SUBJECT:
            refresh_days = REFRESH_TOKEN_EXPIRE_DAYS_SUBJECT
        else:
            refresh_days = REFRESH_TOKEN_EXPIRE_DAYS_DEFAULT

        refresh = create_refresh_token({"sub": str(user.id)}, days=refresh_days)

        return {
            "access_token": access,
            "refresh_token": refresh,
            "token_type": "bearer"
        }
