from Indoora.backend.app.models.home import Home
from sqlmodel import Session, select

class HomeRepository:
    def __init__(self, session: Session):
        self.session = session
    
    def create(self, home: Home) -> Home:
        self.session.add(home)
        self.session.commit()
        self.session.refresh(home)
        return home
    
    def update(self, home: Home) -> Home:
        self.session.add(home)
        self.session.commit()
        self.session.refresh(home)
        return home
    
    def get_all(self) -> list[Home]:
        return self.session.exec(select(Home)).all()
    
    def get_by_id(self, home_id: int) -> Home | None:
        return self.session.get(Home, home_id)