def test_register_supervisor(client):
    response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "ariana@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )
    assert response.status_code == 200
    data = response.json()
    assert data["email"] == "ariana@gmail.com"
    assert data["userType"] == "SUPERVISOR"


def test_register_supervisor_duplicate_email(client):
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "dup@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )

    response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor2",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "dup@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )
    assert response.status_code == 400


def test_login_success(client):
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "login@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )

    response = client.post(
        "/auth/login",
        json={"email": "login@gmail.com", "password": "123456"}
    )

    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert "refresh_token" in data


def test_login_wrong_password(client):
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "wrongpass@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )

    response = client.post(
        "/auth/login",
        json={"email": "wrongpass@gmail.com", "password": "badpass"}
    )

    assert response.status_code == 400


def test_login_user_not_found(client):
    response = client.post(
        "/auth/login",
        json={"email": "noexist@gmail.com", "password": "123456"}
    )
    assert response.status_code == 400


def test_refresh_success(client):
    # Crear usuario
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "refresh@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )

    login = client.post(
        "/auth/login",
        json={"email": "refresh@gmail.com", "password": "123456"}
    ).json()

    refresh_token = login["refresh_token"]

    response = client.post(
        "/auth/refresh",
        json={"refresh_token": refresh_token}
    )

    assert response.status_code == 200
    assert "access_token" in response.json()


def test_refresh_invalid(client):
    response = client.post(
        "/auth/refresh",
        json={"refresh_token": "INVALIDTOKEN"}
    )
    assert response.status_code == 401 or response.status_code == 400


def test_me_authenticated(client):
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "supervisor1",
            "name": "Ariana",
            "surnames": "Grande",
            "email": "me@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        }
    )

    login = client.post(
        "/auth/login",
        json={"email": "me@gmail.com", "password": "123456"}
    ).json()

    token = login["access_token"]

    response = client.get(
        "/auth/me",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 200
    assert response.json()["email"] == "me@gmail.com"


def test_me_unauthenticated(client):
    response = client.get("/auth/me")
    assert response.status_code == 401