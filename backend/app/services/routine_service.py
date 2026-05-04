from app.models.models import User
from fastapi import HTTPException
from sqlmodel import Session
from app.repositories.routine_repository import RoutineRepository
from app.repositories.home_repository import HomeRepository
from app.schemas.routine import RoutineCreate, RoutineUpdate

class RoutineService:
    def __init__(self, session: Session):
        self.repo = RoutineRepository(session)
        self.home_repo = HomeRepository(session)

    def is_routine_overlapping(self, home_id: int, days: list[str], start_time, end_time, exclude_id: int = None) -> bool:
        candidates = self.repo.get_routines_by_home_id_and_days(home_id, days)
        for r in candidates:
            if exclude_id is not None and r.id == exclude_id:
                continue
            if start_time < r.end_time and end_time > r.start_time:
                return True
        return False

    def create_routine(self, data: RoutineCreate, current_user: User = None):

        activity = self.home_repo.get_activity_by_id(data.activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")
        if activity.home_id is None:
            raise HTTPException(400, "Activity must be associated with a Home")
        if not current_user or current_user.home_id != activity.home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        if not data.name.strip():
            raise HTTPException(400, "Routine name cannot be empty")
        if data.start_time >= data.end_time:
            raise HTTPException(400, "startTime must be earlier than endTime")

        if self.is_routine_overlapping(activity.home_id, data.days, data.start_time, data.end_time):
            raise HTTPException(400, "Routine time overlaps with existing routine on the same day")

        return self.repo.create_routine(data)

    def get_routine(self, routine_id: int, current_user: User = None):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")
        return routine

    def get_routines_by_home_id(self, home_id: int, current_user: User = None):
        if not current_user or current_user.home_id != home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")
        return self.repo.get_routines_by_home_id(home_id)

    def get_all_routines(self):
        return self.repo.get_all_routines()

    def update_routine(self, routine_id: int, data: RoutineUpdate, current_user: User = None):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")

        if data.activity_id:
            activity = self.home_repo.get_activity_by_id(data.activity_id)
            if not activity:
                raise HTTPException(404, "Activity not found")
            if activity.home_id is None:
                raise HTTPException(400, "Activity must be associated with a Home")
            if not current_user or current_user.home_id != activity.home_id:
                raise HTTPException(403, "Forbidden: You don't have access to this home's routines")
            home_id = activity.home_id
        else:
            activity = self.home_repo.get_activity_by_id(routine.activity_id)
            if not activity:
                raise HTTPException(404, "Original activity not found")
            home_id = activity.home_id
            if not current_user or current_user.home_id != home_id:
                raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        start_time = data.start_time if data.start_time is not None else routine.start_time
        end_time = data.end_time if data.end_time is not None else routine.end_time
        days = data.days if data.days is not None else routine.days

        if start_time >= end_time:
            raise HTTPException(400, "startTime must be earlier than endTime")

        if self.is_routine_overlapping(home_id, days, start_time, end_time, exclude_id=routine_id):
            raise HTTPException(400, "Routine time overlaps with existing routine on the same day")

        return self.repo.update_routine(routine_id, data)

    def delete_routine(self, routine_id: int, current_user: User = None):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")

        activity = self.home_repo.get_activity_by_id(routine.activity_id)
        if not activity:
            raise HTTPException(404, "Activity not found")
        if activity.home_id is None:
            raise HTTPException(400, "Activity must be associated with a Home")
        if not current_user or current_user.home_id != activity.home_id:
            raise HTTPException(403, "Forbidden: You don't have access to this home's routines")

        self.repo.delete_routine(routine_id)