"""
Training script for anomaly detection models
Supports multiple algorithms: Isolation Forest, LSTM, One-Class SVM
"""

import pandas as pd
import numpy as np
import joblib
import json
import os
import sys
from datetime import datetime
from sklearn.ensemble import IsolationForest
from sklearn.svm import OneClassSVM
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.model_selection import train_test_split
import warnings
warnings.filterwarnings('ignore')

# Add parent directory to path for imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def load_data(csv_path):
    """Load data from CSV file"""
    print(f"Loading data from {csv_path}...")
    df = pd.read_csv(csv_path)
    
    # Convert Wake_Up_Time to numeric (hours since midnight)
    if 'Wake_Up_Time' in df.columns:
        df['Wake_Up_Time'] = pd.to_datetime(df['Wake_Up_Time'], format='%H:%M:%S', errors='coerce')
        df['Wake_Up_Time_Hours'] = df['Wake_Up_Time'].dt.hour + df['Wake_Up_Time'].dt.minute / 60.0
        df = df.drop('Wake_Up_Time', axis=1)
    
    # Select features
    feature_columns = [
        'Sleep_Hours', 'Steps', 'Calories_Burned', 
        'Water_Intake_ml', 'Study_Hours', 'Mood_Score'
    ]
    
    # Add wake up time if available
    if 'Wake_Up_Time_Hours' in df.columns:
        feature_columns.append('Wake_Up_Time_Hours')
    
    # Filter available columns
    available_features = [col for col in feature_columns if col in df.columns]
    
    # Remove rows with missing values
    df_clean = df[available_features].dropna()
    
    print(f"Loaded {len(df_clean)} records with {len(available_features)} features")
    print(f"Features: {available_features}")
    
    return df_clean[available_features], available_features

def prepare_features(df, feature_columns):
    """Prepare features for training"""
    X = df[feature_columns].values
    
    # Handle infinite values
    X = np.nan_to_num(X, nan=0.0, posinf=0.0, neginf=0.0)
    
    return X

def train_isolation_forest(X, contamination=0.1, n_estimators=100):
    """Train Isolation Forest model"""
    print(f"\nTraining Isolation Forest (contamination={contamination}, n_estimators={n_estimators})...")
    
    model = IsolationForest(
        contamination=contamination,
        n_estimators=n_estimators,
        random_state=42,
        n_jobs=-1
    )
    
    model.fit(X)
    
    # Calculate anomaly scores
    scores = model.score_samples(X)
    # Convert to 0-1 scale (higher = more anomalous)
    scores_normalized = 1 - (scores - scores.min()) / (scores.max() - scores.min() + 1e-10)
    
    print(f"Anomaly scores range: {scores_normalized.min():.3f} - {scores_normalized.max():.3f}")
    print(f"Mean anomaly score: {scores_normalized.mean():.3f}")
    
    return model, scores_normalized

def train_one_class_svm(X, nu=0.1, gamma='scale'):
    """Train One-Class SVM model"""
    print(f"\nTraining One-Class SVM (nu={nu}, gamma={gamma})...")
    
    model = OneClassSVM(
        nu=nu,
        gamma=gamma,
        kernel='rbf'
    )
    
    model.fit(X)
    
    # Calculate decision function scores
    scores = model.decision_function(X)
    # Convert to 0-1 scale (higher = more anomalous)
    scores_normalized = 1 / (1 + np.exp(-scores))  # Sigmoid transformation
    
    print(f"Anomaly scores range: {scores_normalized.min():.3f} - {scores_normalized.max():.3f}")
    print(f"Mean anomaly score: {scores_normalized.mean():.3f}")
    
    return model, scores_normalized

