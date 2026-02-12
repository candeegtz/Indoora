from app.repositories.home_repository import HomeRepository
from app.models.models import User, UserType
from backend.app.schemas.home import HomeCreate
from fastapi import HTTPException
from sqlmodel import Session

from app.repositories.user_repository import UserRepository
from app.schemas.user import UserCreate, UserUpdate
from app.core.security import hash_password, verify_password


class UserService:
    def __init__(self, session: Session):
        self.repo = UserRepository(session)
        self.home_repo = HomeRepository(session)


    def create_user(self, data: UserCreate, subject_username: str = None, current_user: User = None):
        # Email no vacío
        if not data.email.strip():
            raise HTTPException(400, "Email cannot be empty")

        # Username no vacío
        if not data.username.strip():
            raise HTTPException(400, "Username cannot be empty")
        
        # Username único
        existing = self.repo.get_user_by_username(data.username)
        if existing:
            raise HTTPException(400, "Username already taken")

        # Contraseña mínima
        if len(data.password) < 6:
            raise HTTPException(400, "Password must be at least 6 characters")

        # Email único
        existing = self.repo.get_user_by_email(data.email)
        if existing:
            raise HTTPException(400, "Email already registered")

        home_id_user = None

        # Si es SUPERVISOR_CREATOR, crear Home nuevo
        if data.user_type == UserType.SUPERVISOR_CREATOR:
            if not data.home_name:
                raise HTTPException(400, "home_name is required for SUPERVISOR_CREATOR")
            home_shema= HomeCreate(name=data.home_name)
            home = self.home_repo.create_home(home_shema)
            home_id_user = home.id
        
        # Si es solo SUPERVISOR, buscar Home existente por username del SUBJECT
        elif data.user_type == UserType.SUPERVISOR:
            if not subject_username:
                raise HTTPException(400, "subject_username is required for SUPERVISOR")
            
            subject = self.repo.get_user_by_username(subject_username)
            if not subject:
                raise HTTPException(404, "Subject not found")
            
            # Verificar que es un Subject
            if subject.user_type != UserType.SUBJECT:
                raise HTTPException(400, f"User '{subject_username}' is not a SUBJECT")
            
            if not subject.home_id:
                raise HTTPException(404, f"Subject '{subject_username}' does not have a Home assigned")
            home_id_user = subject.home_id

        # La creación de SUBJECT se hace exclusivamente en la creación del user SUPERVISOR_CREATOR
        elif data.user_type == UserType.SUBJECT:
            if not current_user or current_user.user_type != UserType.SUPERVISOR_CREATOR:
                raise HTTPException(403, "Only SUPERVISOR_CREATOR users can create SUBJECT users")
            
            if not current_user.home_id:
                raise HTTPException(400, "home_id is required for SUBJECT")
            
            has_home_with_subject = self.repo.has_home_with_subject(current_user.home_id)
            if has_home_with_subject:
                raise HTTPException(400, "Home with subject exists. Homes can only have one subject, and it must be created through a SUPERVISOR_CREATOR user")
            home_id_user = current_user.home_id

        return self.repo.create_user(data, home_id=home_id_user)


    def get_user_by_id(self, user_id: int):
        user = self.repo.get_user_by_id(user_id)
        if not user:
            raise HTTPException(404, "User not found")
        return user

    def get_all_users(self):
        return self.repo.get_all_users()

    def update_user(self, user_id: int, data: UserUpdate, current_user : User = None):
        
        if not current_user:
            raise HTTPException(401, "Authentication required")

        # Verificar que el usuario existe
        user = self.repo.get_user_by_id(user_id)
        if not user:
            raise HTTPException(404, "User not found")
        
        # Solo puede editar sus propios datos o si es supervisor del mismo Home
        if current_user.id != user_id:
            # Si no es el mismo usuario, verificar si es supervisor del mismo Home
            if not current_user.home_id or current_user.home_id != user.home_id:
                raise HTTPException(403, "You can only edit your own profile or users in your Home")
            
            # Solo supervisors pueden editar otros usuarios
            if current_user.user_type not in [UserType.SUPERVISOR, UserType.SUPERVISOR_CREATOR]:
                raise HTTPException(403, "You don't have permission to edit other users")
        
        # Username único (solo si se está cambiando)
        if data.username is not None:
            if not data.username.strip():
                raise HTTPException(400, "Username cannot be empty")
            
            existing = self.repo.get_user_by_username(data.username)
            if existing and existing.id != user_id:
                raise HTTPException(400, "Username already taken")
        
        # Email único 
        if data.email is not None:
            if not data.email.strip():
                raise HTTPException(400, "Email cannot be empty")
            
            existing = self.repo.get_user_by_email(data.email)
            if existing and existing.id != user_id:
                raise HTTPException(400, "Email already registered")
        
        # Contraseña mínima 
        if data.password is not None:
            if len(data.password) < 6:
                raise HTTPException(400, "Password must be at least 6 characters")
        
        # No se puede cambiar el tipo de usuario
        if data.user_type is not None:
            raise HTTPException(400, "Cannot change user type")
        
        return self.repo.update_user(user_id, data)

    def delete_user(self, user_id: int, current_user: User = None):
        user = self.repo.get_user_by_id(user_id)

        if not current_user:
            raise HTTPException(401, "Authentication required")

        if not user:
            raise HTTPException(404, "User not found")

        # Verificar permisos de eliminación
        if current_user.id != user_id:
            # Si no es el mismo usuario, verificar si es supervisor del mismo Home
            if not current_user.home_id or current_user.home_id != user.home_id:
                raise HTTPException(403, "You can only delete your own profile or users in your Home")
            
            # Solo supervisors pueden eliminar otros usuarios
            if current_user.user_type != UserType.SUPERVISOR_CREATOR:
                raise HTTPException(403, "You don't have permission to delete other users")
        
        # Solo se pueden eliminar usuarios supervisores 
        if user.user_type != UserType.SUPERVISOR:
            raise HTTPException(400, "Only supervisors can be deleted, in other case, delete home")

        self.repo.delete_user(user_id)

    def authenticate(self, email: str, password: str):
        user = self.repo.get_user_by_email(email)
        if not user:
            raise HTTPException(400, "Invalid credentials")

        if not verify_password(password, user.password_hash):
            raise HTTPException(400, "Invalid credentials")

        return user
    
    def get_user_by_email(self, email: str):
        return self.repo.get_user_by_email(email)

