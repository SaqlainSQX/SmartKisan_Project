# G:\SmartKisan_Project\app_backend\app\auth.py
from passlib.context import CryptContext
from datetime import datetime, timedelta, timezone
from jose import JWTError, jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from . import schemas, models, crud
from .database import get_db
from sqlalchemy.orm import Session

SECRET_KEY = "your-very-secret-key-keep-it-safe"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def verify_password(plain_password, hashed_password):
    """Verifies a plain password against a hashed one."""
    safe_password_bytes = plain_password.encode('utf-8')[:72]
    return pwd_context.verify(safe_password_bytes, hashed_password)

def get_password_hash(password_bytes: bytes):
    """Hashes password bytes."""
    # This function expects bytes, which crud.py provides
    return pwd_context.hash(password_bytes)

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

# --- UPDATED to use email ---
def get_user(db: Session, email: str):
    """Helper function to get a user from the DB."""
    return crud.get_user_by_email(db, email=email)

# --- UPDATED to use email ---
def get_current_user(
    token: str = Depends(oauth2_scheme), 
    db: Session = Depends(get_db)
):
    """
    Decodes the JWT token, extracts the email, and fetches the 
    user from the database.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub") # The "subject" is now the email
        if email is None:
            raise credentials_exception
        token_data = schemas.TokenData(email=email)
    except JWTError:
        raise credentials_exception
    
    user = get_user(db, email=token_data.email) # Use email to find user
    if user is None:
        raise credentials_exception
    return user