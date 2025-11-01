# G:\SmartKisan_Project\app_backend\app\gemini_service.py
import google.generativeai as genai
import os
import json

# --- IMPORTANT ---
# Set your Gemini API key as an environment variable
# In your terminal: set GEMINI_API_KEY=YOUR_API_KEY
try:
    # Use the environment variable for security
    GEMINI_API_KEY = "" #os.environ["GEMINI_API_KEY"]
    genai.configure(api_key=GEMINI_API_KEY)
except KeyError:
    print("="*50)
    print("ERROR: GEMINI_API_KEY environment variable not set.")
    print("Please set it before running the server.")
    print("="*50)
    GEMINI_API_KEY = None

def get_disease_solution(disease_name: str) -> dict:
    if not GEMINI_API_KEY:
        return {
            "description": "Gemini API key not configured.",
            "solution": "Please contact the administrator."
        }

    if "___" not in disease_name:
        # Not a valid disease name (e.g., "Unknown Disease" or "healthy")
        return {
            "description": "No disease detected or unknown category.",
            "solution": "The plant appears healthy or the disease is not in our database."
        }

    try:
        # --- FIX: Define the JSON schema for the response ---
        json_schema = {
            "type": "OBJECT",
            "properties": {
                "description": {"type": "STRING"},
                "solution": {"type": "STRING"}
            },
            "required": ["description", "solution"]
        }

        # --- FIX: Configure the model to use JSON mode ---
        generation_config = genai.GenerationConfig(
            response_mime_type="application/json",
            response_schema=json_schema
        )
        
        model = genai.GenerativeModel(
            model_name='gemini-2.5-flash-preview-09-2025',
            generation_config=generation_config
        )

        prompt = f"""
        Act as an expert botanist and agricultural scientist.
        A farmer has identified a plant disease: {disease_name}.

        Provide a brief, easy-to-understand description of this disease (what it is, what it does to the plant).
        Then, provide a concise, step-by-step list of solutions. Include both organic and chemical treatment options if available.
        """

        response = model.generate_content(prompt)

        # --- FIX: Parse the JSON directly ---
        # In JSON mode, response.text is a valid JSON string
        data = json.loads(response.text)
        return data

    except Exception as e:
        print(f"Error calling Gemini API: {e}")
        return {
            "description": "Could not fetch detailed information.",
            "solution": f"An error occurred while contacting the AI assistant: {e}"
        }