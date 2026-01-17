from sqlmodel import Session, select
from app.models.user import User

class UserRepository:
    def __init__(self, session: Session):
        self.session = session

    def create(self, user: User) -> User:
        self.session.add(user)
        self.session.commit()
        self.session.refresh(user)
        return user

    def get_all(self) -> list[User]:
        return self.session.exec(select(User)).all()

    def get_by_id(self, user_id: int) -> User | None:
        return self.session.get(User, user_id)
    
    def update(self, user: User) -> User:
        self.session.add(user)
        self.session.commit()
        self.session.refresh(user)
        return user
    
    # Eliminar user, eliminar HOME y todo lo relacionado?
