from app.schemas.home import ActivityCreate, ActivityRead, ActivityUpdate, ActivityWithPositionsRead
from app.services.home_service import HomeService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from app.database import get_session
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/activities", tags=["Activities"])


@router.post("/", response_model=ActivityRead)
def create_activity(
    data: ActivityCreate,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = HomeService(session)
    return repo.create_activity(data, user)

@router.get("/", response_model=list[ActivityRead])
def get_all_activities(
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = HomeService(session)
    return repo.get_all_activities()


@router.put("/{activity_id}", response_model=ActivityRead)
def update_activity(
    activity_id: int,
    data: ActivityUpdate,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = HomeService(session)
    return repo.update_activity(activity_id, data, user)


@router.delete("/{activity_id}")
def delete_activity(
    activity_id: int,
    session: Session = Depends(get_session),
    user = Depends(get_current_user)
):
    repo = HomeService(session)
    repo.delete_activity(activity_id, user)
    return {"message": "Activity deleted successfully"}


@router.get("/home/{home_id}", response_model=list[ActivityRead])
def get_activities_by_home(
    home_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = HomeService(session)   
    return service.get_activities_by_home_id(home_id, current_user)

@router.get("/{activity_id}", response_model=ActivityWithPositionsRead)
def get_activity(
    activity_id: int,
    session: Session = Depends(get_session),
    current_user = Depends(get_current_user)
):
    service = HomeService(session)
    activity = service.get_activity_with_positions(activity_id, current_user)
    return activity