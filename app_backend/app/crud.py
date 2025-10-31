from sqlalchemy.orm import Session
from . import models, schemas, auth

def get_user_by_username(db: Session, username: str):
    """Fetches a user by their username."""
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate):
    """Creates a new user in the database."""
    
    # --- THIS IS THE FIX ---
    # Encode the password to UTF-8 bytes, THEN truncate at 72 bytes.
    # This prevents the "password cannot be longer than 72 bytes" error.
    safe_password_bytes = user.password.encode('utf-8')[:72]
    
    # Use the 'safe_password_bytes' variable here
    # passlib's hash function can accept bytes directly.
    hashed_password = auth.get_password_hash(safe_password_bytes)
    db_user = models.User(username=user.username, hashed_password=hashed_password)

    # Add, commit, and refresh the user
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user
