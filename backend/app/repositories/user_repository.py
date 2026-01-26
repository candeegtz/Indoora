from Indoora.backend.app.schemas.user import UserCreate, UserUpdate
from sqlmodel import Session, select
from app.models.user import User
from Indoora.backend.app.core.security import hash_password

class UserRepository:
    def __init__(self, session: Session):
        self.session = session

    def create(self, data: UserCreate) -> User:
        user = User(
            username = data.username,
            name = data.name,
            surnames = data.surnames,
            email = data.email,
            password = hash_password(data.password),
            userType = data.userType
        )
        self.session.add(user)
        self.session.commit()
        self.session.refresh(user)
        return user

    def get_user_by_id(self, user_id: int) -> User | None: 
        return self.session.get(User, user_id)
    
    def get_all_users(self) -> list[User]: 
        return self.session.exec(select(User)).all()

    def update_user(self, user_id: int, data: UserUpdate) -> User:
        user = self.get_user_by_id(user_id)
        if not user:
            raise ValueError("User not found")
        
        update_data = data.dict(exclude_unset=True)
        
        if "password" in update_data:
            update_data["password_hash"] = hash_password(update_data.pop("password"))
        
        for key, value in update_data.items():
            setattr(user, key, value)

        self.session.commit()
        self.session.refresh(user)
        return user
        
    def delete_user(self, user_id: int):
        user = self.get_user_by_id(user_id)
        if not user:
            raise ValueError("User not found")
        
        self.session.delete(user)
        self.session.commit()


    # Eliminar user, eliminar HOME y todo lo relacionado?
