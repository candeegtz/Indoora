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


    # ------------Routine------------

    def create_routine(self, data: RoutineCreate):
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

        return self.repo.create_routine(data)

    def get_routine(self, routine_id: int):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")
        return routine

    def get_all_routines(self):
        return self.repo.get_all_routines()

    def update_routine(self, routine_id: int, data: RoutineUpdate):
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

        return self.repo.update_routine(routine_id, data)

    def delete_routine(self, routine_id: int):
        routine = self.repo.get_routine_by_id(routine_id)
        if not routine:
            raise HTTPException(404, "Routine not found")

        self.repo.delete_routine(routine_id)
