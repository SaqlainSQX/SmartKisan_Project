# G:\SmartKisan_Project\app_backend\app\profile_router.py

from fastapi import APIRouter, Depends, HTTPException, status, File, UploadFile
from sqlalchemy.orm import Session
from . import crud, schemas, auth, models # <-- Import models
from .database import get_db
from .models import User
import shutil
import os
import uuid

router = APIRouter(
    prefix="/profile",
    tags=["Profile"],
    dependencies=[Depends(auth.get_current_user)] 
)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROFILE_PIC_DIR = os.path.join(BASE_DIR, "static", "profile_pics")
os.makedirs(PROFILE_PIC_DIR, exist_ok=True) 

# --- NEW HELPER FUNCTION ---
def _get_or_create_profile(db: Session, user_id: int) -> models.UserProfile:
    """
    Safely gets a user's profile. If it doesn't exist (e.g., for an old user),
    it creates one and returns it.
    """
    profile = db.query(models.UserProfile).filter(models.UserProfile.user_id == user_id).first()
    
    if not profile:
        profile = models.UserProfile(user_id=user_id)
        db.add(profile)
        db.commit()
        db.refresh(profile)
    
    return profile
# --- END HELPER FUNCTION ---


@router.get("/", response_model=schemas.UserResponse)
def get_user_profile(
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    """
    Fetches the profile for the currently logged-in user.
    """
    # --- FIX: Safely get the profile ---
    profile = _get_or_create_profile(db, current_user.id)
    
    return schemas.UserResponse(
        id=current_user.id,
        email=current_user.email,
        created_at=current_user.created_at,
        name=profile.name,
        contact_number=profile.contact_number,
        profile_photo_url=profile.profile_photo_url
    )


@router.put("/", response_model=schemas.UserResponse)
def update_user_profile_data(
    profile_update: schemas.ProfileUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    """
    Updates the text-based profile data (name, contact) for the logged-in user.
    """
    # --- FIX: Safely get the profile before updating it ---
    # We pass 'current_user' (which has the .profile relationship)
    # but the crud function will now be smarter
    updated_user = crud.update_user_profile(db, user=current_user, profile_data=profile_update)
    
    return schemas.UserResponse(
        id=updated_user.id,
        email=updated_user.email,
        created_at=updated_user.created_at,
        name=updated_user.profile.name,
        contact_number=updated_user.profile.contact_number,
        profile_photo_url=updated_user.profile.profile_photo_url
    )

@router.post("/photo", response_model=schemas.UserResponse)
async def upload_profile_photo(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    """
    Uploads a new profile photo for the logged-in user.
    """
    file_extension = os.path.splitext(file.filename)[1].lower()
    if file_extension not in [".jpg", ".jpeg", ".png"]:
        raise HTTPException(status_code=400, detail="Invalid file type. Only jpg/png allowed.")
        
    unique_filename = f"{current_user.id}_{uuid.uuid4()}{file_extension}"
    file_path = os.path.join(PROFILE_PIC_DIR, unique_filename)
    
    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error saving file: {e}")
    finally:
        file.file.close()

    file_url = f"/static/profile_pics/{unique_filename}"
    
    # --- FIX: Safely update the photo ---
    updated_user = crud.update_user_profile_photo(db, user=current_user, photo_url=file_url)
    
    return schemas.UserResponse(
        id=updated_user.id,
        email=updated_user.email,
        created_at=updated_user.created_at,
        name=updated_user.profile.name,
        contact_number=updated_user.profile.contact_number,
        profile_photo_url=updated_user.profile.profile_photo_url
    )