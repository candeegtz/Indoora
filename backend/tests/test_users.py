import pytest
from app.models.models import UserType

@pytest.fixture
def auth_header(client):
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "admin",
            "name": "Admin",
            "surnames": "Root",
            "email": "admin@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaAdmin"
        }
    )
    
    assert register_response.status_code == 200

    login_response = client.post(
        "/auth/login",
        json={"email": "admin@gmail.com", "password": "123456"}
    )

    token = login_response.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


@pytest.fixture
def create_supervisor_creator_with_subject(client, auth_header):
    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Subject",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=auth_header
    ).json()
    
    return {"subject": subject, "headers": auth_header}


def test_create_supervisor_creator_success(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaNueva"
        },
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["homeId"] is not None
    assert response.json()["username"] == "creator2"


def test_create_subject_success(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["homeId"] is not None
    assert response.json()["userType"] == "SUBJECT"


def test_create_supervisor_for_subject_success(client, create_supervisor_creator_with_subject):
    data = create_supervisor_creator_with_subject
    
    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "subject1"
        },
        headers=data["headers"]
    )

    assert response.status_code == 200
    assert response.json()["homeId"] == data["subject"]["homeId"]


def test_create_user_empty_username(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "   ",
            "name": "Test",
            "surnames": "User",
            "email": "test@example.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "Username cannot be empty" in response.json()["detail"]


def test_create_user_empty_email(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "testuser",
            "name": "Test",
            "surnames": "User",
            "email": "   ",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=auth_header
    )

    assert response.status_code == 422
    errors = response.json()["detail"]
    assert any("email" in error["loc"] for error in errors)


def test_create_user_short_password(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "testuser",
            "name": "Test",
            "surnames": "User",
            "email": "test@example.com",
            "password": "123",
            "userType": "SUBJECT"
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "at least 6 characters" in response.json()["detail"]


def test_create_user_duplicate_username(client, create_supervisor_creator_with_subject):
    data = create_supervisor_creator_with_subject
    
    response = client.post(
        "/users/",
        json={
            "username": "subject1",  # Ya existe
            "name": "Another",
            "surnames": "User",
            "email": "another@example.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Username already taken" in response.json()["detail"]


def test_create_user_duplicate_email(client, create_supervisor_creator_with_subject):
    data = create_supervisor_creator_with_subject
    
    response = client.post(
        "/users/",
        json={
            "username": "anotheruser",
            "name": "Another",
            "surnames": "User",
            "email": "subject1@gmail.com",  # Ya existe
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Email already registered" in response.json()["detail"]

def test_create_second_subject_same_home_fails(client, create_supervisor_creator_with_subject):
    data = create_supervisor_creator_with_subject
    
    response = client.post(
        "/users/",
        json={
            "username": "subject2",
            "name": "Sub",
            "surnames": "Two",
            "email": "subject2@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Home with subject exists" in response.json()["detail"]


def test_create_supervisor_without_subject_username_fails(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "subject_username is required" in response.json()["detail"]


def test_create_supervisor_creator_without_home_name_fails(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR"
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "home_name is required" in response.json()["detail"]


def test_create_supervisor_for_nonexistent_subject_fails(client, auth_header):
    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "nonexistent"
        },
        headers=auth_header
    )

    assert response.status_code == 404
    assert "Subject not found" in response.json()["detail"]


def test_create_supervisor_for_non_subject_user_fails(client, auth_header):
    client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=auth_header
    )
    
    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "creator2"  # No es SUBJECT
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "is not a SUBJECT" in response.json()["detail"]


# ==================== TESTS DE GET ====================

def test_get_user_by_id_success(client, create_supervisor_creator_with_subject):
    """Obtener usuario por ID"""
    data = create_supervisor_creator_with_subject
    user_id = data["subject"]["id"]
    
    response = client.get(
        f"/users/{user_id}",
        headers=data["headers"]
    )

    assert response.status_code == 200
    assert response.json()["id"] == user_id
    assert response.json()["username"] == "subject1"


def test_get_user_not_found(client, auth_header):
    """Obtener usuario que no existe debe dar 404"""
    response = client.get(
        "/users/99999",
        headers=auth_header
    )

    assert response.status_code == 404
    assert "User not found" in response.json()["detail"]


def test_get_all_users_success(client, create_supervisor_creator_with_subject):
    """Listar todos los usuarios"""
    data = create_supervisor_creator_with_subject
    
    response = client.get(
        "/users/",
        headers=data["headers"]
    )

    assert response.status_code == 200
    users = response.json()
    assert len(users) >= 2  # Al menos admin y subject1
    assert isinstance(users, list)


# ==================== TESTS DE UPDATE ====================

def test_update_own_profile_success(client, create_supervisor_creator_with_subject):
    """Usuario puede editar su propio perfil"""
    data = create_supervisor_creator_with_subject
    user_id = data["subject"]["id"]
    
    response = client.put(
        f"/users/{user_id}",
        json={"name": "Updated Name"},
        headers=data["headers"]
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Updated Name"


def test_update_user_change_usertype_forbidden(client, create_supervisor_creator_with_subject):
    """No se puede cambiar user_type"""
    data = create_supervisor_creator_with_subject
    user_id = data["subject"]["id"]
    
    response = client.put(
        f"/users/{user_id}",
        json={"userType": "SUPERVISOR"},
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Cannot change user type" in response.json()["detail"]


def test_update_user_duplicate_username_fails(client, create_supervisor_creator_with_subject):
    """Update con username existente debe fallar"""
    data = create_supervisor_creator_with_subject
    
    # Crear otro usuario
    user2 = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=data["headers"]
    ).json()
    
    # Intentar actualizar subject1 con username de creator2
    response = client.put(
        f"/users/{data['subject']['id']}",
        json={"username": "creator2"},  # Ya existe
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Username already taken" in response.json()["detail"]


def test_update_user_duplicate_email_fails(client, create_supervisor_creator_with_subject):
    """Update con email existente debe fallar"""
    data = create_supervisor_creator_with_subject
    
    # Crear otro usuario
    client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=data["headers"]
    )
    
    # Intentar actualizar subject1 con email de creator2
    response = client.put(
        f"/users/{data['subject']['id']}",
        json={"email": "creator2@gmail.com"},  # Ya existe
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Email already registered" in response.json()["detail"]


def test_update_user_forbidden_outside_home(client, create_supervisor_creator_with_subject):
    """No se puede editar usuarios de otro Home"""
    data = create_supervisor_creator_with_subject
    
    # Crear otro SUPERVISOR_CREATOR con otro Home
    creator2 = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=data["headers"]
    ).json()

    # Login como creator2
    login2 = client.post(
        "/auth/login",
        json={"email": "creator2@gmail.com", "password": "123456"}
    ).json()
    headers2 = {"Authorization": f"Bearer {login2['access_token']}"}

    # Intentar editar subject1 (de otro Home)
    response = client.put(
        f"/users/{data['subject']['id']}",
        json={"name": "Hacked"},
        headers=headers2
    )

    assert response.status_code == 403
    assert "your own profile or users in your Home" in response.json()["detail"]


def test_update_password_success(client, create_supervisor_creator_with_subject):
    """Actualizar contraseña correctamente"""
    data = create_supervisor_creator_with_subject
    user_id = data["subject"]["id"]
    
    response = client.put(
        f"/users/{user_id}",
        json={"password": "newpassword123"},
        headers=data["headers"]
    )

    assert response.status_code == 200
    
    # Verificar que puede hacer login con nueva contraseña
    login = client.post(
        "/auth/login",
        json={"email": "subject1@gmail.com", "password": "newpassword123"}
    )
    assert login.status_code == 200


def test_update_password_too_short_fails(client, create_supervisor_creator_with_subject):
    """Password corto en update debe fallar"""
    data = create_supervisor_creator_with_subject
    user_id = data["subject"]["id"]
    
    response = client.put(
        f"/users/{user_id}",
        json={"password": "123"},
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "at least 6 characters" in response.json()["detail"]


# ==================== TESTS DE DELETE ====================

def test_delete_supervisor_success(client, create_supervisor_creator_with_subject):
    """SUPERVISOR_CREATOR puede eliminar SUPERVISOR"""
    data = create_supervisor_creator_with_subject
    
    # Crear SUPERVISOR
    supervisor = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "subject1"
        },
        headers=data["headers"]
    ).json()

    # Eliminar SUPERVISOR
    response = client.delete(
        f"/users/{supervisor['id']}",
        headers=data["headers"]
    )

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]


def test_delete_subject_forbidden(client, create_supervisor_creator_with_subject):
    """No se puede eliminar SUBJECT"""
    data = create_supervisor_creator_with_subject
    
    response = client.delete(
        f"/users/{data['subject']['id']}",
        headers=data["headers"]
    )

    assert response.status_code == 400
    assert "Only supervisors can be deleted" in response.json()["detail"]


def test_delete_user_different_home_forbidden(client, create_supervisor_creator_with_subject):
    """No se puede eliminar usuarios de otro Home"""
    data = create_supervisor_creator_with_subject
    
    # Crear otro Home con SUPERVISOR_CREATOR
    creator2 = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=data["headers"]
    ).json()

    # Login como creator2
    login2 = client.post(
        "/auth/login",
        json={"email": "creator2@gmail.com", "password": "123456"}
    ).json()
    headers2 = {"Authorization": f"Bearer {login2['access_token']}"}

    # Intentar eliminar subject1 (de otro Home)
    response = client.delete(
        f"/users/{data['subject']['id']}",
        headers=headers2
    )

    assert response.status_code == 403


def test_delete_user_not_found(client, auth_header):
    """Eliminar usuario que no existe debe dar 404"""
    response = client.delete(
        "/users/99999",
        headers=auth_header
    )

    assert response.status_code == 404
    assert "User not found" in response.json()["detail"]


# ==================== TESTS SIN AUTENTICACIÓN ====================

def test_create_user_without_auth_fails(client):
    """Crear usuario sin autenticación debe fallar"""
    response = client.post(
        "/users/",
        json={
            "username": "test",
            "name": "Test",
            "surnames": "User",
            "email": "test@example.com",
            "password": "123456",
            "userType": "SUBJECT"
        }
    )

    assert response.status_code == 401


def test_get_user_without_auth_fails(client):
    """GET sin autenticación debe fallar"""
    response = client.get("/users/1")
    assert response.status_code == 401


def test_update_user_without_auth_fails(client):
    """UPDATE sin autenticación debe fallar"""
    response = client.put(
        "/users/1",
        json={"name": "Test"}
    )
    assert response.status_code == 401


def test_delete_user_without_auth_fails(client):
    """DELETE sin autenticación debe fallar"""
    response = client.delete("/users/1")
    assert response.status_code == 401