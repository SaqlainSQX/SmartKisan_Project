# G:\SmartKisan_Project\app_backend\app\auth.py

from passlib.context import CryptContext
from datetime import datetime, timedelta, timezone
from jose import JWTError, jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer

# --- ADDED IMPORTS ---
# We need 'crud' to find the user and 'get_db' for the session
from . import schemas, models, crud
from .database import get_db
# --- END ADDED IMPORTS ---

from sqlalchemy.orm import Session

# !!! USE A STRONG, RANDOMLY GENERATED KEY IN PRODUCTION !!!
# You can generate one using: openssl rand -hex 32
SECRET_KEY = "your-very-secret-key-keep-it-safe"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# Password hashing context
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# OAuth2 scheme
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def verify_password(plain_password, hashed_password):
    """Verifies a plain password against a hashed one."""
    # This is your existing, correct logic
    safe_password_bytes = plain_password.encode('utf-8')[:72]
    return pwd_context.verify(safe_password_bytes, hashed_password)

def get_password_hash(password):
    """Hashes a plain password."""
    # This is your existing, correct logic
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: timedelta | None = None):
    """Creates a new JWT access token."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(timezone.utc) + expires_delta
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def get_user(db: Session, username: str):
    """Helper function to get a user from the DB."""
    # --- UPDATED FOR CONSISTENCY ---
    # Use the crud function instead of a direct query
    return crud.get_user_by_username(db, username=username)

# --- THIS IS THE NEW FUNCTION YOU NEED ---
def get_current_user(
    token: str = Depends(oauth2_scheme), 
    db: Session = Depends(get_db)
):
    """
    Decodes the JWT token, extracts the username, and fetches the 
    user from the database. This is a secure dependency.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
        token_data = schemas.TokenData(username=username)
    except JWTError:
        raise credentials_exception
    
    user = get_user(db, username=token_data.username)
    if user is None:
        raise credentials_exception
    return user