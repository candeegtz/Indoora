from fastapi import APIRouter, Depends, HTTPException 
from sqlmodel import Session 
from Indoora.backend.app.database import get_session 
from Indoora.backend.app.repositories.routine_repository import RoutineRepository
from Indoora.backend.app.schemas.routine import ( RoutineCreate, RoutineUpdate, RoutineRead, DayRoutineCreate, DayRoutineUpdate, DayRoutineRead )

router = APIRouter(prefix="/routines", tags=["Routines"])

@router.post("/", response_model=RoutineRead)
def create_routine(data: RoutineCreate, session: Session = Depends(get_session)):
    repo = RoutineRepository(session)
    return repo.create_routine(data)

@router.get("/{routine_id}", response_model=RoutineRead)
def get_routine_by_id(routine_id: int, session: Session = Depends(get_session)):
    repo = RoutineRepository(session)
    routine = repo.get_routine_by_id(routine_id)
    if not routine:
        raise HTTPException(status_code=404, detail="Routine not found")
    return routine

@router.get("/", response_model=list[RoutineRead]) 
def get_all_routines(session: Session = Depends(get_session)):
    repo = RoutineRepository(session)
    return repo.get_all_routines()

@router.put("/{routine_id}", response_model=RoutineRead)
def update_routine(routine_id: int, data: RoutineUpdate, session: Session = Depends(get_session)):
    repo = RoutineRepository(session)
    return repo.update_routine(routine_id, data)

@router.delete("/{routine_id}")
def delete_routine(routine_id: int, session: Session = Depends(get_session)):
    repo = RoutineRepository(session)
    repo.delete_routine(routine_id)
    return {"message": "Routine deleted"}