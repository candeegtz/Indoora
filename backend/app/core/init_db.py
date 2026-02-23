# app/core/init_db.py
from sqlmodel import Session, select
from app.models.models import User, UserType
from app.core.security import hash_password

def create_admin_user(session: Session):
    existing = session.exec(
        select(User).where(User.username == "admin")
    ).first()
    
    if existing:
        print("Admin already exists")
        return

    admin = User(
        username="admin",
        name="Admin",
        surnames="Indoora",
        email="admin@indoora.com",
        password_hash=hash_password("admin123456"),
        user_type=UserType.ADMIN,
        home_id=None  
    )
    session.add(admin)
    session.commit()
    print("Admin user created: username='admin', password='admin123456'")