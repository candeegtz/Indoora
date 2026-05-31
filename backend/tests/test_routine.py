import pytest
from datetime import time


@pytest.fixture
def setup_routine_data(client):
    """Setup: crear supervisor, login, home con activities"""
    
    # 1. Registrar supervisor
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "routine_supervisor",
            "name": "Routine",
            "surnames": "Supervisor",
            "email": "routine@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Routine Test Home"
        }
    )
    assert register_response.status_code == 200
    supervisor = register_response.json()
    home_id = supervisor["homeId"]
    
    # 2. Login
    login_response = client.post(
        "/auth/login",
        json={"username": "routine_supervisor", "password": "123456"}
    )
    assert login_response.status_code == 200
    token = login_response.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    
    # 3. Crear rooms
    kitchen = client.post(
        "/homes/rooms",
        json={
            "name": "Cocina",
            "roomType": "KITCHEN",
            "homeId": home_id
        },
        headers=headers
    ).json()
    
    living_room = client.post(
        "/homes/rooms",
        json={
            "name": "Salón",
            "roomType": "LIVING_ROOM",
            "homeId": home_id
        },
        headers=headers
    ).json()
    
    # 4. Crear positions
    position1 = client.post(
        "/homes/positions",
        json={
            "name": "Puerta Cocina",
            "roomId": kitchen["id"]
        },
        headers=headers
    ).json()
    
    position2 = client.post(
        "/homes/positions",
        json={
            "name": "Sofá Salón",
            "roomId": living_room["id"]
        },
        headers=headers
    ).json()
    
    # 5. Crear activities
    activity1 = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": home_id,
            "positionIds": [position1["id"]]
        },
        headers=headers
    ).json()
    
    activity2 = client.post(
        "/activities/",
        json={
            "name": "Ver TV",
            "homeId": home_id,
            "positionIds": [position2["id"]]
        },
        headers=headers
    ).json()
    
    return {
        "token": token,
        "headers": headers,
        "home_id": home_id,
        "activity1_id": activity1["id"],
        "activity2_id": activity2["id"]
    }


def test_create_routine_success(client, setup_routine_data):
    """Test crear rutina exitosamente"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Cocinar Mañana",
            "description": "Cocinar en la mañana",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    routine = response.json()
    assert routine["name"] == "Cocinar Mañana"
    assert routine["activityId"] == data["activity1_id"]
    assert routine["id"] is not None


def test_create_routine_all_days(client, setup_routine_data):
    """Test crear rutina para todos los días"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina Diaria",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    routine = response.json()
    assert len(routine["days"]) == 7


def test_create_routine_invalid_times(client, setup_routine_data):
    """Test crear rutina con horarios inválidos (start >= end)"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina Inválida",
            "startTime": "10:00:00",
            "endTime": "10:00:00",  # Mismo horario
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 400
    assert "startTime must be earlier than endTime" in response.json()["detail"]


def test_create_routine_start_after_end(client, setup_routine_data):
    """Test crear rutina con start después de end"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina Inválida",
            "startTime": "15:00:00",
            "endTime": "10:00:00",  # End antes de start
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 400
    assert "startTime must be earlier than endTime" in response.json()["detail"]


def test_create_routine_empty_name(client, setup_routine_data):
    """Test crear rutina con nombre vacío"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "   ",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 400
    assert "Routine name cannot be empty" in response.json()["detail"]


def test_create_routine_activity_not_found(client, setup_routine_data):
    """Test crear rutina con actividad inexistente"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": 9999
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 404
    assert "Activity not found" in response.json()["detail"]


def test_create_routine_unauthorized(client, setup_routine_data):
    """Test crear rutina sin autenticación"""
    data = setup_routine_data
    
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        }
    )
    
    assert response.status_code == 401


