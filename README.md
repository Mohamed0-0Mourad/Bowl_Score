# BowlScore

<div align="center">
  <video src="[INSERT_GITHUB_ASSET_LINK_HERE]" width="600" />
</div>

**Real-time edge AI tracking system for autonomous RC car bowling competitions.** Runs locally on budget Android hardware with zero network dependency and no frame drops.

---

## Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/TensorFlow_Lite-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Android NDK](https://img.shields.io/badge/Android_NDK-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![YOLO](https://img.shields.io/badge/YOLO-00AAFF?style=for-the-badge)

---

## Key Features

### **Head-to-Head Multiplayer**

Dual-player competitive mode where each contestant selects a distinct neon accent color (electric purple, neon pink) to track their individual RC car trajectory in real-time. The system automatically computes fallen pin counts and winner determination across multiple lanes simultaneously, with live score synchronization and replay analysis.

### **Smart Frame Sampling**

The inference pipeline intentionally operates at a sparse keyframe cadence (one detection every 250 milliseconds) while rendering continuously at 30 FPS. This deliberate undersampling reduces CPU load by up to 5x compared to per-frame inference, allowing complex deep learning models to run without thermal throttling or memory pressure on constrained hardware.

### **Buttery-Smooth Physics**

A custom Alpha-Beta-Gamma kinematic tracker seamlessly interpolates object trajectories between sparse AI detections. The result is a fluid visual experience where neon trajectory lines perfectly track RC car motion, curves, and drifts with zero frame drops and no perceptible latency.

---

## Under the Hood

> **This system runs entirely on a Realme C11 (3GB RAM, MediaTek Helio G35).** No cloud backend. No API calls. No streaming delays. All computation is local and deterministic.

The architecture combines hardware-accelerated video decoding (via MediaMetadataRetriever) with a zero-allocation streaming pipeline that reuses frame buffers in a circular queue. This eliminates garbage collection stalls and ensures consistent 30 FPS presentation even under sustained inference load.

YOLO26n detection models are quantized to INT8 and deployed via TensorFlow Lite with XNNPACK CPU delegation. Initial GPU acceleration attempts were pragmatically abandoned after discovering the target SoC lacks native FP16 support, but the final CPU-only solution achieves production-grade latency and reliability.

**For the full architectural deep-dive, mathematical formulations, and hardware optimization strategies, see the academic report:** [BowlScore_Report.pdf](report/BowlScore_Report.pdf)

---

## Quick Start

### Clone the Repository

```bash
git clone https://github.com/Mohamed0-0Mourad/Bowl_Score.git
cd Bowl_Score/android-app
```

### Build the APK

```bash
# Open the project in Android Studio
# Select Build > Build Bundle(s) / APK(s) > Build APK(s)

# Or via Gradle CLI:
./gradlew assembleDebug
```

### Deploy to Device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Project Structure

```
Bowl_Score/
  android-app/          # Jetpack Compose UI and TensorFlow Lite integration
  cv-pipeline/          # Training scripts, augmentation, and model export utilities
  report/               # Full academic engineering report (LaTeX)
```

---

## Performance Metrics

- **Real-time Latency:** 28-30 FPS on target device
- **Keyframe Detection:** 180-220 ms (every 5th frame at 20 FPS)
- **Memory Peak Usage:** ~320 MB (including frame buffer)
- **Model Size:** 6.2 MB (YOLOv8n) / 11.3 MB (YOLO26n)
- **Dataset:** Custom stadium-angle bowling + Roboflow public dataset

---

## Model Training

Three distinct model architectures were evaluated:

1. **YOLOv8n (Frozen Backbone)** - mAP50: 0.9868, mAP50-95: 0.6612
2. **YOLOv8n (Dropout Regularization)** - mAP50: 0.9844, mAP50-95: 0.6519
3. **YOLO26n (Full Fine-Tuning)** - mAP50: 0.9473, mAP50-95: 0.6563 (selected for motion blur resilience)

Despite marginal accuracy reduction, YOLO26n's superior robustness to motion blur and reduced bounding box jitter under high-speed conditions made it the optimal choice for production deployment. Training curves and confusion matrices are included in the academic report.

---

## Research & Documentation

- **Full Technical Report:** [BowlScore_Report.pdf](report/BowlScore_Report.pdf) - Comprehensive system architecture, hardware optimization strategies, and mathematical formulations
- **Dataset Links:**
  - [Upstream Roboflow Dataset](https://universe.roboflow.com/aryans-workspace-b9ulo/bowling_pin_obb)
  - [Custom Forked Dataset](https://app.roboflow.com/mohamedmoradmagdy1000-gmail-com/bowling_pin_obb-jvd6f/1)
- **Repository:** https://github.com/Mohamed0-0Mourad/Bowl_Score.git

---

## What Makes This Production-Grade

- **Deterministic Rendering:** Zero-allocation streaming pipeline eliminates garbage collection latency spikes
- **Graceful Hardware Fallback:** Pragmatic shift from GPU to CPU delegation when hardware constraints emerge
- **Multiplayer Synchronization:** Frame-accurate score and trajectory state coordination across dual video streams
- **Edge-First Design:** 100% local inference; no network dependency, no privacy leakage, no cold-start latency

---

## Future Enhancements

- Dynamic keyframe scheduling based on motion magnitude
- Temporal model ensembles combining keyframe detections with transformer-based features
- Specialized quantization for bounding box coordinate precision
- Multi-threaded inference pipelining with frame-level parallelism

---

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).

You are free to:
- Share and adapt the material for non-commercial purposes
- Provide appropriate credit and indicate changes

You are not permitted to:
- Use the material for commercial purposes

For details, see the [LICENSE](LICENSE) file or visit [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/).

---

## Contact

Have questions, collaboration ideas, or want to discuss the architecture? Reach out via:

- **Linktree:** [INSERT_LINKTREE_LINK_HERE]

For technical discussions and bug reports, please open an issue on GitHub.

