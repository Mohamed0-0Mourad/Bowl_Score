import os
import cv2
import albumentations as A
import glob


def get_augmentation_pipeline():
    """
    Defines the exact stress-test augmentations for the Bowling/RC Car tracker.
    Includes: Perspective Shear, Hue Jitter, Motion Blur, and Synthetic Occlusion.
    """
    return A.Compose(
        [
            # 1. Perspective Shear: ±5 deg horizontally, ±10 deg vertically
            A.Affine(shear={'x': (-5, 5), 'y': (-10, 10)}, p=0.6),

            # 2. Hue Jitter: Shifts hue by ±35 without altering brightness/saturation
            A.HueSaturationValue(hue_shift_limit=35, sat_shift_limit=0, val_shift_limit=0, p=0.6),

            # 3. Motion Blur: Simulates the high-speed RC Car
            A.MotionBlur(blur_limit=15, p=0.4),

            # 4. Occlusion: Drops black boxes to simulate pins hiding behind each other
            A.CoarseDropout(
                num_holes_range=(1, 5),
                hole_height_range=(15, 45),
                hole_width_range=(15, 45),
                fill=0,
                p=0.5
            ),
        ],
        bbox_params=A.BboxParams(format='yolo', label_fields=['class_labels'])
    )

def process_dataset(raw_img_dir, raw_label_dir, output_img_dir, output_label_dir):
    """Applies pipeline to a directory and saves to the processed folder."""
    os.makedirs(output_img_dir, exist_ok=True)
    os.makedirs(output_label_dir, exist_ok=True)

    transform = get_augmentation_pipeline()
    image_paths = glob.glob(os.path.join(raw_img_dir, "*.jpg"))

    for img_path in image_paths:
        filename = os.path.basename(img_path)
        label_path = os.path.join(raw_label_dir, filename.replace('.jpg', '.txt'))

        if not os.path.exists(label_path):
            continue  # Skip if no label (unless handling background images)

        # Load image and labels
        image = cv2.imread(img_path)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        bboxes, class_labels = [], []
        with open(label_path, 'r') as f:
            for line in f.readlines():
                parts = line.strip().split()
                class_labels.append(int(parts[0]))
                bboxes.append([float(x) for x in parts[1:5]])

        # Apply Augmentation
        try:
            transformed = transform(image=image, bboxes=bboxes, class_labels=class_labels)
            aug_image = cv2.cvtColor(transformed['image'], cv2.COLOR_RGB2BGR)
            aug_bboxes = transformed['bboxes']
            aug_labels = transformed['class_labels']

            # Save Augmented Image & Labels (appending '_aug' to filename)
            new_filename = filename.replace('.jpg', '_aug.jpg')
            cv2.imwrite(os.path.join(output_img_dir, new_filename), aug_image)

            new_label_path = os.path.join(output_label_dir, new_filename.replace('.jpg', '.txt'))
            with open(new_label_path, 'w') as f:
                for bbox, label in zip(aug_bboxes, aug_labels):
                    f.write(f"{label} {' '.join(map(str, bbox))}\n")

        except ValueError as e:
            print(f"Skipping {filename} due to bounding box clipping: {e}")


if __name__ == "__main__":
    # Example usage:
    # process_dataset("data/raw/train/images", "data/raw/train/labels",
    #                 "data/processed/train/images", "data/processed/train/labels")
    print("Augmentation pipeline ready.")