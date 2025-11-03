# G:\SmartKisan_Project\app_backend\app\crud.py
from sqlalchemy.orm import Session
from . import models, schemas, auth
from datetime import datetime

# --- REPLACED get_user_by_username ---
def get_user_by_email(db: Session, email: str):
    """Fetches a user by their email."""
    return db.query(models.User).filter(models.User.email == email).first()

def get_user_by_otp(db: Session, otp: str):
    """Fetches a user by a valid (non-expired) OTP."""
    return db.query(models.User).filter(
        models.User.otp == otp,
        models.User.otp_expires_at > datetime.now(datetime.timezone.utc)
    ).first()

def create_user(db: Session, user: schemas.UserCreate):
    """Creates a new user in the database."""
    
    # Truncate the password to 72 bytes BEFORE hashing
    safe_password_bytes = user.password.encode('utf-8')[:72]
    hashed_password = auth.get_password_hash(safe_password_bytes)
    
    # --- UPDATED to use email ---
    db_user = models.User(email=user.email, hashed_password=hashed_password)

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def update_user_otp(db: Session, user: models.User, otp: str, expires_at: datetime):
    """Updates a user's OTP and expiration time."""
    user.otp = otp
    user.otp_expires_at = expires_at
    db.commit()
    db.refresh(user)
    return user

def update_user_password(db: Session, user: models.User, new_password: str):
    """Updates a user's password and clears their OTP."""
    
    safe_password_bytes = new_password.encode('utf-8')[:72]
    hashed_password = auth.get_password_hash(safe_password_bytes)
    
    user.hashed_password = hashed_password
    user.otp = None # Invalidate the OTP
    user.otp_expires_at = None
    
    db.commit()
    return user

# --- Chat CRUD Functions ---

def get_chat_history(db: Session, user_id: int, skip: int = 0, limit: int = 100):
    """Fetches chat history for a specific user, most recent first."""
    return db.query(models.ChatMessage)\
             .filter(models.ChatMessage.user_id == user_id)\
             .order_by(models.ChatMessage.timestamp.desc())\
             .offset(skip)\
             .limit(limit)\
             .all()

def create_chat_message(db: Session, user_id: int, message: schemas.ChatMessageCreate):
    """Saves a new chat message to the database."""
    db_message = models.ChatMessage(
        user_id=user_id,
        role=message.role,
        content=message.content
    )
    db.add(db_message)
    db.commit()
    db.refresh(db_message)
    return db_message