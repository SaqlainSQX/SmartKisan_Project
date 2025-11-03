# G:\SmartKisan_Project\app_backend\app\schemas.py
from pydantic import BaseModel, EmailStr, constr
from datetime import datetime

# --- Password Validation ---
# constr (constrained string)
PasswordStr = constr(min_length=8)

# --- Schemas for User Auth ---

class UserCreate(BaseModel):
    email: EmailStr
    password: PasswordStr # Enforces 8-character minimum

class UserLogin(BaseModel):
    email: EmailStr
    password: str # No min_length check on login

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: EmailStr | None = None # Subject is now email

# --- Schemas for Password Reset ---

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    email: EmailStr
    otp: constr(min_length=6, max_length=6)
    new_password: PasswordStr # Enforces 8-character minimum

class MessageResponse(BaseModel):
    message: str

# --- Schemas for Chat ---

class ChatMessageBase(BaseModel):
    role: str
    content: str

class ChatMessageCreate(ChatMessageBase):
    pass

class ChatMessageResponse(ChatMessageBase):
    id: int
    user_id: int
    timestamp: datetime

    class Config:
        from_attributes = True

class ChatRequest(BaseModel):
    message: str