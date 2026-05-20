from sqlmodel import Session, select
from sqlalchemy import cast
from sqlalchemy.dialects.postgresql import JSONB 
from datetime import time
from typing import List
from app.models.models import Routine, Activity

class PositioningRepository:
    def __init__(self, session: Session):
        self.session = session

    def get_active_routines(self, home_id: int, current_time: time, current_day: str) -> List[Routine]:
        target_day = current_day.upper()
        target_jsonb = cast([target_day], JSONB)
        
        stmt = select(Routine).join(Activity).where(Activity.home_id == home_id).where(Routine.days.contains(target_jsonb)).where(Routine.start_time <= current_time).where(Routine.end_time >= current_time)        
        result = self.session.exec(stmt).all()
        return result

    def get_expected_positions(self, routine: Routine) -> list[tuple[str, str]]:
        '''Devuelve lista de habitación/posición de la rutina esperada'''
        expected = []
        for pos in routine.activity.positions:
            expected.append((pos.room.name, pos.name))
        return expected