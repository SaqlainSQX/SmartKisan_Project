# G:\SmartKisan_Project\app_backend\app\main.py

from fastapi import FastAPI, Depends, HTTPException, status, Form
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from datetime import timedelta, datetime, timezone
import random
import string
from fastapi.staticfiles import StaticFiles
import os
from pydantic import EmailStr # Import EmailStr for validation

# Import all your modules
from . import crud, models, schemas, auth, database, disease_router, chatbot_router , profile_router
from .email_service import send_otp_email # Import the new email service


# Create all database tables (on startup)
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI()

# --- MOUNT THE STATIC FOLDER ---
# This makes the "app/static" folder publicly accessible at the "/static" URL
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
static_path = os.path.join(BASE_DIR, "static")
app.mount("/static", StaticFiles(directory=static_path), name="static")
# --- END MOUNT ---

# --- Include all your routers ---
app.include_router(chatbot_router.router)
app.include_router(disease_router.router)
app.include_router(profile_router.router)

# --- Dependency ---
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- UPDATED /register Endpoint ---
@app.post("/register/", response_model=schemas.MessageResponse)
def register_user(
    email: EmailStr = Form(...), 
    password: str = Form(...), 
    db: Session = Depends(get_db)
):
    """
    Register a new user.
    Uses Form data (email, password).
    """
    # 1. Check password length
    if len(password) < 8:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Password must be at least 8 characters"
        )
    
    # 2. Check if user already exists (using email)
    db_user = crud.get_user_by_email(db, email=email)
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )
    
    # 3. Create the user object from the form data
    user_create = schemas.UserCreate(email=email, password=password)
    crud.create_user(db=db, user=user_create)
    return {"message": "User created successfully"}

# --- UPDATED /token Endpoint ---
@app.post("/token", response_model=schemas.Token)
def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    """
    Login user and return a JWT token.
    The 'username' field from form_data will contain the user's email.
    """
    # 1. Authenticate user using email
    user = auth.get_user(db, email=form_data.username) # "username" field holds the email
    if not user or not auth.verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # 2. Create access token with email as the subject
    access_token_expires = timedelta(minutes=auth.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = auth.create_access_token(
        data={"sub": user.email}, expires_delta=access_token_expires # Use user.email
    )
    return {"access_token": access_token, "token_type": "bearer"}

# --- NEW /forgot-password Endpoint ---
@app.post("/forgot-password/", response_model=schemas.MessageResponse)
async def forgot_password(
    request: schemas.ForgotPasswordRequest, 
    db: Session = Depends(get_db)
):
    user = crud.get_user_by_email(db, email=request.email)
    if not user:
        # Don't reveal if user exists, just return a safe message
        return {"message": "If an account with this email exists, an OTP has been sent."}
    
    # 1. Generate OTP
    otp = ''.join(random.choices(string.digits, k=6))
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=10) # 10-min expiry
    
    # 2. Save OTP to database
    crud.update_user_otp(db, user=user, otp=otp, expires_at=expires_at)
    
    # 3. Send OTP via email
    try:
        # --- THIS IS THE FIX ---
        # Changed 'email_to' to 'email' to match the function definition
        await send_otp_email(email=user.email, otp=otp)
        # --- END FIX ---
        return {"message": "If an account with this email exists, an OTP has been sent."}
    except Exception as e:
        print(f"Error sending email: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error sending email. Please try again later."
        )

# --- NEW /reset-password Endpoint ---
@app.post("/reset-password/", response_model=schemas.MessageResponse)
def reset_password(
    request: schemas.ResetPasswordRequest, 
    db: Session = Depends(get_db)
):
    # 1. Find user by email
    user = crud.get_user_by_email(db, email=request.email)
    if not user:
        raise HTTPException(status_code=400, detail="Invalid request. User not found.")

    # 2. Verify their OTP
    if not (
        user.otp == request.otp and
        user.otp_expires_at and
        user.otp_expires_at > datetime.now(timezone.utc)
    ):
        raise HTTPException(status_code=400, detail="Invalid or expired OTP")
    
    # 3. All checks passed, update the password
    crud.update_user_password(db, user=user, new_password=request.new_password)
    
    return {"message": "Password has been reset successfully."}

@app.get("/")
def read_root():
    return {"message": "Welcome to the API!"}