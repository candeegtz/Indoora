from app.schemas.routine import RoutineCreate, RoutineUpdate
from sqlmodel import Session, select
from app.models.models import Routine


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
    
    def get_routines_by_home_id_and_days(self, home_id: int, days: list[str]) -> list[Routine]:
        return self.session.exec(select(Routine).where(Routine.activity.home_id == home_id and Routine.days.overlap(days))).all()
    
    def get_routines_by_home_id(self, home_id: int) -> list[Routine]:
        return self.session.exec(select(Routine).where(Routine.activity.home_id == home_id)).all()