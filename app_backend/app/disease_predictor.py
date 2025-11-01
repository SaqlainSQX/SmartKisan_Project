# G:\SmartKisan_Project\app_backend\app\disease_predictor.py
import tensorflow as tf
import numpy as np
from PIL import Image
import json
import os

class DiseasePredictor:
    def __init__(self, model_path, labels_path):
        print(f"Loading model from {model_path}...")
        self.model = tf.keras.models.load_model(model_path)
        print("Model loaded successfully.")

        print(f"Loading labels from {labels_path}...")
        with open(labels_path, 'r') as f:
            self.labels = json.load(f)
        print("Labels loaded successfully.")

        self.img_size = 224

    def preprocess_image(self, image: Image.Image):
        # Ensure image is RGB
        image = image.convert('RGB')
        image = image.resize((self.img_size, self.img_size))
        image_array = tf.keras.preprocessing.image.img_to_array(image)
        image_array = np.expand_dims(image_array, axis=0)
        processed_image = tf.keras.applications.mobilenet_v2.preprocess_input(image_array)
        return processed_image

    def predict_image(self, image: Image.Image):
        processed_image = self.preprocess_image(image)

        predictions = self.model.predict(processed_image)

        score = np.max(predictions)
        class_index = str(np.argmax(predictions)) # Index is a string in our JSON

        disease_name = self.labels.get(class_index, "Unknown Disease")
        confidence = float(score)

        return disease_name, confidence

# --- Create a single, global instance ---
# Get the directory of the current file
base_dir = os.path.dirname(os.path.abspath(__file__))

MODEL_PATH = os.path.join(base_dir, 'plant_disease_model.h5')
LABELS_PATH = os.path.join(base_dir, 'labels.json')

# Check if files exist
if not os.path.exists(MODEL_PATH):
    print(f"ERROR: Model file not found at {MODEL_PATH}")
    predictor = None
elif not os.path.exists(LABELS_PATH):
    print(f"ERROR: Labels file not found at {LABELS_PATH}")
    predictor = None
else:
    # This instance will be loaded once on server startup
    predictor = DiseasePredictor(model_path=MODEL_PATH, labels_path=LABELS_PATH)