def test_create_routine_overlapping(client, setup_routine_data):
    """Test crear rutina que se superpone con otra existente"""
    data = setup_routine_data
    
    # Crear primera rutina
    client.post(
        "/routines/",
        json={
            "name": "Rutina 1",
            "startTime": "08:00:00",
            "endTime": "10:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    # Intentar crear rutina que se superpone
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina Superpuesta",
            "startTime": "09:00:00",
            "endTime": "11:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity2_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 400
    assert "overlaps" in response.json()["detail"]


def test_create_routine_different_days_no_overlap(client, setup_routine_data):
    """Test crear rutinas en días diferentes (sin superposición)"""
    data = setup_routine_data
    
    # Crear rutina el lunes
    client.post(
        "/routines/",
        json={
            "name": "Rutina Lunes",
            "startTime": "08:00:00",
            "endTime": "10:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    # Crear rutina el martes (no hay superposición)
    response = client.post(
        "/routines/",
        json={
            "name": "Rutina Martes",
            "startTime": "08:00:00",
            "endTime": "10:00:00",
            "days": ["TUESDAY"],
            "activityId": data["activity2_id"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200


def test_get_routine_success(client, setup_routine_data):
    """Test obtener rutina por ID"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Rutina Test",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Obtener rutina
    response = client.get(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    routine = response.json()
    assert routine["name"] == "Rutina Test"
    assert routine["id"] == routine_id


def test_get_routine_not_found(client, setup_routine_data):
    """Test obtener rutina inexistente"""
    data = setup_routine_data
    
    response = client.get(
        "/routines/9999",
        headers=data["headers"]
    )
    
    assert response.status_code == 404
    assert "Routine not found" in response.json()["detail"]


def test_get_all_routines(client, setup_routine_data):
    """Test obtener todas las rutinas"""
    data = setup_routine_data
    
    # Crear varias rutinas
    for i in range(3):
        client.post(
            "/routines/",
            json={
                "name": f"Rutina {i+1}",
                "startTime": f"0{i}:00:00",
                "endTime": f"0{i+1}:00:00",
                "days": ["MONDAY"],
                "activityId": data["activity1_id"]
            },
            headers=data["headers"]
        )
    
    response = client.get(
        "/routines",
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    routines = response.json()
    assert len(routines) >= 3


def test_get_routines_by_home(client, setup_routine_data):
    """Test obtener rutinas por home"""
    data = setup_routine_data
    
    # Crear rutinas
    client.post(
        "/routines/",
        json={
            "name": "Rutina Home",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    
    response = client.get(
        f"/routines/home/{data['home_id']}",
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    routines = response.json()
    assert len(routines) >= 1


def test_get_routines_by_home_unauthorized(client, setup_routine_data):
    """Test obtener rutinas de home sin acceso"""
    data = setup_routine_data
    
    # Crear otro usuario
    other_user = client.post(
        "/auth/register-supervisor",
        json={
            "username": "other_supervisor",
            "name": "Other",
            "surnames": "User",
            "email": "other@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Other Home"
        }
    ).json()
    
    other_home_id = other_user["homeId"]
    
    # Login como otro usuario
    other_login = client.post(
        "/auth/login",
        json={"username": "other_supervisor", "password": "123456"}
    ).json()
    
    other_token = other_login["access_token"]
    other_headers = {"Authorization": f"Bearer {other_token}"}
    
    # Intentar acceder a home ajeno
    response = client.get(
        f"/routines/home/{data['home_id']}",
        headers=other_headers
    )
    
    assert response.status_code == 403
    assert "Forbidden" in response.json()["detail"]


def test_update_routine_name(client, setup_routine_data):
    """Test actualizar nombre de rutina"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Nombre Original",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Actualizar nombre
    response = client.put(
        f"/routines/{routine_id}",
        json={
            "name": "Nombre Actualizado"
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    updated = response.json()
    assert updated["name"] == "Nombre Actualizado"


def test_update_routine_times(client, setup_routine_data):
    """Test actualizar horarios de rutina"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Rutina",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Actualizar horarios
    response = client.put(
        f"/routines/{routine_id}",
        json={
            "startTime": "10:00:00",
            "endTime": "11:00:00"
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200


def test_update_routine_days(client, setup_routine_data):
    """Test actualizar días de rutina"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Rutina",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Actualizar días
    response = client.put(
        f"/routines/{routine_id}",
        json={
            "days": ["TUESDAY", "WEDNESDAY", "THURSDAY"]
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    updated = response.json()
    assert "TUESDAY" in updated["days"]


def test_update_routine_invalid_times(client, setup_routine_data):
    """Test actualizar con horarios inválidos"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Rutina",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Intentar actualizar con times inválidos
    response = client.put(
        f"/routines/{routine_id}",
        json={
            "startTime": "15:00:00",
            "endTime": "14:00:00"  # end antes de start
        },
        headers=data["headers"]
    )
    
    assert response.status_code == 400
    assert "startTime must be earlier than endTime" in response.json()["detail"]


def test_update_routine_not_found(client, setup_routine_data):
    """Test actualizar rutina inexistente"""
    data = setup_routine_data
    
    response = client.put(
        "/routines/9999",
        json={"name": "Actualizada"},
        headers=data["headers"]
    )
    
    assert response.status_code == 404


def test_delete_routine_success(client, setup_routine_data):
    """Test eliminar rutina exitosamente"""
    data = setup_routine_data
    
    # Crear rutina
    created = client.post(
        "/routines/",
        json={
            "name": "Rutina a Eliminar",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    ).json()
    
    routine_id = created["id"]
    
    # Eliminar
    response = client.delete(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    
    assert response.status_code == 200
    assert "deleted" in response.json()["message"]
    
    # Verificar que fue eliminada
    get_response = client.get(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    assert get_response.status_code == 404


def test_delete_routine_not_found(client, setup_routine_data):
    """Test eliminar rutina inexistente"""
    data = setup_routine_data
    
    response = client.delete(
        "/routines/9999",
        headers=data["headers"]
    )
    
    assert response.status_code == 404


def test_routine_lifecycle(client, setup_routine_data):
    """Test ciclo completo: crear → leer → actualizar → eliminar"""
    data = setup_routine_data
    
    # 1. Crear
    create_response = client.post(
        "/routines/",
        json={
            "name": "Rutina Ciclo",
            "description": "Descripción inicial",
            "startTime": "08:00:00",
            "endTime": "09:00:00",
            "days": ["MONDAY", "TUESDAY"],
            "activityId": data["activity1_id"]
        },
        headers=data["headers"]
    )
    assert create_response.status_code == 200
    routine = create_response.json()
    routine_id = routine["id"]
    
    # 2. Leer
    read_response = client.get(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    assert read_response.status_code == 200
    assert read_response.json()["name"] == "Rutina Ciclo"
    
    # 3. Actualizar
    update_response = client.put(
        f"/routines/{routine_id}",
        json={
            "name": "Rutina Actualizada",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY"]
        },
        headers=data["headers"]
    )
    assert update_response.status_code == 200
    assert update_response.json()["name"] == "Rutina Actualizada"
    
    # 4. Eliminar
    delete_response = client.delete(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    assert delete_response.status_code == 200
    
    # 5. Verificar eliminación
    final_response = client.get(
        f"/routines/{routine_id}",
        headers=data["headers"]
    )
    assert final_response.status_code == 404