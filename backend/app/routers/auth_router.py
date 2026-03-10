from app.models.models import UserType
from app.schemas.user import UserCreate, UserRead
from app.services.user_service import UserService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session

from app.database import get_session
from app.services.auth_service import AuthService
from app.dependencies.auth import get_current_user
from app.schemas.auth import LoginRequest, RefreshRequest

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/login")
def login(data: LoginRequest, session: Session = Depends(get_session)):
    service = AuthService(session)
    return service.login(data.username, data.password)


@router.post("/refresh")
def refresh(data: RefreshRequest, session: Session = Depends(get_session)):
    service = AuthService(session)
    return service.refresh(data.refresh_token)


@router.get("/me")
def me(user = Depends(get_current_user)):
    return user


@router.post("/register-supervisor", response_model=UserRead)
def register_supervisor(
    data: UserCreate,
    session: Session = Depends(get_session)
):

    if data.user_type != UserType.SUPERVISOR_CREATOR and data.user_type != UserType.SUPERVISOR:
        raise HTTPException(400, "Invalid user type")

    service = UserService(session)
    return service.create_user(data)
