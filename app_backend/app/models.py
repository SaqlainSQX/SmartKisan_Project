# G:\SmartKisan_Project\app_backend\app\models.py
from sqlalchemy import Column, Integer, String, TIMESTAMP, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql.expression import text
from .database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    
    # --- THIS WAS MISSING ---
    # This matches your database screenshot
    created_at = Column(
        TIMESTAMP(timezone=True), 
        nullable=False, 
        server_default=text('CURRENT_TIMESTAMP')
    )
    
    # --- THIS RELATIONSHIP IS REQUIRED FOR THE CHAT ---
    messages = relationship("ChatMessage", back_populates="owner")

# --- THIS IS THE NEW TABLE FOR YOUR CHAT FEATURE ---
class ChatMessage(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    role = Column(String(10), nullable=False) # "user" or "model"
    content = Column(String, nullable=False)
    timestamp = Column(
        TIMESTAMP(timezone=True), 
        nullable=False, 
        server_default=text('CURRENT_TIMESTAMP')
    )
    
    owner = relationship("User", back_populates="messages")