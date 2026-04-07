from flask import Flask, request, jsonify
import torch

# Import your exact model blueprint from your training file
# (This assumes your training file is named train_dkt.py)
from train_dkt import DKTModel

app = Flask(__name__)

# ==========================================
# 1. LOAD THE "BRAIN"
# ==========================================
# IMPORTANT: Change this to the exact number of skills your dataset has!
NUM_SKILLS = 102

# Force it to use CPU for the API (easier for web servers)
device = torch.device('cpu')
print("Loading model...")

# Rebuild the empty architecture
model = DKTModel(num_skills=NUM_SKILLS, embed_dim=128, hidden_dim=256).to(device)

# Pour the trained weights (the .pth file) into the architecture
model.load_state_dict(torch.load('dkt_model_weights.pth', map_location=device))

# Turn off "training mode" so the model knows it is taking a real test now
model.eval()
print("Model loaded successfully and ready for predictions!")


# ==========================================
# 2. CREATE THE API ENDPOINT
# ==========================================
@app.route('/predict', methods=['POST'])
def predict():
    try:
        # 1. Get the JSON data sent from the frontend
        data = request.get_json()
        skills_list = data.get('skills')
        corrects_list = data.get('corrects')

        # 2. Convert to Tensors
        skills_tensor = torch.tensor(skills_list, dtype=torch.long).unsqueeze(0).to(device)
        corrects_tensor = torch.tensor(corrects_list, dtype=torch.long).unsqueeze(0).to(device)

        # 3. Ask the AI for a prediction
        with torch.no_grad():
            predictions = model(skills_tensor, corrects_tensor)

        latest_predictions = predictions[0, -1, :].tolist()

        # --- EXACT BACKEND FORMATTING ---
        detailed_skills = {}

        for i, prob in enumerate(latest_predictions):
            # 1. predicted_probability (Raw float)
            pred_prob = round(prob, 4)

            # 2. master_score_percentage (Formatted as a percentage string)
            mastery_pct = round(prob * 100, 2)

            # 3. status (Categorized based on how high the score is)
            if prob >= 0.80:
                skill_status = "Mastered"
            elif prob >= 0.50:
                skill_status = "Developing"
            else:
                skill_status = "Needs Intervention"

            # 4. confidence (Calculated by how far the AI is from a 50/50 guess)
            # A score of 0.99 (knows it) or 0.01 (doesn't know it) is High Confidence.
            # A score of 0.51 is Low Confidence.
            certainty = abs(prob - 0.5) * 2
            if certainty >= 0.60:
                ai_confidence = "High"
            elif certainty >= 0.20:
                ai_confidence = "Medium"
            else:
                ai_confidence = "Low"

            # Append the exact dictionary structure your backend requested
            detailed_skills[str(i)] = {
                "predicted_probability": pred_prob,
                "mastery_score_percentage": mastery_pct,
                "model_confidence": round(certainty*100 , 2),
            }

        # 4. Send the result back as JSON
        return jsonify({
            "api_status": "success",
            "skills_data": detailed_skills
        })

    except Exception as e:
        return jsonify({
            "api_status": "error",
            "message": str(e)
        }), 400


# ==========================================
# 3. RUN THE SERVER
# ==========================================
if __name__ == '__main__':
    # Runs on port 5000 by default
    app.run(host='0.0.0.0', port=5000, debug=True)