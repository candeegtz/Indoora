from fastapi import APIRouter, Depends
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