def evaluate_model(model, X, scores, model_type='isolation_forest'):
    """Evaluate model performance"""
    print(f"\nEvaluating {model_type} model...")
    
    # Predict anomalies (1 = normal, -1 = anomaly for Isolation Forest)
    if model_type == 'isolation_forest':
        predictions = model.predict(X)
        anomalies = (predictions == -1).sum()
    else:  # One-Class SVM
        predictions = model.predict(X)
        anomalies = (predictions == -1).sum()
    
    total = len(X)
    anomaly_rate = anomalies / total
    
    print(f"Total samples: {total}")
    print(f"Detected anomalies: {anomalies} ({anomaly_rate*100:.2f}%)")
    print(f"Normal samples: {total - anomalies} ({(1-anomaly_rate)*100:.2f}%)")
    
    return {
        'total_samples': total,
        'anomalies': anomalies,
        'anomaly_rate': anomaly_rate,
        'mean_score': float(scores.mean()),
        'std_score': float(scores.std()),
        'min_score': float(scores.min()),
        'max_score': float(scores.max())
    }

def save_model(model, scaler, feature_columns, model_path, metadata_path, model_type, metrics):
    """Save model and metadata"""
    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    
    # Save model
    model_data = {
        'model': model,
        'scaler': scaler,
        'feature_columns': feature_columns,
        'model_type': model_type,
        'trained_at': datetime.now().isoformat()
    }
    
    joblib.dump(model_data, model_path)
    print(f"\nModel saved to {model_path}")
    
    # Save metadata
    metadata = {
        'model_type': model_type,
        'feature_columns': feature_columns,
        'trained_at': datetime.now().isoformat(),
        'metrics': metrics,
        'model_path': model_path
    }
    
    with open(metadata_path, 'w') as f:
        json.dump(metadata, f, indent=2)
    
    print(f"Metadata saved to {metadata_path}")

def main():
    """Main training function"""
    # Configuration
    csv_path = os.getenv('CSV_PATH', r"C:\Users\loula\eclipse-newworkspace\PBADS-PFA\PFA documentation\Daily_Habit_Tracker.csv")
    output_dir = os.getenv('OUTPUT_DIR', "./models/trained")
    algorithm = os.getenv('ALGORITHM', 'isolation-forest')  # 'isolation-forest' or 'one-class-svm'
    
    # Hyperparameters
    contamination = float(os.getenv('CONTAMINATION', '0.1'))
    n_estimators = int(os.getenv('N_ESTIMATORS', '100'))
    nu = float(os.getenv('NU', '0.1'))
    
    print("=" * 60)
    print("PBADS - Anomaly Detection Model Training")
    print("=" * 60)
    print(f"Algorithm: {algorithm}")
    print(f"CSV Path: {csv_path}")
    print(f"Output Directory: {output_dir}")
    
    # Load data
    df, feature_columns = load_data(csv_path)
    
    if len(df) == 0:
        print("ERROR: No data loaded. Please check the CSV file path and format.")
        return
    
    # Prepare features
    X = prepare_features(df, feature_columns)
    
    # Scale features
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Train model
    if algorithm == 'isolation-forest':
        model, scores = train_isolation_forest(X_scaled, contamination, n_estimators)
        model_type = 'isolation_forest'
    elif algorithm == 'one-class-svm':
        model, scores = train_one_class_svm(X_scaled, nu)
        model_type = 'one_class_svm'
    else:
        print(f"ERROR: Unknown algorithm: {algorithm}")
        return
    
    # Evaluate model
    metrics = evaluate_model(model, X_scaled, scores, model_type)
    
    # Save model
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    model_filename = f"{model_type}_{timestamp}.joblib"
    metadata_filename = f"{model_type}_{timestamp}_metadata.json"
    
    model_path = os.path.join(output_dir, model_filename)
    metadata_path = os.path.join(output_dir, metadata_filename)
    
    save_model(model, scaler, feature_columns, model_path, metadata_path, model_type, metrics)
    
    print("\n" + "=" * 60)
    print("Training completed successfully!")
    print("=" * 60)

if __name__ == "__main__":
    main()

