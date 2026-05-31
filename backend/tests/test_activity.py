import pytest
from app.models.models import RoomType


@pytest.fixture
def setup_activity_data(client):
    """Setup: crear supervisor, login, y home con rooms y positions"""
    
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "activity_supervisor",
            "name": "Activity",
            "surnames": "Supervisor",
            "email": "activity@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Activity Test Home"
        }
    )
    assert register_response.status_code == 200
    supervisor = register_response.json()
    home_id = supervisor["homeId"]
    
    login_response = client.post(
        "/auth/login",
        json={"username": "activity_supervisor", "password": "123456"}
    )
    assert login_response.status_code == 200
    token = login_response.json()["access_token"]
    
    kitchen = client.post(
        "/homes/rooms",
        json={
            "name": "Cocina",
            "roomType": "KITCHEN",
            "homeId": home_id
        },
        headers={"Authorization": f"Bearer {token}"}
    ).json()
    
    living_room = client.post(
        "/homes/rooms",
        json={
            "name": "Salón",
            "roomType": "LIVING_ROOM",
            "homeId": home_id
        },
        headers={"Authorization": f"Bearer {token}"}
    ).json()
    
    position1 = client.post(
        "/homes/positions",
        json={
            "name": "Puerta Cocina",
            "roomId": kitchen["id"]
        },
        headers={"Authorization": f"Bearer {token}"}
    ).json()
    
    position2 = client.post(
        "/homes/positions",
        json={
            "name": "Sofá Salón",
            "roomId": living_room["id"]
        },
        headers={"Authorization": f"Bearer {token}"}
    ).json()
    
    return {
        "token": token,
        "home_id": home_id,
        "kitchen_id": kitchen["id"],
        "living_room_id": living_room["id"],
        "position1_id": position1["id"],
        "position2_id": position2["id"]
    }


def test_create_activity_success(client, setup_activity_data):
    """Test crear una actividad exitosamente"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    response = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    )
    
    assert response.status_code == 200
    activity = response.json()
    assert activity["name"] == "Cocinar"
    assert activity["homeId"] == data["home_id"]
    assert activity["id"] is not None


def test_create_activity_multiple_positions(client, setup_activity_data):
    """Test crear actividad con múltiples posiciones"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    response = client.post(
        "/activities/",
        json={
            "name": "Limpiar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"], data["position2_id"]]
        },
        headers=headers
    )
    
    assert response.status_code == 200
    activity = response.json()
    assert activity["name"] == "Limpiar"


def test_create_activity_unauthorized(client, setup_activity_data):
    """Test crear actividad sin autenticación"""
    data = setup_activity_data
    
    response = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        }
    )
    
    assert response.status_code == 401


def test_get_activities_by_home(client, setup_activity_data):
    """Test obtener actividades por home"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    )
    
    client.post(
        "/activities/",
        json={
            "name": "Limpiar",
            "homeId": data["home_id"],
            "positionIds": [data["position2_id"]]
        },
        headers=headers
    )
    
    response = client.get(
        f"/activities/home/{data['home_id']}",
        headers=headers
    )
    
    assert response.status_code == 200
    activities = response.json()
    assert len(activities) >= 2


def test_get_activity_with_positions(client, setup_activity_data):
    """Test obtener actividad con sus posiciones asociadas"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    created = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"], data["position2_id"]]
        },
        headers=headers
    ).json()
    
    activity_id = created["id"]
    
    response = client.get(
        f"/activities/{activity_id}",
        headers=headers
    )
    
    assert response.status_code == 200
    activity = response.json()
    assert activity["name"] == "Cocinar"
    assert "positionIds" in activity
    assert len(activity["positionIds"]) == 2


def test_get_activity_not_found(client, setup_activity_data):
    """Test obtener actividad que no existe"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    response = client.get(
        "/activities/9999",
        headers=headers
    )
    
    assert response.status_code == 404


def test_update_activity_name(client, setup_activity_data):
    """Test actualizar nombre de actividad"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    created = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    ).json()
    
    activity_id = created["id"]
    
    response = client.put(
        f"/activities/{activity_id}",
        json={
            "name": "Preparar Comida"
        },
        headers=headers
    )
    
    assert response.status_code == 200
    updated = response.json()
    assert updated["name"] == "Preparar Comida"
    assert updated["id"] == activity_id


def test_update_activity_positions(client, setup_activity_data):
    """Test actualizar posiciones de actividad"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    created = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    ).json()
    
    activity_id = created["id"]
    
    response = client.put(
        f"/activities/{activity_id}",
        json={
            "name": "Cocinar",
            "positionIds": [data["position1_id"], data["position2_id"]]
        },
        headers=headers
    )
    
    assert response.status_code == 200


def test_update_activity_name_and_positions(client, setup_activity_data):
    """Test actualizar nombre y posiciones"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    created = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    ).json()
    
    activity_id = created["id"]
    
    response = client.put(
        f"/activities/{activity_id}",
        json={
            "name": "Limpiar",
            "positionIds": [data["position2_id"]]
        },
        headers=headers
    )
    
    assert response.status_code == 200
    updated = response.json()
    assert updated["name"] == "Limpiar"


def test_delete_activity_success(client, setup_activity_data):
    """Test eliminar actividad exitosamente"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    created = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    ).json()
    
    activity_id = created["id"]
    
    response = client.delete(
        f"/activities/{activity_id}",
        headers=headers
    )
    
    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]
    
    get_response = client.get(
        f"/activities/{activity_id}",
        headers=headers
    )
    assert get_response.status_code == 404


def test_delete_activity_not_found(client, setup_activity_data):
    """Test eliminar actividad que no existe"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    response = client.delete(
        "/activities/9999",
        headers=headers
    )
    
    assert response.status_code == 404


def test_delete_activity_unauthorized(client, setup_activity_data):
    """Test eliminar actividad sin autenticación"""
    
    response = client.delete(
        "/activities/1"
    )
    
    assert response.status_code == 401


def test_activity_lifecycle(client, setup_activity_data):
    """Test ciclo completo: crear → leer → actualizar → eliminar"""
    data = setup_activity_data
    headers = {"Authorization": f"Bearer {data['token']}"}
    
    create_response = client.post(
        "/activities/",
        json={
            "name": "Actividad Test",
            "homeId": data["home_id"],
            "positionIds": [data["position1_id"]]
        },
        headers=headers
    )
    assert create_response.status_code == 200
    activity = create_response.json()
    activity_id = activity["id"]
    
    read_response = client.get(
        f"/activities/{activity_id}",
        headers=headers
    )
    assert read_response.status_code == 200
    assert read_response.json()["name"] == "Actividad Test"
    
    update_response = client.put(
        f"/activities/{activity_id}",
        json={
            "name": "Actividad Actualizada",
            "positionIds": [data["position1_id"], data["position2_id"]]
        },
        headers=headers
    )
    assert update_response.status_code == 200
    assert update_response.json()["name"] == "Actividad Actualizada"
    
    delete_response = client.delete(
        f"/activities/{activity_id}",
        headers=headers
    )
    assert delete_response.status_code == 200
    
    final_response = client.get(
        f"/activities/{activity_id}",
        headers=headers
    )
    assert final_response.status_code == 404