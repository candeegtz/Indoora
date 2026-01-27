from Indoora.backend.app.models.home import Home, Position, Room
from Indoora.backend.app.schemas.home import HomeCreate, HomeUpdate, PositionCreate, RoomCreate
from sqlmodel import Session, select

class HomeRepository:
    def __init__(self, session: Session):
        self.session = session
    
    # ------------Home------------

    def create_home(self, data: HomeCreate) -> Home:
        home = Home(
            name = data.name,
            subject_id = data.subject_id
        )

        self.session.add(home)
        self.session.commit()
        self.session.refresh(home)
        return home
    
    def get_home_by_id(self, home_id: int) -> Home | None:
        return self.session.get(Home, home_id)

    def get_all_homes(self) -> list[Home]:
        return self.session.exec(select(Home)).all()   
    
    def update(self, home_id: int, data: HomeUpdate) -> Home:
        home = self.get_home_by_id(home_id)
        if not home:
            raise ValueError("Home not found")
        
        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(home, key, value)

        self.session.commit()
        self.session.refresh(home)
        return home
    
    def delete_home(self, home_id: int):
        home = self.get_home_by_id(home_id) 
        if not home:
            raise ValueError("Home not found")
        
        self.session.delete(home)
        self.session.commit()
    

    # ------------Room------------
    
    def create_room(self, data: RoomCreate) -> Room:
        room = Room(
            name = data.name,
            home_id = data.home_id
        )

        self.session.add(room)
        self.session.commit()
        self.session.refresh(room)
        return room
    
    def get_room_by_id(self, room_id: int) -> Room | None:
        return self.session.get(Room, room_id)

    def get_all_rooms(self) -> list[Room]:
        return self.session.exec(select(Room)).all()
    
    def update_room(self, room_id: int, data: RoomCreate) -> Room:
        room = self.get_room_by_id(room_id)
        if not room:
            raise ValueError("Room not found")
        
        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(room, key, value)

        self.session.commit()
        self.session.refresh(room)
        return room
    
    def delete_room(self, room_id: int):
        room = self.get_room_by_id(room_id)
        if not room:
            raise ValueError("Room not found")
        
        self.session.delete(room)
        self.session.commit()   

    
    # ------------Position------------

    def create_position(self, data: PositionCreate) -> Position:
        position = Position(
            name = data.name,
            room_id = data.room_id
        )

        self.session.add(position)
        self.session.commit()
        self.session.refresh(position)
        return position
    
    def get_position_by_id(self, position_id: int) -> Position | None:
        return self.session.get(Position, position_id)

    def get_all_positions(self) -> list[Position]:
        return self.session.exec(select(Position)).all()
    
    def update_position(self, position_id: int, data: PositionCreate) -> PositionCreate:
        position = self.get_room_by_id(position_id)
        if not position:
            raise ValueError("Position not found")
        
        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(position, key, value)

        self.session.commit()
        self.session.refresh(position)
        return position
    
    def delete_position(self, position_id: int):
        position = self.get_room_by_id(position_id)
        if not position:
            raise ValueError("Position not found")
        
        self.session.delete(position)
        self.session.commit()   
