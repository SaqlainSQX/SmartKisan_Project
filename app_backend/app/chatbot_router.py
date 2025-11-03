# G:\SmartKisan_Project\app_backend\app\chatbot_router.py
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from . import crud, schemas, auth, gemini_service
from .database import get_db
from .models import User

router = APIRouter(
    prefix="/chat",
    tags=["Chatbot"],
    dependencies=[Depends(auth.get_current_user)] # <-- SECURES ALL ROUTES IN THIS FILE
)

@router.get("/", response_model=List[schemas.ChatMessageResponse])
def get_user_chat_history(
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    """
    Fetches the logged-in user's chat history.
    """
    # We reverse the list so it's in chronological order (oldest first)
    history = crud.get_chat_history(db, user_id=current_user.id)
    return list(reversed(history))

@router.post("/", response_model=schemas.ChatMessageResponse)
def post_chat_message(
    request: schemas.ChatRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    """
    Receives a new chat message, saves it, gets a response
    from Gemini, saves that, and returns the response.
    """
    # 1. Save the user's message
    user_msg = schemas.ChatMessageCreate(role="user", content=request.message)
    crud.create_chat_message(db, user_id=current_user.id, message=user_msg)
    
    # 2. Get response from Gemini
    # (Here you would pass the full history to gemini_service if you want context)
    model_response = gemini_service.get_chatbot_response(request.message)
    
    # 3. Save the model's response
    model_msg = schemas.ChatMessageCreate(
        role=model_response["role"], 
        content=model_response["content"]
    )
    db_model_msg = crud.create_chat_message(db, user_id=current_user.id, message=model_msg)
    
    # 4. Return only the model's response to the app
    return db_model_msg