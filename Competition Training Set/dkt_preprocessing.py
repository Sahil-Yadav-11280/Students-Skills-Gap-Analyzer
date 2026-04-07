import pandas as pd
import numpy as np
import glob
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.preprocessing import LabelEncoder


# ==========================================
# STEP 1: LOAD AND MERGE THE 10 CSV FILES
# ==========================================
def load_and_clean_data(file_pattern="student_log_*.csv"):
    print("Loading all student log files...")
    all_files = glob.glob(file_pattern)

    if not all_files:
        raise ValueError("No CSV files found! Make sure they are in the same folder.")

    # Read and concatenate all 10 CSV files into one massive DataFrame
    print(f"Found these files: {all_files}")
    df_list = [pd.read_csv(f, low_memory=False) for f in all_files]
    df = pd.concat(df_list, ignore_index=True)

    # We only need the core DKT columns for the sequence model
    # ITEST_id = Student ID
    df = df[['ITEST_id', 'startTime', 'skill', 'correct', 'hintCount', 'timeTaken']]

    # Drop rows where the skill is missing (we can't train on blank questions)
    df = df.dropna(subset=['skill', 'correct'])

    # Sort chronologically for EVERY student
    df = df.sort_values(by=['ITEST_id', 'startTime'])

    print(f"Total interaction rows loaded: {len(df)}")
    return df


# ==========================================
# STEP 2: BUILD THE SEQUENCES
# ==========================================
def generate_sequences(df, max_seq_length=50):
    print("Encoding skills and grouping by student sequences...")

    # Convert text skills (e.g., "algebraic-manipulation") into integer IDs (e.g., 1, 2, 3)
    skill_encoder = LabelEncoder()
    df['skill_id'] = skill_encoder.fit_transform(df['skill'])
    num_skills = len(skill_encoder.classes_)

    # Group the massive dataframe by Student ID
    grouped = df.groupby('ITEST_id')

    sequences = []

    for student_id, group in grouped:
        # Extract the student's chronological journey into lists
        skill_history = group['skill_id'].values
        correct_history = group['correct'].values

        # If a student has 120 attempts, we split it into multiple sequences of 50
        for i in range(0, len(skill_history), max_seq_length):
            seq_skills = skill_history[i: i + max_seq_length]
            seq_correct = correct_history[i: i + max_seq_length]

            # If the sequence is shorter than 50 (e.g., their last few attempts), we pad it with -1
            if len(seq_skills) < max_seq_length:
                pad_len = max_seq_length - len(seq_skills)
                seq_skills = np.pad(seq_skills, (0, pad_len), constant_values=-1)
                seq_correct = np.pad(seq_correct, (0, pad_len), constant_values=-1)

            sequences.append((seq_skills, seq_correct))

    print(f"Generated {len(sequences)} total training sequences.")
    return sequences, num_skills, skill_encoder


# ==========================================
# STEP 3: CREATE THE PYTORCH DATASET
# ==========================================
class DKTDataset(Dataset):
    def __init__(self, sequences):
        self.sequences = sequences

    def __len__(self):
        return len(self.sequences)

    def __getitem__(self, idx):
        # Return (Inputs, Targets)
        # Inputs: All steps except the last one
        # Targets: All steps except the first one (we are predicting the NEXT step)
        seq_skills, seq_correct = self.sequences[idx]

        # Convert to PyTorch Tensors
        skills_tensor = torch.tensor(seq_skills, dtype=torch.long)
        correct_tensor = torch.tensor(seq_correct, dtype=torch.float32)

        return skills_tensor, correct_tensor


# --- Execution Block ---
if __name__ == "__main__":
    # 1. Run the extraction
    df = load_and_clean_data("student_log_*.csv")

    # 2. Generate Sequences (Max 50 interactions per sequence)
    seqs, num_skills, encoder = generate_sequences(df, max_seq_length=50)

    # 3. Load into PyTorch
    dataset = DKTDataset(seqs)
    dataloader = DataLoader(dataset, batch_size=32, shuffle=True)

    print(f"Number of unique skills detected: {num_skills}")
    print("Data is perfectly formatted and ready for the LSTM!")

    # You can save the encoder so the Flask API knows how to map names back later
    import joblib

    joblib.dump(encoder, 'skill_encoder.joblib')