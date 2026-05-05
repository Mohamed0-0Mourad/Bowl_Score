import os
from roboflow import Roboflow

def download_dataset(path:str="../data/raw"):
    os.makedirs(path, exist_ok=True)

    # Initialize Roboflow (Replace with your actual API key, workspace, and project names)
    rf = Roboflow(api_key=os.environ['API_KEY'])
    project = rf.workspace(os.environ['WORKSPACE']).project("bowling_pin_obb-jvd6f")

    # Download the dataset in YOLOv8 format directly into the raw data folder
    dataset = project.version(1).download("yolov8", location=path)

    print(f"Raw data downloaded to: {dataset.location}")