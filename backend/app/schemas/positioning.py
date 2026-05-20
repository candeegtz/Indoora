from pydantic import BaseModel
from datetime import datetime

# Posición estable definida por el motor tras varias lecturas en la misma posición
class StablePosition(BaseModel):
    home_id: int
    room: str
    position: str
    timestamp: datetime
    deviation_timer: bool = False  # Indica si el temporizador de desviación está activo para esta posición estable