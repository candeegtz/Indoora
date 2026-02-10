from app.models.models import User, UserType
from sqlmodel import Session
from fastapi import HTTPException

from app.repositories.home_repository import HomeRepository
from app.schemas.home import (
    HomeCreate, HomeUpdate,
    RoomCreate, RoomUpdate,
    PositionCreate, PositionUpdate,
    ActivityCreate, ActivityUpdate
)

 # TODO: Añadir reglas de negocio

class HomeService:
    def __init__(self, session: Session):
        self.repo = HomeRepository(session)


    # ------------Home------------

    def create_home(self, data: HomeCreate, current_user: User = None):
        
        if not current_user or current_user.user_type != UserType.SUPERVISOR_CREATOR and current_user.user_type != UserType.ADMIN:
            raise HTTPException(403, "Only admins and supervisors can create homes")

        if not data.name.strip():
            raise HTTPException(400, "Home name cannot be empty")

        return self.repo.create_home(data)

    def get_home(self, home_id: int):
        home = self.repo.get_home_by_id(home_id)
        if not home:
            raise HTTPException(404, "Home not found")
        return home

    def update_home(self, home_id: int, data: HomeUpdate, current_user: User = None):
        if not current_user or current_user.user_type != UserType.SUPERVISOR_CREATOR and current_user.user_type != UserType.ADMIN:
            raise HTTPException(403, "Only admins and supervisors can update homes")
        
        return self.repo.update(home_id, data)

    def delete_home(self, home_id: int):
        self.repo.delete_home(home_id)

    
    # ------------Room------------

    def create_room(self, data: RoomCreate):
        # Asociación a un
        home = self.repo.get_home_by_id(data.home_id)
        if not home:
            raise HTTPException(404, "Home not found")

        return self.repo.create_room(data)

    def get_room(self, room_id: int):
        room = self.repo.get_room_by_id(room_id)
        if not room:
            raise HTTPException(404, "Room not found")
        return room

    def update_room(self, room_id: int, data: RoomUpdate):
        return self.repo.update_room(room_id, data)

    def delete_room(self, room_id: int):
        self.repo.delete_room(room_id)

    
    # ------------Position------------

    def create_position(self, data: PositionCreate):
        # ASociada a una room
        room = self.repo.get_room_by_id(data.room_id)
        if not room:
            raise HTTPException(404, "Room not found")

        return self.repo.create_position(data)

    def get_position(self, position_id: int):
        pos = self.repo.get_position_by_id(position_id)
        if not pos:
            raise HTTPException(404, "Position not found")
        return pos

    def update_position(self, position_id: int, data: PositionUpdate):
        return self.repo.update_position(position_id, data)

    def delete_position(self, position_id: int):
        self.repo.delete_position(position_id)

    
    # ------------Activity------------

    def create_activity(self, data: ActivityCreate):
        return self.repo.create_activity(data)

    def get_activity(self, activity_id: int):
        activity = self.repo.get_activity_by_id(activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")
        return activity

    def update_activity(self, activity_id: int, data: ActivityUpdate):
        return self.repo.update_activity(activity_id, data)

    def delete_activity(self, activity_id: int):
        self.repo.delete_activity(activity_id)
