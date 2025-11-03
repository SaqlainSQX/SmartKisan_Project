# G:\SmartKisan_Project\app_backend\app\models.py
from sqlalchemy import Column, Integer, String, TIMESTAMP, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql.expression import text
from .database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    
    # --- USERNAME IS GONE ---
    # email is the new unique identifier
    email = Column(String(100), unique=True, index=True, nullable=False)
    
    hashed_password = Column(String(255), nullable=False)
    
    created_at = Column(
        TIMESTAMP(timezone=True), 
        nullable=False, 
        server_default=text('CURRENT_TIMESTAMP')
    )
    
    # --- NEW FIELDS FOR OTP ---
    otp = Column(String(6), nullable=True)
    otp_expires_at = Column(DateTime(timezone=True), nullable=True)
    
    # Relationship to ChatMessage
    messages = relationship("ChatMessage", back_populates="owner")

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