import warnings
from datetime import datetime, timedelta
from jose import jwt, JWTError
from passlib.context import CryptContext

warnings.filterwarnings("ignore", category=DeprecationWarning)

SECRET_KEY = "SUPER_SECRET_KEY_CHANGE_THIS"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 15
REFRESH_TOKEN_EXPIRE_DAYS_DEFAULT = 7
REFRESH_TOKEN_EXPIRE_DAYS_SUBJECT = 365

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Límite de bcrypt
MAX_PASSWORD_LENGTH = 72


def hash_password(password: str) -> str:
    # Truncar la contraseña si excede el límite de bcrypt
    if len(password.encode('utf-8')) > MAX_PASSWORD_LENGTH:
        password = password.encode('utf-8')[:MAX_PASSWORD_LENGTH].decode('utf-8', errors='ignore')
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    # Truncar la contraseña si excede el límite de bcrypt
    if len(plain.encode('utf-8')) > MAX_PASSWORD_LENGTH:
        plain = plain.encode('utf-8')[:MAX_PASSWORD_LENGTH].decode('utf-8', errors='ignore')
    return pwd_context.verify(plain, hashed)


def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def create_refresh_token(data: dict, days: int):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=days)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str):
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        return None