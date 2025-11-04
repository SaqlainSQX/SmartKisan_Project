# G:\SmartKisan_Project\app_backend\app\models.py
from sqlalchemy import Column, Integer, String, TIMESTAMP, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql.expression import text
from .database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(100), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    
    created_at = Column(
        TIMESTAMP(timezone=True), 
        nullable=False, 
        server_default=text('CURRENT_TIMESTAMP')
    )
    
    otp = Column(String(6), nullable=True)
    otp_expires_at = Column(DateTime(timezone=True), nullable=True)

    # --- Link to Chat Messages ---
    messages = relationship("ChatMessage", back_populates="owner")
    
    # --- THIS IS THE FIX ---
    # This line was missing from the file your server is running.
    # It links the User to their UserProfile.
    profile = relationship("UserProfile", back_populates="user", uselist=False, cascade="all, delete-orphan")
    # --- END FIX ---

# --- NEW CLASS for UserProfile ---
class UserProfile(Base):
    __tablename__ = "user_profiles"

    user_id = Column(Integer, ForeignKey("users.id"), primary_key=True)
    name = Column(String(100), nullable=True)
    contact_number = Column(String(20), nullable=True)
    profile_photo_url = Column(String(255), nullable=True)
    
    # This line 'back_populates' to the 'profile' property we just added
    user = relationship("User", back_populates="profile")

class ChatMessage(Base):
    __tablename__ = "chat_messages"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    role = Column(String(10), nullable=False)
    content = Column(String, nullable=False)
    timestamp = Column(
        TIMESTAMP(timezone=True), 
        nullable=False, 
        server_default=text('CURRENT_TIMESTAMP')
    )
    owner = relationship("User", back_populates="messages")