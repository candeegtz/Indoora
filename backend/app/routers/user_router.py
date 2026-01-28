from Indoora.backend.app.services.user_service import UserService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from Indoora.backend.app.database import get_session
from Indoora.backend.app.schemas.user import UserCreate, UserUpdate, UserRead

router = APIRouter(prefix="/users", tags=["Users"])


@router.post("/", response_model=UserRead)
def create_user(data: UserCreate, session: Session = Depends(get_session)):
    repo = UserService(session)
    return repo.create_user(data)


@router.get("/{user_id}", response_model=UserRead)
def get_user(user_id: int, session: Session = Depends(get_session)):
    repo = UserService(session)
    user = repo.get_user_by_id(user_id)
    if not user:
        raise HTTPException(404, "User not found")
    return user


@router.get("/", response_model=list[UserRead])
def get_all_users(session: Session = Depends(get_session)):
    repo = UserService(session)
    return repo.get_all_users()


@router.put("/{user_id}", response_model=UserRead)
def update_user(user_id: int, data: UserUpdate, session: Session = Depends(get_session)):
    repo = UserService(session)
    return repo.update_user(user_id, data)


@router.delete("/{user_id}")
def delete_user(user_id: int, session: Session = Depends(get_session)):
    repo = UserService(session)
    repo.delete_user(user_id)
    return {"message": "User deleted successfully"}
