from ultralytics import YOLO
import os
import argparse


def export_to_mobile(weights_path, data_path, imgsz=640):
    """
    Converts a fine-tuned YOLOv8 PyTorch model to INT8 Quantized TFLite.
    """

    if not os.path.exists(weights_path):
        print(f"Error: Could not find weights at {weights_path}")
        return

    print("? Loading PyTorch model...")
    model = YOLO(weights_path)

    print("? Starting INT8 TFLite Export. This may take a few minutes for calibration...")

    export_path = model.export(
        format="tflite",
        int8=True,
        data=data_path,
        imgsz=imgsz,
        optimize=True
    )

    print("? Export complete!")
    print(f"? Your mobile-ready model is located at: {export_path}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export YOLOv8 model to INT8 TFLite")

    parser.add_argument("--weights", required=True, help="Path to best.pt weights")
    parser.add_argument("--data", required=True, help="Path to data.yaml")
    parser.add_argument("--imgsz", type=int, default=640, help="Image size (default: 640)")

    args = parser.parse_args()

    export_to_mobile(
        weights_path=args.weights,
        data_path=args.data,
        imgsz=args.imgsz
    )