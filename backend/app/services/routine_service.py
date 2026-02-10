from fastapi import HTTPException
from sqlmodel import Session

from app.repositories.routine_repository import RoutineRepository
from app.repositories.home_repository import HomeRepository

from app.schemas.routine import (
    RoutineCreate, RoutineUpdate
)


class RoutineService:
    def __init__(self, session: Session):
        self.repo = RoutineRepository(session)
        self.home_repo = HomeRepository(session)
        self.activity_repo = HomeRepository(session)


    # ------------Routine------------

    def is_routine_overlapping(self, home_id: int, days: list[str], startTime, endTime) -> bool:
        routines = self.repo.get_routines_by_home_id_and_days(home_id, days)
        for d in days:
            for r in routines:
                if d in r.days:
                    if (startTime < r.endTime and endTime > r.startTime):
                        return True
        return False

    def create_routine(self, data: RoutineCreate, current_user = None):
        
        activity = self.activity_repo.get_activity_by_id(data.activity_id) 
        if not activity:
            raise HTTPException(404, "Activity not found")
        
        if activity.home_id is None:
            raise HTTPException(400, "Activity must be associated with a Home")

        if not current_user or current_user.home_id != activity.home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        # nombre no vacío
        if not data.name.strip():
            raise HTTPException(400, "Routine name cannot be empty")

        # actividad debe existir
        activity = self.activity_repo.get_activity_by_id(data.activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")

        # horas coherentes
        if data.startTime >= data.endTime:
            raise HTTPException(400, "startTime must be earlier than endTime")
        
        # Rutinas no superpuestas
        if self.is_routine_overlapping(activity.home_id, data.days, data.startTime, data.endTime):
            raise HTTPException(400, "Routine time overlaps with existing routine on the same day")

        return self.repo.create_routine(data)
    

    def get_routine(self, routine_id: int, current_user = None):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")
        return routine
    
    def get_routines_by_home_id(self, home_id: int, current_user = None):
        if not current_user or current_user.home_id != home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        return self.repo.get_routines_by_home_id(home_id)

    def get_all_routines(self):
        return self.repo.get_all_routines()

    def update_routine(self, routine_id: int, data: RoutineUpdate, current_user = None):

        activity = self.activity_repo.get_activity_by_id(data.activity_id) 
        if not activity:
            raise HTTPException(404, "Activity not found")
        
        if activity.home_id is None:
            raise HTTPException(400, "Activity must be associated with a Home")

        if not current_user or current_user.home_id != activity.home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")

        # actividad si se actualiza
        if data.activity_id:
            activity = self.activity_repo.get_activity_by_id(data.activity_id)
            if not activity:
                raise HTTPException(404, "Activity not found")

        # horas coherentes
        if data.startTime and data.endTime:
            if data.startTime >= data.endTime:
                raise HTTPException(400, "startTime must be earlier than endTime")
            
         # Rutinas no superpuestas
        if self.is_routine_overlapping(activity.home_id, data.days, data.startTime, data.endTime):
                        raise HTTPException(400, "Routine time overlaps with existing routine on the same day")

        return self.repo.update_routine(routine_id, data)

    def delete_routine(self, routine_id: int, current_user = None):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")
        
        activity = self.activity_repo.get_activity_by_id(routine.activity_id) 
        if not activity:
            raise HTTPException(404, "Activity not found")
        
        if activity.home_id is None:
            raise HTTPException(400, "Activity must be associated with a Home")

        if not current_user or current_user.home_id != activity.home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        self.repo.delete_routine(routine_id)
