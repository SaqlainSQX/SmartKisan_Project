# G:\SmartKisan_Project\app_backend\app\crud.py
from sqlalchemy.orm import Session
from . import models, schemas, auth
from datetime import datetime

# ... (get_user_by_email, get_user_by_otp are unchanged) ...

def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def get_user_by_otp(db: Session, otp: str):
    return db.query(models.User).filter(
        models.User.otp == otp,
        models.User.otp_expires_at > datetime.now(datetime.timezone.utc)
    ).first()

def create_user(db: Session, user: schemas.UserCreate):
    """Creates a new user and their empty profile in the database."""
    
    safe_password_bytes = user.password.encode('utf-8')[:72]
    hashed_password = auth.get_password_hash(safe_password_bytes)
    
    db_user = models.User(email=user.email, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)

    # --- 2. Create the associated empty UserProfile ---
    db_profile = models.UserProfile(user_id=db_user.id)
    db.add(db_profile)
    db.commit()
    
    return db_user

# ... (update_user_otp, update_user_password are unchanged) ...

def update_user_otp(db: Session, user: models.User, otp: str, expires_at: datetime):
    user.otp = otp
    user.otp_expires_at = expires_at
    db.commit()
    db.refresh(user)
    return user

def update_user_password(db: Session, user: models.User, new_password: str):
    safe_password_bytes = new_password.encode('utf-8')[:72]
    hashed_password = auth.get_password_hash(safe_password_bytes)
    user.hashed_password = hashed_password
    user.otp = None 
    user.otp_expires_at = None
    db.commit()
    return user

# --- UPDATED Profile Functions ---

def update_user_profile(db: Session, user: models.User, profile_data: schemas.ProfileUpdate):
    """Updates a user's name and contact number in their profile."""
    # --- FIX: Safely get or create profile ---
    profile = user.profile
    if not profile:
        profile = models.UserProfile(user_id=user.id)
        db.add(profile)
    
    if profile_data.name is not None:
        profile.name = profile_data.name
    if profile_data.contact_number is not None:
        profile.contact_number = profile_data.contact_number
    
    db.commit()
    db.refresh(profile)
    return user 

def update_user_profile_photo(db: Session, user: models.User, photo_url: str):
    """Updates a user's profile photo URL."""
    # --- FIX: Safely get or create profile ---
    profile = user.profile
    if not profile:
        profile = models.UserProfile(user_id=user.id)
        db.add(profile)
        
    profile.profile_photo_url = photo_url
    
    db.commit()
    db.refresh(profile)
    return user

# ... (your get_chat_history and create_chat_message functions are unchanged) ...