from app.models.models import Home, Position, Room, Activity, User
from app.schemas.home import HomeCreate, HomeUpdate, PositionCreate, RoomCreate, ActivityCreate, ActivityUpdate
from sqlmodel import Session, select


class HomeRepository:
    def __init__(self, session: Session):
        self.session = session
    
    # ------------Home------------

    def create_home(self, data: HomeCreate) -> Home:
        home = Home(
            name = data.name
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
    
    def get_home_by_user_id(self, user_id: int) -> Home | None:
        return self.session.exec(select(Home).where(Home.users.any(User.id == user_id))).first()
    
    def has_home_with_subject(self, home_id: int) -> bool:
        return self.session.exec(select(Home).where(Home.id == home_id and Home.users.any(User.user_type == "SUBJECT"))).first() is not None

    # ------------Room------------
    
    def create_room(self, data: RoomCreate) -> Room:
        room = Room(
            name = data.name,
            roomType = data.roomType,
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
        position = self.get_position_by_id(position_id)
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


    # ------------Activity------------

    def create_activity(self, data: ActivityCreate) -> Activity:
        activity = Activity(name=data.name)
        self.session.add(activity)
        self.session.commit()
        self.session.refresh(activity)
        return activity

    def get_activity_by_id(self, activity_id: int) -> Activity | None:
        return self.session.get(Activity, activity_id)

    def get_all_activities(self) -> list[Activity]:
        return self.session.exec(select(Activity)).all()

    def update_activity(self, activity_id: int, data: ActivityUpdate) -> Activity:
        activity = self.get_activity_by_id(activity_id)
        if not activity:
            raise ValueError("Activity not found")

        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(activity, key, value)

        self.session.commit()
        self.session.refresh(activity)
        return activity

    def delete_activity(self, activity_id: int):
        activity = self.get_activity_by_id(activity_id)
        if not activity:
            raise ValueError("Activity not found")

        self.session.delete(activity)
        self.session.commit()
