# G:\SmartKisan_Project\app_backend\app\disease_router.py
from fastapi import APIRouter, File, UploadFile, HTTPException, status
from PIL import Image
import io
import time
import asyncio
from asyncio import TimeoutError  # <-- ADD THIS IMPORT

# Import our new modules
from . import disease_predictor
from . import gemini_service

router = APIRouter(
    prefix="/disease",
    tags=["Disease Prediction"]
)

@router.post("/predict")
async def predict_disease(file: UploadFile = File(...)):
    """
    Receives an image of a plant leaf, predicts the disease,
    and returns detailed information from the Gemini API.
    """
    
    start_time = time.time()
    print(f"\n[REQUEST]: Received image at {start_time}")
    
    # ... (steps 1 & 2 are unchanged) ...
    # 1. Check if the predictor is loaded
    if disease_predictor.predictor is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Model is not loaded. Check server logs."
        )
        
    # 2. Read and validate the image
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid image file."
        )

    # 3. Get model prediction
    try:
        print("[REQUEST]: Starting model prediction...")
        model_start_time = time.time()
        disease_name, confidence = disease_predictor.predictor.predict_image(image)
        model_end_time = time.time()
        print(f"[REQUEST]: Model prediction finished in {model_end_time - model_start_time:.2f} seconds.")
        print(f"[REQUEST]: Prediction: {disease_name} (Confidence: {confidence:.2f})")

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error during prediction: {e}"
        )

    # 4. Get solution from Gemini (with 10-second timeout)
    gemini_result = None  # Default to None
    try:
        print("[REQUEST]: Calling Gemini API (10s timeout)...")
        gemini_start_time = time.time()
        
        # --- THIS IS THE FIX ---
        # We create the task
        gemini_task = asyncio.to_thread(
            gemini_service.get_disease_solution, disease_name
        )
        # And we run it with a 10-second budget
        gemini_result = await asyncio.wait_for(gemini_task, timeout=10.0)
        # --- END FIX ---
        
        gemini_end_time = time.time()
        print(f"[REQUEST]: Gemini call finished in {gemini_end_time - gemini_start_time:.2f} seconds.")

    except TimeoutError:
        # --- THIS IS OUR FALLBACK ---
        print("[REQUEST]: Gemini call TIMED OUT after 10 seconds. Sending partial response.")
        # We set gemini_result to None, so we can handle it below
        gemini_result = None

    except Exception as e:
        print(f"[REQUEST]: Gemini call FAILED. Error: {e}")
        gemini_result = None # Also treat any other error as a partial response

    # 5. Return the full or partial response
    end_time = time.time()
    print(f"[REQUEST]: Total request time: {end_time - start_time:.2f} seconds.")
    
    if gemini_result:
        # --- FULL RESPONSE (Success) ---
        return {
            "disease_name": disease_name,
            "confidence": confidence,
            "description": gemini_result.get("description"),
            "solution": gemini_result.get("solution")
        }
    else:
        # --- PARTIAL RESPONSE (Timeout or Error) ---
        # Your app will now receive this immediately instead of timing out.
        return {
            "disease_name": disease_name,
            "confidence": confidence,
            "description": "AI assistant is busy. Could not fetch details.",
            "solution": "Please try again in a moment."
        }
    