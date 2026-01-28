from Indoora.backend.app.schemas.home import ActivityCreate, ActivityRead, ActivityUpdate
from Indoora.backend.app.services.home_service import HomeService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from Indoora.backend.app.database import get_session


router = APIRouter(prefix="/activities", tags=["Activities"])


@router.post("/", response_model=ActivityRead)
def create_activity(data: ActivityCreate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.create_activity(data)


@router.get("/{activity_id}", response_model=ActivityRead)
def get_activity(activity_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    activity = repo.get_activity_by_id(activity_id)
    if not activity:
        raise HTTPException(404, "Activity not found")
    return activity


@router.get("/", response_model=list[ActivityRead])
def get_all_activities(session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.get_all_activities()


@router.put("/{activity_id}", response_model=ActivityRead)
def update_activity(activity_id: int, data: ActivityUpdate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.update_activity(activity_id, data)


@router.delete("/{activity_id}")
def delete_activity(activity_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    repo.delete_activity(activity_id)
    return {"message": "Activity deleted successfully"}
