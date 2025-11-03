from sqlalchemy.orm import Session
from . import models, schemas, auth

def get_user_by_username(db: Session, username: str):
    """Fetches a user by their username."""
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate):
    """Creates a new user in the database."""
    
    # --- The test line is now REMOVED ---
    
    # --- THIS IS THE REAL FIX ---
    # It will now run and fix the 72-byte error
    safe_password_bytes = user.password.encode('utf-8')[:72]
    
    hashed_password = auth.get_password_hash(safe_password_bytes)
    db_user = models.User(username=user.username, hashed_password=hashed_password)

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


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