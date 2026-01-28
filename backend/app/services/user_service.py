from fastapi import HTTPException
from sqlmodel import Session

from Indoora.backend.app.repositories.user_repository import UserRepository
from Indoora.backend.app.schemas.user import UserCreate, UserUpdate
from Indoora.backend.app.core.security import hash_password, verify_password


class UserService:
    def __init__(self, session: Session):
        self.repo = UserRepository(session)


    def create_user(self, data: UserCreate):
        # Email no vacío
        if not data.email.strip():
            raise HTTPException(400, "Email cannot be empty")

        # Username no vacío
        if not data.username.strip():
            raise HTTPException(400, "Username cannot be empty")

        # Contraseña mínima
        if len(data.password) < 6:
            raise HTTPException(400, "Password must be at least 6 characters")

        # Email único
        existing = self.repo.get_user_by_email(data.email)
        if existing:
            raise HTTPException(400, "Email already registered")

        return self.repo.create_user(data)

    def get_user(self, user_id: int):
        user = self.repo.get_user_by_id(user_id)
        if not user:
            raise HTTPException(404, "User not found")
        return user

    def get_all_users(self):
        return self.repo.get_all_users()

    def update_user(self, user_id: int, data: UserUpdate):
        user = self.repo.get_user_by_id(user_id)
        if not user:
            raise HTTPException(404, "User not found")

        # Email único 
        if data.email:
            existing = self.repo.get_user_by_email(data.email)
            if existing and existing.id != user_id:
                raise HTTPException(400, "Email already registered")

        # Constraseña mínima
        if data.password and len(data.password) < 6:
            raise HTTPException(400, "Password must be at least 6 characters")

        return self.repo.update_user(user_id, data)

    def delete_user(self, user_id: int):
        user = self.repo.get_user_by_id(user_id)
        if not user:
            raise HTTPException(404, "User not found")

        self.repo.delete_user(user_id)

    def authenticate(self, email: str, password: str):
        user = self.repo.get_user_by_email(email)
        if not user:
            raise HTTPException(400, "Invalid credentials")

        if not verify_password(password, user.password_hash):
            raise HTTPException(400, "Invalid credentials")

        return user
