from pydantic import BaseModel
from datetime import datetime

# Schema for user creation
class UserCreate(BaseModel):
    username: str
    password: str

# Schema for user login
class UserLogin(BaseModel):
    username: str
    password: str

# Schema for token response
class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    username: str | None = None


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
        from_attributes = True # Replaces orm_mode = True

class ChatRequest(BaseModel):
    message: str