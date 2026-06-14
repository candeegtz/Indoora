from typing import List

from app.models.models import ActivityPosition, Home, Position, Room, Activity, RoomType, User, ROOM_TYPE_LABELS, UserType
from app.schemas.home import HomeCreate, HomeUpdate, PositionCreate, RoomCreate, ActivityCreate, ActivityUpdate
from sqlmodel import Session, select, delete


class HomeRepository:
    def __init__(self, session: Session):
        self.session = session
    
    # ------------Home------------

    def create_home(self, data: HomeCreate) -> Home:
        home = Home(
            name=data.name,
            estado_config=data.estado_config
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
        
        update_data = data.model_dump(exclude_unset=True)  

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

    # ------------Room------------
    
    def create_room(self, data: RoomCreate) -> Room:
        room = Room.model_validate(data.model_dump())

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
        
        update_data = data.model_dump(exclude_unset=True)  

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

    def get_rooms_by_home_id(self, home_id: int) -> list[Room]:
        return self.session.exec(select(Room).where(Room.home_id == home_id)).all()
    
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
    
    def update_position(self, position_id: int, data: PositionCreate) -> Position:
        position = self.get_position_by_id(position_id)
        if not position:
            raise ValueError("Position not found")
        
        update_data = data.model_dump(exclude_unset=True) 

        for key, value in update_data.items():
            setattr(position, key, value)

        self.session.commit()
        self.session.refresh(position)
        return position
    
    def delete_position(self, position_id: int):
        position = self.get_position_by_id(position_id)
        if not position:
            raise ValueError("Position not found")
        
        self.session.delete(position)
        self.session.commit()   

    def get_positions_by_room_id(self, room_id: int) -> list[Position]:
        return self.session.exec(select(Position).where(Position.room_id == room_id)).all()

    # ------------Activity------------

    def create_activity(self, data: ActivityCreate) -> Activity:
        activity = Activity(name=data.name, home_id=data.home_id)  
        self.session.add(activity)
        self.session.flush()   # para obtener activity.id
        # Asociar posiciones
        for pos_id in data.position_ids:
            activity_position = ActivityPosition(activity_id=activity.id, position_id=pos_id)
            self.session.add(activity_position)
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

        allowed_fields = {"name"}
        update_data = data.model_dump(exclude_unset=True)
        filtered = {k: v for k, v in update_data.items() if k in allowed_fields}
        
        for key, value in filtered.items():
            setattr(activity, key, value)
        
        self.session.commit()
        self.session.refresh(activity)
        return activity
    
    def update_activity_positions(self, activity_id: int, position_ids: List[int]):
        self.session.exec(
            delete(ActivityPosition).where(ActivityPosition.activity_id == activity_id)
        )
        for pos_id in position_ids:
            self.session.add(ActivityPosition(activity_id=activity_id, position_id=pos_id))
        self.session.commit()

    def delete_activity(self, activity_id: int):
        activity = self.get_activity_by_id(activity_id)
        if not activity:
            raise ValueError("Activity not found")

        self.session.delete(activity)
        self.session.commit()

    def get_activities_by_home_id(self, home_id: int) -> list[Activity]:
        return self.session.exec(select(Activity).where(Activity.home_id == home_id)).all()
    
    def get_activity_with_positions(self, activity_id: int) -> tuple[Activity | None, List[int]]:
        activity = self.session.get(Activity, activity_id)
        if not activity:
            return None, []

        stmt = select(ActivityPosition.position_id).where(ActivityPosition.activity_id == activity_id)
        position_ids = self.session.exec(stmt).all()
        return activity, position_ids
    
    # ------------Positioning------------
    def get_rooms_and_positions(self, home_id: int) -> dict:
        '''Diccionario de habitaciones y posiciones para el motor indoor'''
        rooms = self.get_rooms_by_home_id(home_id)
        result = {}
        for room in rooms:
            positions = self.get_positions_by_room_id(room.id)
            result[room.name] = [p.name for p in positions]
        return result

    def get_supervisor_emails_by_home(self, home_id: int) -> List[str]:
        users = self.session.exec(
            select(User).where(User.home_id == home_id, User.user_type.in_([UserType.SUPERVISOR, UserType.SUPERVISOR_CREATOR]))
        ).all()
        return [u.email for u in users if u.email]