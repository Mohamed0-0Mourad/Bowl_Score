# BowlScore
Demo on test scene (not existent in training dataset).
<div align="center">
  <video src="https://github.com/user-attachments/assets/9d8801d1-14bd-4c02-aefa-048726d11d27" width="600" />
</div>

**A real-time edge AI tracking system for physical RC car bowling.** Built for maximum performance, BowlScore runs entirely offline on budget Android hardware, delivering flawless tracking with zero network dependency and zero frame drops.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/TensorFlow_Lite-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Android NDK](https://img.shields.io/badge/Android_NDK-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![YOLO](https://img.shields.io/badge/YOLO-00AAFF?style=for-the-badge)

---

## Why BowlScore Stands Out

### **Head-to-Head Multiplayer**
> A built-in dual-player competitive mode where contestants select distinct neon accent colors (Electric Purple, Neon Pink) to track their individual runs. The system tracks the RC car trajectory, automatically computes fallen pin counts, and determines the winner with live score synchronization.

### **Smart Frame Sampling & Buttery-Smooth Physics**
> To maintain a blazing-fast 20-30 FPS on edge hardware, the AI model intentionally samples only keyframes. You never notice the gaps because they are seamlessly bridged using a custom Alpha-Beta-Gamma kinematic tracker. This ensures the digital trajectory lines perfectly match the physical drifts, curves, and high-speed momentum of the RC car. 

### **Zero-Allocation Architecture**
> Mobile devices crash when memory fills up. BowlScore uses a "Draw and Drop" streaming pipeline that completely eliminates garbage collection stalls. Frame buffers are reused in a strict circular queue, ensuring a perfectly flat memory profile even under sustained inference load.

---

## Under the Hood

> **This system runs entirely on a Realme C11 (3GB RAM, MediaTek Helio G35).** No cloud backend. No API calls. All computation is local, deterministic, and highly optimized for budget silicon.

When deploying to edge devices, hardware fragmentation is the ultimate bottleneck. After discovering the target MediaTek SoC lacked native FP16 GPU support, we pragmatically pivoted the architecture. We combined hardware-accelerated video decoding with a highly optimized XNNPACK CPU pipeline running quantized YOLO26n models. The result is a production-grade application that avoids thermal throttling and maintains a strict 20-30 FPS visual output.

**For the full architectural deep-dive, mathematical formulations, and hardware optimization strategies, read the official engineering report:** **[Read the BowlScore Technical Report (PDF)](report/BowlScore_Report.pdf)**

---

## Technical Specifications & Performance

- **Inference Speed:** 20-30 FPS sustained on target budget device.
- **Memory Footprint:** Flatlined at ~320 MB peak (zero heap accumulation).
- **Model Architecture:** YOLO26n (Full Fine-Tuning). Selected specifically over YOLOv8n due to its superior resilience to motion blur and reduced bounding box jitter at high speeds.
- **Deployment:** TensorFlow Lite INT16 with XNNPACK CPU delegation.
- **Dataset:** Custom stadium-angle bowling imagery combined with Roboflow public datasets.
  - [Upstream Roboflow Dataset](https://universe.roboflow.com/aryans-workspace-b9ulo/bowling_pin_obb)
  - [Custom Forked Dataset](https://app.roboflow.com/mohamedmoradmagdy1000-gmail-com/bowling_pin_obb-jvd6f/1)

---

## Developer Setup

### 1. Clone the Repository

```bash
git clone [https://github.com/Mohamed0-0Mourad/Bowl_Score.git](https://github.com/Mohamed0-0Mourad/Bowl_Score.git)
cd Bowl_Score/android-app

```

### 2. Project Structure

```text
Bowl_Score/
  android-app/          # Jetpack Compose UI, DrawThread, and TFLite integration
  cv-pipeline/          # Training scripts, augmentation, and model export utilities
  report/               # Full academic engineering report (LaTeX source & PDF)

```

### 3. Build & Deploy

Open the `android-app` folder in Android Studio, or build directly via the Gradle CLI:

```bash
# Build the APK
./gradlew assembleDebug

# Deploy to connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

```

---

## License & Contact

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0). You are free to share and adapt the material for non-commercial purposes with appropriate credit.

Have questions, collaboration ideas, or want to discuss edge AI architecture? Reach out:

* **Linktree:** [LinkTree](https://lnk.bio/MohamedMourad)
* **Bug Reports:** Please open an issue directly on this repository.
---
