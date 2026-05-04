from app.services.routine_service import RoutineService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from app.database import get_session
from app.schemas.routine import RoutineCreate, RoutineUpdate, RoutineRead
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/routines", tags=["Routines"])

@router.post("", response_model=RoutineRead)   # ← sin barra al final para evitar redirección 307
def create_routine(
    data: RoutineCreate,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    return service.create_routine(data, current_user)

@router.get("/{routine_id}", response_model=RoutineRead)
def get_routine_by_id(
    routine_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    routine = service.get_routine(routine_id, current_user)
    if not routine:
        raise HTTPException(status_code=404, detail="Routine not found")
    return routine

@router.get("", response_model=list[RoutineRead])   # ← sin barra
def get_all_routines(
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    return service.get_all_routines()

@router.get("/home/{home_id}", response_model=list[RoutineRead])
def get_routines_by_home_id(
    home_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    return service.get_routines_by_home_id(home_id, current_user)

@router.put("/{routine_id}", response_model=RoutineRead)
def update_routine(
    routine_id: int,
    data: RoutineUpdate,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    return service.update_routine(routine_id, data, current_user)

@router.delete("/{routine_id}")
def delete_routine(
    routine_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = RoutineService(session)
    service.delete_routine(routine_id, current_user)
    return {"message": "Routine deleted"}