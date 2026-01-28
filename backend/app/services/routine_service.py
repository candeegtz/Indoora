from fastapi import HTTPException
from sqlmodel import Session

from Indoora.backend.app.repositories.routine_repository import RoutineRepository
from Indoora.backend.app.repositories.home_repository import HomeRepository
from Indoora.backend.app.repositories.activity_repository import ActivityRepository

from Indoora.backend.app.schemas.routine import (
    RoutineCreate, RoutineUpdate,
    DayRoutineCreate, DayRoutineUpdate
)


class RoutineService:
    def __init__(self, session: Session):
        self.repo = RoutineRepository(session)
        self.home_repo = HomeRepository(session)
        self.activity_repo = ActivityRepository(session)


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


    # ------------DayRoutine------------

    def create_dayroutine(self, data: DayRoutineCreate):
        # home debe existir
        home = self.home_repo.get_home_by_id(data.home_id)
        if not home:
            raise HTTPException(404, "Home not found")

        # day debe estar entre 1 y 7
        if not (1 <= data.day <= 7):
            raise HTTPException(400, "Day must be between 1 and 7")

        return self.repo.create_dayroutine(data)

    def get_dayroutine(self, dayroutine_id: int):
        dr = self.repo.get_dayroutine_by_id(dayroutine_id)
        if not dr:
            raise HTTPException(404, "DayRoutine not found")
        return dr

    def get_all_dayroutines(self):
        return self.repo.get_all_dayroutines()

    def update_dayroutine(self, dayroutine_id: int, data: DayRoutineUpdate):
        dr = self.repo.get_dayroutine_by_id(dayroutine_id)
        if not dr:
            raise HTTPException(404, "DayRoutine not found")

        # home si se actualiza
        if data.home_id:
            home = self.home_repo.get_home_by_id(data.home_id)
            if not home:
                raise HTTPException(404, "Home not found")

        # day si se actualiza
        if data.day and not (1 <= data.day <= 7):
            raise HTTPException(400, "Day must be between 1 and 7")

        return self.repo.update_dayroutine(dayroutine_id, data)

    def delete_dayroutine(self, dayroutine_id: int):
        dr = self.repo.get_dayroutine_by_id(dayroutine_id)
        if not dr:
            raise HTTPException(404, "DayRoutine not found")

        self.repo.delete_dayroutine(dayroutine_id)
