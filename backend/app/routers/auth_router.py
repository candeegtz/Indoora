from Indoora.backend.app.models.user import UserType
from Indoora.backend.app.schemas.user import UserCreate, UserRead
from Indoora.backend.app.services.user_service import UserService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session

from Indoora.backend.app.database import get_session
from Indoora.backend.app.services.auth_service import AuthService
from Indoora.backend.app.dependencies.auth import get_current_user

router = APIRouter(prefix="/auth", tags=["Auth"])


@router.post("/login")
def login(email: str, password: str, session: Session = Depends(get_session)):
    service = AuthService(session)
    return service.login(email, password)


@router.post("/refresh")
def refresh(refresh_token: str, session: Session = Depends(get_session)):
    service = AuthService(session)
    return service.refresh(refresh_token)


@router.get("/me")
def me(user = Depends(get_current_user)):
    return user


@router.post("/register-supervisor", response_model=UserRead)
def register_supervisor(
    data: UserCreate,
    session: Session = Depends(get_session)
):
    data.userType = UserType.SUPERVISOR

    repo = UserService(session)

    existing = repo.get_user_by_email(data.email)
    if existing:
        raise HTTPException(400, "Email already registered")

    user = repo.create_user(data)
    return user
