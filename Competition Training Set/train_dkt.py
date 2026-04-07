import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
import pandas as pd
import numpy as np
import time
from sklearn.metrics import accuracy_score, roc_auc_score

# Import the dataset preparation we wrote earlier
# Ensure your previous file is named dkt_preprocessing.py
from dkt_preprocessing import load_and_clean_data, generate_sequences, DKTDataset


# ==========================================
# 1. THE NEURAL NETWORK ARCHITECTURE
# ==========================================
class DKTModel(nn.Module):
    def __init__(self, num_skills, embed_dim=64, hidden_dim=128):
        super(DKTModel, self).__init__()
        self.num_skills = num_skills

        # We create a unique embedding for every combination of (Skill + Correct/Incorrect)
        # We add +1 for the padding token (-1)
        self.interaction_embed = nn.Embedding(2 * num_skills + 1, embed_dim, padding_idx=2 * num_skills)

        # The core LSTM that remembers the student's history over time
        self.lstm = nn.LSTM(embed_dim, hidden_dim, batch_first=True)

        # The output layer predicts the probability of getting EVERY skill correct next
        self.out = nn.Linear(hidden_dim, num_skills)
        self.sigmoid = nn.Sigmoid()

    def forward(self, skills, corrects):
        # Create a unique interaction ID: e.g., Skill 5 Correct (5 + 104) vs Skill 5 Incorrect (5 + 0)
        interaction = skills + self.num_skills * corrects

        # Handle the padding tokens (-1) so they don't break the embedding layer
        interaction = torch.where(skills == -1, torch.tensor(2 * self.num_skills).to(skills.device), interaction)

        x = self.interaction_embed(interaction.long())
        lstm_out, _ = self.lstm(x)
        logits = self.out(lstm_out)
        preds = self.sigmoid(logits)
        return preds


# ==========================================
# 2. THE TRAINING LOOP
# ==========================================
def train_model():
    print("--- Starting Deep Learning Pipeline ---")

    # 1. Load Data
    df = load_and_clean_data("student_log_*.csv")  # Uses low_memory=False if you updated it!
    seqs, num_skills, encoder = generate_sequences(df, max_seq_length=50)

    dataset = DKTDataset(seqs)
    dataloader = DataLoader(dataset, batch_size=32, shuffle=True)

    # 2. Initialize Model
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Training on: {device}")

    model = DKTModel(num_skills=num_skills, embed_dim=128, hidden_dim=256).to(device)
    criterion = nn.BCELoss()  # Binary Cross Entropy (Perfect for Correct/Incorrect)
    optimizer = optim.Adam(model.parameters(), lr=0.001)

    epochs = 25

    print("\n--- Training Model ---")
    for epoch in range(epochs):
        model.train()
        total_loss = 0.0
        start_time = time.time()

        # Arrays to store predictions for metrics
        all_preds = []
        all_targets = []

        for batch_skills, batch_corrects in dataloader:
            batch_skills = batch_skills.to(device)
            batch_corrects = batch_corrects.to(device)

            optimizer.zero_grad()

            # Forward pass: Predict all future steps
            preds = model(batch_skills, batch_corrects)

            # We want to predict step t+1 using data up to step t.
            # So we shift our predictions and targets by 1 time step.
            preds_shifted = preds[:, :-1, :]  # Predictions based on step 0 to T-1
            target_skills = batch_skills[:, 1:]  # The actual skill attempted at step 1 to T
            target_corrects = batch_corrects[:, 1:]  # The actual outcome at step 1 to T

            # Mask out the padding (-1) so we don't penalize the AI for empty data
            mask = (target_skills != -1)

            # Gather the specific prediction for the skill the student actually attempted
            # (Because the model outputs predictions for ALL 104 skills at once)
            target_skills_safe = torch.where(mask, target_skills, torch.tensor(0).to(device))
            preds_for_target = torch.gather(preds_shifted, 2, target_skills_safe.unsqueeze(2).long()).squeeze(2)

            valid_preds = preds_for_target[mask]
            valid_targets = target_corrects[mask].float()

            # Calculate loss only on valid (non-padded) interactions
            loss = criterion(valid_preds, valid_targets)

            loss.backward()
            optimizer.step()
            total_loss += loss.item()

            # Store predictions for accuracy and AUC
            all_preds.extend(valid_preds.detach().cpu().numpy())
            all_targets.extend(valid_targets.detach().cpu().numpy())

        epoch_time = time.time() - start_time
        avg_loss = total_loss / len(dataloader)

        # Calculate Metrics
        binary_preds = [1 if p >= 0.5 else 0 for p in all_preds]
        acc = accuracy_score(all_targets, binary_preds)

        try:
            auc = roc_auc_score(all_targets, all_preds)
        except ValueError:
            auc = 0.0

        print(
            f"Epoch {epoch + 1}/{epochs} | Loss: {avg_loss:.4f} | Acc: {acc:.4f} | AUC: {auc:.4f} | Time: {epoch_time:.1f}s")

    # 3. Save the trained weights!
    print("\nTraining Complete! Saving Model weights...")
    torch.save(model.state_dict(), 'dkt_model_weights.pth')
    print("Saved as 'dkt_model_weights.pth'. Ready for Flask API integration!")


if __name__ == "__main__":
    train_model()