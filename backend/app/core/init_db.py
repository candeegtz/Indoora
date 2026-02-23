# app/core/init_db.py
from sqlmodel import Session, select
from app.models.models import Home, User, UserType
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

def create_initial_data(session: Session):
    existing_supervisor = session.exec(
        select(User).where(User.username == "supervisor_prueba")
    ).first()

    if existing_supervisor:
        print("Supervisor already exists")
        return

    home = Home(
        name="Home Prueba"
    )
    session.add(home)
    session.commit()
    session.refresh(home)

    supervisor = User(
        username="supervisor_prueba",
        name="Supervisor",  
        surnames="Prueba",
        email="supervisor_prueba@indoora.com",
        password_hash=hash_password("supervisor123456"),
        user_type=UserType.SUPERVISOR_CREATOR,
        home_id=home.id  
    )
    session.add(supervisor)
    session.commit()

    subject = User(
        username="subject_prueba",
        name="Subject",
        surnames="Prueba",
        email="subject_prueba@indoora.com",
        password_hash=hash_password("subject123456"),
        user_type=UserType.SUBJECT,
        home_id=home.id  
    )
    session.add(subject)
    session.commit()

    print("Initial data created")