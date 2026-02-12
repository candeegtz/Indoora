from app.services.routine_service import RoutineService
from fastapi import APIRouter, Depends, HTTPException 
from sqlmodel import Session 
from app.database import get_session 
from app.schemas.routine import (
    RoutineCreate, RoutineUpdate, RoutineRead,
)
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/routines", tags=["Routines"])


@router.post("/", response_model=RoutineRead)
def create_routine(
    data: RoutineCreate,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.create_routine(data, current_user)


@router.get("/{routine_id}", response_model=RoutineRead)
def get_routine_by_id(
    routine_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    routine = repo.get_routine_by_id(routine_id)
    if not routine:
        raise HTTPException(status_code=404, detail="Routine not found")
    return routine


@router.get("/", response_model=list[RoutineRead])
def get_all_routines(
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.get_all_routines()

@router.get("/home/{home_id}", response_model=list[RoutineRead])
def get_routines_by_home_id(
    home_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.get_routines_by_home_id(home_id, current_user)

@router.put("/{routine_id}", response_model=RoutineRead)
def update_routine(
    routine_id: int,
    data: RoutineUpdate,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.update_routine(routine_id, data, current_user)


@router.delete("/{routine_id}")
def delete_routine(
    routine_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    repo = RoutineService(session)
    repo.delete_routine(routine_id)
    return {"message": "Routine deleted"}
