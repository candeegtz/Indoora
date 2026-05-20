from sqlmodel import Session, select
from app.models.models import Alert
from typing import List

class AlertRepository:
    def __init__(self, session: Session):
        self.session = session

    def get_by_id(self, alert_id: int) -> Alert | None:
        return self.session.get(Alert, alert_id)

    def save_alert(self, alert: Alert) -> Alert:
        self.session.add(alert)
        self.session.commit()
        self.session.refresh(alert)
        return alert

    def get_unread_by_home(self, home_id: int) -> List[Alert]:
        return self.session.exec(
            select(Alert)
            .where(Alert.home_id == home_id, Alert.is_read == False)
            .order_by(Alert.timestamp.desc())
        ).all()

    def mark_as_read(self, alert_id: int) -> None:
        alert = self.session.get(Alert, alert_id)
        if alert:
            alert.is_read = True
            self.session.add(alert)
            self.session.commit()