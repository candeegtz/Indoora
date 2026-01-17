from sqlmodel import Session, select
from app.models.routine import DayRoutine, Routine

class RoutineRepository:
    def __init__(self, session: Session):
        self.session = session

    def create_routine(self, routine: Routine) -> Routine:
        self.session.add(routine)
        self.session.commit()
        self.session.refresh(routine)
        return routine
    
    def get_all_routines(self) -> list[Routine]:
        return self.session.exec(select(Routine)).all()

    def get_routine_by_id(self, routine_id: int) -> Routine | None:
        return self.session.get(Routine, routine_id)

    def update_routine(self, routine: Routine) -> Routine:
        db_routine = self.get_routine_by_id(routine.id)
        if not db_routine:
            raise ValueError("Routine not found")
        for field, value in vars(routine).items():
            if field != "id" and value is not None:
                setattr(db_routine, field, value)
        self.session.commit()
        self.session.refresh(routine)
        return routine
    
    def create_dayroutine(self, dayroutine: DayRoutine) -> DayRoutine:
        self.session.add(dayroutine)
        self.session.commit()
        self.session.refresh(dayroutine)
        return dayroutine
    
    def update_dayroutine(self, dayroutine: DayRoutine) -> DayRoutine:
        self.session.add(dayroutine)
        self.session.commit()
        self.session.refresh(dayroutine)
        return dayroutine
    
    def get_dayroutine_by_id(self, dayroutine_id: int) -> DayRoutine | None:
        return self.session.get(DayRoutine, dayroutine_id)

    def get_all_dayroutines(self) -> list[DayRoutine]:
        return self.session.exec(select(DayRoutine)).all()
    