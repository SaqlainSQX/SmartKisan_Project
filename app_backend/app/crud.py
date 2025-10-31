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