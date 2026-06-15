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

    def get_home_by_id(self, home_id: int):
        home = self.repo.get_home_by_id(home_id)
        if not home:
            raise HTTPException(404, "Home not found")
        return home

    def update_home(self, home_id: int, data: HomeUpdate, current_user: User = None):
        if not current_user or current_user.user_type != UserType.SUPERVISOR_CREATOR and current_user.user_type != UserType.ADMIN:
            raise HTTPException(403, "Only admins and supervisors can update homes")
        
        return self.repo.update(home_id, data)
    
    def get_all_homes(self, current_user: User = None):
        if not current_user or current_user.user_type != UserType.ADMIN:
            raise HTTPException(403, "Only admins and supervisors can view homes")
        
        return self.repo.get_all_homes()

    def delete_home(self, home_id: int):
        self.repo.delete_home(home_id)

    
    # ------------Room------------

    def create_room(self, data: RoomCreate, current_user: User):
        if not current_user.home_id:
            raise HTTPException(400, "User does not have a home assigned")
        
        if not data.name.strip():
            raise HTTPException(400, "Room name cannot be empty")
        
        data.home_id = current_user.home_id
        
        return self.repo.create_room(data)

    def get_room_by_id(self, room_id: int):
        room = self.repo.get_room_by_id(room_id)
        if not room:
            raise HTTPException(404, "Room not found")
        return room

    def update_room(self, room_id: int, data: RoomUpdate):
        return self.repo.update_room(room_id, data)

    def delete_room(self, room_id: int):
        self.repo.delete_room(room_id)

    def get_rooms_by_home_id(self, home_id: int):
        return self.repo.get_rooms_by_home_id(home_id)
    
    # ------------Position------------

    def create_position(self, data: PositionCreate, current_user: User):
        if not data.name or not data.name.strip():
            raise HTTPException(400, "Position name cannot be empty")
            
        room = self.repo.get_room_by_id(data.room_id)
        if not room:
            raise HTTPException(404, "Room not found")
            
        if room.home_id != current_user.home_id:
            raise HTTPException(403, "This room does not belong to your home")

        return self.repo.create_position(data)

    def get_position_by_id(self, position_id: int):
        pos = self.repo.get_position_by_id(position_id)
        if not pos:
            raise HTTPException(404, "Position not found")
        return pos

    def update_position(self, position_id: int, data: PositionUpdate):
        # No se permite actualizar posiciones en la app
        return self.repo.update_position(position_id, data)

    def delete_position(self, position_id: int):
        # No se permite eliminar posiciones en la app
        self.repo.delete_position(position_id)

    def get_positions_by_room_by_id(self, room_id: int):
        return self.repo.get_positions_by_room_id(room_id)
    
    # ------------Activity------------

    def create_activity(self, data: ActivityCreate, current_user: User):
        if not data.name or not data.name.strip():
            raise HTTPException(400, "Activity name cannot be empty")
            
        if data.home_id != current_user.home_id:
            raise HTTPException(403, "You can only create activities for your own home")
        
        return self.repo.create_activity(data)

    def get_activity_by_id(self, activity_id: int):
        activity = self.repo.get_activity_by_id(activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")
        return activity

    def update_activity(self, activity_id: int, data: ActivityUpdate, current_user: User):
        # Validar que la actividad a actualizar pertenece al usuario
        activity = self.repo.get_activity_by_id(activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")
            
        if activity.home_id != current_user.home_id:
            raise HTTPException(403, "You can only update activities from your own home")

        if not data.name or not data.name.strip():
            raise HTTPException(400, "Activity name cannot be empty")
            
        updated = self.repo.update_activity(activity_id, data)

        if data.position_ids is not None:
            self.repo.update_activity_positions(activity_id, data.position_ids)
        
        return updated

    def delete_activity(self, activity_id: int, current_user: User):
        activity = self.repo.get_activity_by_id(activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")

        if activity.home_id != current_user.home_id:
            raise HTTPException(403, "You can only delete activities from your own home")

        self.repo.delete_activity(activity_id)

    def get_activities_by_home_id(self, home_id: int, current_user: User):
        if home_id != current_user.home_id:
            raise HTTPException(403, "You can only view activities from your own home")
        
        return self.repo.get_activities_by_home_id(home_id)
    
    def get_activity_with_positions(self, activity_id: int, current_user: User):
        activity, position_ids = self.repo.get_activity_with_positions(activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")

        if activity.home_id != current_user.home_id:
            raise HTTPException(403, "You can only view activities from your own home")

        # Construimos el diccionario con los datos de la actividad más los position_ids
        result = activity.dict()
        result["position_ids"] = position_ids
        return result
