from app.schemas.device import EmisorDeviceCreate, EmisorDeviceUpdate, ReceptorDeviceCreate, ReceptorDeviceUpdate
from sqlmodel import Session, select
from app.models.models import EmisorDevice, ReceptorDevice


class DeviceRepository:
    def __init__(self, session: Session):
        self.session = session

    # ------------EmisorDevice (pulsera)------------

    def create_emisor(self, device: EmisorDeviceCreate) -> EmisorDevice:
        device = EmisorDevice(
            name = device.name,
            macAddress = device.macAddress, 
            user_id = device.user_id
        )

        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device
    
    def get_emisor_by_id(self, device_id: int) -> EmisorDevice | None:
        return self.session.get(EmisorDevice, device_id)

    def get_all_emisor(self) -> list[EmisorDevice]:
        return self.session.exec(select(EmisorDevice)).all()
    
    def update_emisor(self, device_id: int, device: EmisorDeviceUpdate) -> EmisorDevice:
        device = self.get_emisor_by_id(device_id)
        if not device:
            raise ValueError("EmisorDevice not found")

        update_data = device.dict(exclude_unset=True)

        for key, value in update_data.items():
            setattr(device, key, value)
        
        self.session.commit()
        self.session.refresh(device)
        return device
    
    def delete_emisor(self, device_id: int):
        device = self.get_emisor_by_id(device_id)
        if not device:
            raise ValueError("EmisorDevice not found")
        
        self.session.delete(device)
        self.session.commit()


    # ------------ReceptorDevice (ESP32)------------

    def create_receptor(self, device: ReceptorDeviceCreate) -> ReceptorDevice:
        device = ReceptorDevice(
            macAddress = device.macAddress,
            room_id = device.room_id
        )

        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device
    

    def get_receptor_by_id(self, device_id: int) -> ReceptorDevice | None:
        return self.session.get(ReceptorDevice, device_id)  
    
    def get_all_receptor(self) -> list[ReceptorDevice]:
        return self.session.exec(select(ReceptorDevice)).all()
    
    def update_receptor(self, device_id: int, device: ReceptorDeviceUpdate) -> ReceptorDevice:
        device = self.get_receptor_by_id(device_id)
        if not device:
            raise ValueError("ReceptorDevice not found")    
        
        updated_data = device.dict(exclude_unset=True)

        for key, value in updated_data.items():
            setattr(device, key, value)

        self.session.commit()
        self.session.refresh(device)
        return device
    
    def delete_receptor(self, device_id: int):
        device = self.get_receptor_by_id(device_id)
        if not device:
            raise ValueError("ReceptorDevice not found")
        
        self.session.delete(device)
        self.session.commit()