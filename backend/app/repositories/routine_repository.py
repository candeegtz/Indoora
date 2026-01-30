from app.schemas.routine import DayRoutineCreate, DayRoutineUpdate, RoutineCreate, RoutineUpdate
from sqlmodel import Session, select
from app.models.routine import DayRoutine, Routine

class RoutineRepository:
    def __init__(self, session: Session):
        self.session = session

    # ------------Routine------------

    def create_routine(self, data: RoutineCreate) -> Routine:
        routine = Routine(
            name = data.name,
            description = data.description,
            startTime = data.startTime,
            endTime = data.endTime,
            activity_id = data.activity_id
        )

        self.session.add(routine)
        self.session.commit()
        self.session.refresh(routine)
        return routine
    
    def get_routine_by_id(self, routine_id: int) -> Routine | None:
        return self.session.get(Routine, routine_id)

    def get_all_routines(self) -> list[Routine]:
        return self.session.exec(select(Routine)).all()

    def update_routine(self, routine_id: int, data: RoutineUpdate) -> Routine:
        routine = self.get_routine_by_id(routine_id)
        if not routine:
            raise ValueError("Routine not found")

        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(routine, key, value)

        self.session.commit()
        self.session.refresh(routine)

        return routine
    
    def delete_routine(self, routine_id: int):
        routine = self.get_routine_by_id(routine_id)
        if not routine:
            raise ValueError("Routine not found")
        
        self.session.delete(routine)
        self.session.commit()
    

    # ------------DayRoutine------------
    
    def create_dayroutine(self, data: DayRoutineCreate) -> DayRoutine:
        dayroutine = DayRoutine(
            day = data.day,
            home_id = data.home_id
        )

        self.session.add(dayroutine)
        self.session.commit()
        self.session.refresh(dayroutine)
        return dayroutine
    
    def get_dayroutine_by_id(self, dayroutine_id: int) -> DayRoutine | None:
        return self.session.get(DayRoutine, dayroutine_id)

    def get_all_dayroutines(self) -> list[DayRoutine]:
        return self.session.exec(select(DayRoutine)).all()
    
    def update_dayroutine(self, dayroutine_id: int, data: DayRoutineUpdate) -> DayRoutine:
        dayroutine = self.get_dayroutine_by_id(dayroutine_id)
        if not dayroutine:
            raise ValueError("DayRoutine not found")
        
        update_data = data.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(dayroutine, key, value)

        self.session.commit()
        self.session.refresh(dayroutine)
        return dayroutine
    
    def delete_dayroutine(self, dayroutine_id: int):
        dayroutine = self.get_dayroutine_by_id(dayroutine_id)
        if not dayroutine:
            raise ValueError("DayRoutine not found")
        
        self.session.delete(dayroutine)
        self.session.commit()
    

    