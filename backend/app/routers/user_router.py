from app.services.user_service import UserService
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
    user = Depends(get_current_user)
):
    repo = UserService(session)
    return repo.create_user(data)


@router.get("/{user_id}", response_model=UserRead)
def get_user(
    user_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = UserService(session)
    user_db = repo.get_user_by_id(user_id)
    if not user_db:
        raise HTTPException(404, "User not found")
    return user_db


@router.get("/", response_model=list[UserRead])
def get_all_users(
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = UserService(session)
    return repo.get_all_users()


@router.put("/{user_id}", response_model=UserRead)
def update_user(
    user_id: int,
    data: UserUpdate,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = UserService(session)
    return repo.update_user(user_id, data)


@router.delete("/{user_id}")
def delete_user(
    user_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = UserService(session)
    repo.delete_user(user_id)
    return {"message": "User deleted successfully"}
