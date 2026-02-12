from typing import Optional
from app.services.user_service import UserService
from app.models.models import User, User, UserType
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from app.database import get_session
from app.schemas.user import UserCreate, UserUpdate, UserRead
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/users", tags=["Users"])


@router.post("/", response_model=UserRead)
def create_user(
    data: UserCreate,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user)
):
    service = UserService(session)
        
    return service.create_user(data, data.subject_username, current_user)


@router.get("/{user_id}", response_model=UserRead)
def get_user(
    user_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    service = UserService(session)
    user_db = service.get_user_by_id(user_id)
    if not user_db:
        raise HTTPException(404, "User not found")
    return user_db


@router.get("/", response_model=list[UserRead])
def get_all_users(
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    service = UserService(session)
    return service.get_all_users()


@router.put("/{user_id}", response_model=UserRead)
def update_user(
    user_id: int,
    data: UserUpdate,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = UserService(session)
    return service.update_user(user_id, data, current_user)


@router.delete("/{user_id}")
def delete_user(
    user_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = UserService(session)
    service.delete_user(user_id, current_user)
    return {"message": "User deleted successfully"}
