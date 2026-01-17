from sqlmodel import Session, select
from app.models.device import EmisorDevice

class DeviceRepository:
    def __init__(self, session: Session):
        self.session = session

    def create(self, device: EmisorDevice) -> EmisorDevice:
        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device
    
    def update(self, device: EmisorDevice) -> EmisorDevice:
        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device

    def get_all(self) -> list[EmisorDevice]:
        return self.session.exec(select(EmisorDevice)).all()

    def get_by_id(self, device_id: int) -> EmisorDevice | None:
        return self.session.get(EmisorDevice, device_id)