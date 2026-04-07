from flask import Flask, request, jsonify
import joblib
import pandas as pd

app = Flask(__name__)

# Load the model into memory when the server starts
# Make sure 'skill_gap_model.joblib' is in the same directory
try:
    model = joblib.load('skill_gap_model.joblib')
    print("Model loaded successfully.")
except Exception as e:
    print(f"Error loading model: {e}")
    model = None

@app.route('/predict', methods=['POST'])
def predict_mastery():
    if not model:
        return jsonify({"error": "Model not loaded on server."}), 500

    try:
        # Get the JSON data sent from your Spring Boot backend
        data = request.get_json()

        # Extract the features expected by the model
        # We wrap them in lists to create a 1-row 2D array, which scikit-learn requires
        features = pd.DataFrame([{
            'skill_attempts': data.get('skill_attempts', 0),
            'skill_accuracy': data.get('skill_accuracy', 0.0),
            'total_attempts': data.get('total_attempts', 0),
            'overall_accuracy': data.get('overall_accuracy', 0.0),
            'recent_accuracy': data.get('recent_accuracy', 0.0)
        }])

        # Predict the probability of getting the NEXT question correct (Class 1)
        probability = model.predict_proba(features)[0][1]

        # Calculate a simple confidence score
        confidence = abs(probability - 0.5) * 2

        # Return the payload as JSON
        return jsonify({
            "status": "success",
            "predicted_probability": float(probability),
            "mastery_score_percentage": round(probability * 100, 1),
            "model_confidence": float(confidence)
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 400

if __name__ == '__main__':
    # Run the Flask app on port 5000
    app.run(host='0.0.0.0', port=5000, debug=True)