import pandas as pd
from sklearn.ensemble import RandomForestClassifier # NEW ALGORITHM
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, roc_auc_score, classification_report
import joblib

def train_evaluate_and_export(csv_file_path, export_path='skill_gap_model.joblib'):
    print(f"Loading data from {csv_file_path}...")
    df = pd.read_csv(csv_file_path)

    if 'recent_accuracy' in df.columns and 'overall_accuracy' in df.columns:
        df['recent_accuracy'] = df['recent_accuracy'].fillna(df['overall_accuracy'])

    # --- UPGRADE 1: Add More Features ---
    # We are now including time and hints to give the model more context
    features = [
        'skill_attempts', 'skill_accuracy',
        'total_attempts', 'overall_accuracy', 'recent_accuracy',
        'hintCount', 'timeTaken'
    ]
    target = 'correct'

    df_clean = df.dropna(subset=features + [target])
    X = df_clean[features]
    y = df_clean[target]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, shuffle=False)

    # --- UPGRADE 2: Switch to Random Forest ---
    print(f"Training Random Forest Classifier...")
    # n_estimators=100 means it builds 100 decision trees and averages them out
    # max_depth=10 prevents it from memorizing the training data (overfitting)
    model = RandomForestClassifier(n_estimators=100, max_depth=10, class_weight='balanced', random_state=42)
    model.fit(X_train, y_train)

    print("\nEvaluating Model Performance...")
    predictions = model.predict(X_test)
    probabilities = model.predict_proba(X_test)[:, 1]

    print(f"Accuracy: {accuracy_score(y_test, predictions):.4f}")
    print(f"ROC-AUC:  {roc_auc_score(y_test, probabilities):.4f}")
    print("\nClassification Report:")
    print(classification_report(y_test, predictions))

    joblib.dump(model, export_path)
    print(f"\nSuccess! Model exported to: {export_path}")

if __name__ == "__main__":
    dataset_path = 'cleaned2.csv'
    train_evaluate_and_export(dataset_path)