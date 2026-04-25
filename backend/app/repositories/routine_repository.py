from app.schemas.routine import RoutineCreate, RoutineUpdate
from sqlmodel import Session, select
from app.models.models import Activity, Routine

class RoutineRepository:
    def __init__(self, session: Session):
        self.session = session

    # ------------Routine------------

    def create_routine(self, data: RoutineCreate) -> Routine:
        # Usamos los nombres reales de los campos del modelo (snake_case)
        routine = Routine(
            name=data.name,
            description=data.description,
            start_time=data.start_time,      # ← corregido
            end_time=data.end_time,          # ← corregido
            activity_id=data.activity_id
        )
        # days se asigna automáticamente desde el esquema si está en Routine
        # Asegúrate de que el modelo Routine tenga el campo days (JSON)
        routine.days = data.days
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
        all_routines = self.get_routines_by_home_id(home_id)
        # Filtra aquellas que tengan algún día en común
        return [r for r in all_routines if any(d in r.days for d in days)]
    
    def get_routines_by_home_id(self, home_id: int) -> list[Routine]:
        return self.session.exec(
            select(Routine).join(Activity).where(Activity.home_id == home_id)
        ).all()