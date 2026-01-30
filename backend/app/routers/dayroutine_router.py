from app.services.routine_service import RoutineService
from fastapi import APIRouter, Depends, HTTPException 
from sqlmodel import Session 
from app.database import get_session 
from app.schemas.routine import (
    DayRoutineCreate, DayRoutineUpdate, DayRoutineRead
)
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/dayroutines", tags=["DayRoutines"])


@router.post("/", response_model=DayRoutineRead)
def create_dayroutine(
    data: DayRoutineCreate,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.create_dayroutine(data)


@router.get("/{dayroutine_id}", response_model=DayRoutineRead)
def get_dayroutine_by_id(
    dayroutine_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = RoutineService(session)
    dayroutine = repo.get_dayroutine_by_id(dayroutine_id)
    if not dayroutine:
        raise HTTPException(status_code=404, detail="DayRoutine not found")
    return dayroutine


@router.get("/", response_model=list[DayRoutineRead])
def get_all_dayroutines(
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.get_all_dayroutines()


@router.put("/{dayroutine_id}", response_model=DayRoutineRead)
def update_dayroutine(
    dayroutine_id: int,
    data: DayRoutineUpdate,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = RoutineService(session)
    return repo.update_dayroutine(dayroutine_id, data)


@router.delete("/{dayroutine_id}")
def delete_dayroutine(
    dayroutine_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = RoutineService(session)
    repo.delete_dayroutine(dayroutine_id)
    return {"message": "DayRoutine deleted"}
