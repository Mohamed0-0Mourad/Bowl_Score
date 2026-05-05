import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os


def plot_prettified_loss(csv_path: str, save_dir: str = "."):
    """
    Parses YOLOv8 results.csv and plots a prettified Training vs Validation Loss curve.
    """
    if not os.path.exists(csv_path):
        print(f"Error: Could not find {csv_path}. Has training finished?")
        return

    # Load data and strip whitespace from YOLO's column names
    df = pd.read_csv(csv_path)
    df.columns = df.columns.str.strip()

    # YOLO calculates three losses: Box (coordinates), Cls (class label), DFL (distribution)
    # We combine them to see the overall network performance
    df['total_train_loss'] = df['train/box_loss'] + df['train/cls_loss'] + df['train/dfl_loss']
    df['total_val_loss'] = df['val/box_loss'] + df['val/cls_loss'] + df['val/dfl_loss']

    # Set an elegant, professional seaborn theme
    sns.set_theme(style="whitegrid", context="talk", palette="deep")

    plt.figure(figsize=(12, 7))

    # Plot the curves
    sns.lineplot(x=df['epoch'], y=df['total_train_loss'], label='Training Loss', linewidth=3)
    sns.lineplot(x=df['epoch'], y=df['total_val_loss'], label='Validation Loss', linewidth=3, linestyle='--')

    # Styling
    plt.title("YOLOv8 Object Detection: Training vs Validation Loss", fontsize=20, pad=20, fontweight='bold')
    plt.xlabel("Epoch", fontsize=16, labelpad=15)
    plt.ylabel("Total Loss (Box + Cls + DFL)", fontsize=16, labelpad=15)
    plt.legend(frameon=True, shadow=True, fontsize=14)

    # Highlight minimum validation loss (best weights)
    best_epoch = df.loc[df['total_val_loss'].idxmin()]
    plt.axvline(best_epoch['epoch'], color='gray', linestyle=':', alpha=0.6)
    plt.text(best_epoch['epoch'] + 1, best_epoch['total_val_loss'], 'Best Weights Saved', color='gray', fontsize=12)

    plt.tight_layout()

    # Save the output
    save_path = os.path.join(save_dir, "loss_curve_prettified.png")
    plt.savefig(save_path, dpi=300, bbox_inches='tight')
    print(f"Prettified plot saved to: {save_path}")

    plt.show()


# If running directly
if __name__ == "__main__":
    plot_prettified_loss("../runs/detect/v1_frozen_backbone/results.csv")