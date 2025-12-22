"""
Model evaluation script for anomaly detection models
"""

import pandas as pd
import numpy as np
import joblib
import json
import os
import sys
from sklearn.metrics import classification_report, confusion_matrix
import warnings
warnings.filterwarnings('ignore')

def load_model(model_path):
    """Load trained model"""
    print(f"Loading model from {model_path}...")
    model_data = joblib.load(model_path)
    return model_data

def load_test_data(csv_path):
    """Load test data"""
    print(f"Loading test data from {csv_path}...")
    df = pd.read_csv(csv_path)
    
    # Convert Wake_Up_Time if present
    if 'Wake_Up_Time' in df.columns:
        df['Wake_Up_Time'] = pd.to_datetime(df['Wake_Up_Time'], format='%H:%M:%S', errors='coerce')
        df['Wake_Up_Time_Hours'] = df['Wake_Up_Time'].dt.hour + df['Wake_Up_Time'].dt.minute / 60.0
        df = df.drop('Wake_Up_Time', axis=1)
    
    return df

def prepare_features(df, feature_columns):
    """Prepare features for prediction"""
    available_features = [col for col in feature_columns if col in df.columns]
    df_clean = df[available_features].dropna()
    
    X = df_clean[available_features].values
    X = np.nan_to_num(X, nan=0.0, posinf=0.0, neginf=0.0)
    
    return X, available_features, df_clean.index

def predict_anomalies(model_data, X):
    """Predict anomalies"""
    model = model_data['model']
    scaler = model_data['scaler']
    
    # Scale features
    X_scaled = scaler.transform(X)
    
    # Predict
    predictions = model.predict(X_scaled)
    
    # Get anomaly scores
    if model_data['model_type'] == 'isolation_forest':
        scores = model.score_samples(X_scaled)
        scores_normalized = 1 - (scores - scores.min()) / (scores.max() - scores.min() + 1e-10)
    else:  # One-Class SVM
        scores = model.decision_function(X_scaled)
        scores_normalized = 1 / (1 + np.exp(-scores))
    
    # Convert predictions: -1 = anomaly, 1 = normal
    is_anomaly = (predictions == -1)
    
    return is_anomaly, scores_normalized

def main():
    """Main evaluation function"""
    model_path = os.getenv('MODEL_PATH', './models/trained/isolation_forest_latest.joblib')
    test_csv_path = os.getenv('CSV_PATH', r"C:\Users\loula\eclipse-newworkspace\PBADS-PFA\PFA documentation\Daily_Habit_Tracker.csv")
    
    print("=" * 60)
    print("PBADS - Model Evaluation")
    print("=" * 60)
    
    # Load model
    model_data = load_model(model_path)
    feature_columns = model_data['feature_columns']
    
    # Load test data
    df = load_test_data(test_csv_path)
    
    # Prepare features
    X, available_features, indices = prepare_features(df, feature_columns)
    
    if len(X) == 0:
        print("ERROR: No test data available.")
        return
    
    print(f"\nEvaluating on {len(X)} samples...")
    
    # Predict
    is_anomaly, scores = predict_anomalies(model_data, X)
    
    # Print results
    print(f"\nResults:")
    print(f"Total samples: {len(X)}")
    print(f"Anomalies detected: {is_anomaly.sum()} ({is_anomaly.sum()/len(X)*100:.2f}%)")
    print(f"Normal samples: {(~is_anomaly).sum()} ({(~is_anomaly).sum()/len(X)*100:.2f}%)")
    print(f"\nAnomaly scores:")
    print(f"  Mean: {scores.mean():.3f}")
    print(f"  Std: {scores.std():.3f}")
    print(f"  Min: {scores.min():.3f}")
    print(f"  Max: {scores.max():.3f}")
    
    # Show top anomalies
    top_anomalies = np.argsort(scores)[-10:][::-1]
    print(f"\nTop 10 anomalies (highest scores):")
    for idx in top_anomalies:
        print(f"  Sample {idx}: score={scores[idx]:.3f}, anomaly={is_anomaly[idx]}")
    
    print("\n" + "=" * 60)
    print("Evaluation completed!")
    print("=" * 60)

if __name__ == "__main__":
    main()

