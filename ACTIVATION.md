# ⚡ Filter Preview Activation Guide

This document covers the **Filter Preview** feature—a major contribution designed to enhance real-time interactive segmentation by providing an instantaneous visual response when tuning filters.

---

## 🚀 1. What is Filter Preview?

The **Filter Preview** allows users to visualize the effects of the **GAUSS** (Gaussian) filter directly on the image before final application. This enables real-time parameter tuning (e.g., scale and sigma) making the segmentation process both intuitive and computationally efficient.

---

## ⚙️ 2. How to Activate Preview

To "activate" the preview mode within the Active Segmentation platform:

1.  **Launch the Plugin**: Go to `Plugins > Segmentation > Active Segmentation`.
2.  **Initialize Project**:
    - **Project Folder Selection**: Browse and select your working directory.
    - **Project Image Selection**: Choose the image you wish to segment.
    - **Click Finish**: This initializes the workspace.
3.  **Navigate to Filters**: Click the **Select Filters** button on the main dashboard.
4.  **Activate Preview**:
    - Select the **GAUSS** filter tab.
    - Click the **Preview** checkbox.
5.  **Real-Time Tuning**: As you adjust parameters (like `initial scale`), the image display will update dynamically based on your contribution!

---

## 🏗 3. Technical Implementation

The feature is built on a robust, non-destructive architecture located in `activeSegmentation.gui.PreviewManager`:

### 🛡 Snapshot/Restore Pattern
- When preview is activated, a **snapshot** (duplicate) of the original `ImageProcessor` is stored.
- Filter operations are applied to a *copy* of the snapshot, leaving the original data untouched.
- When deactivated, the original snapshot is restored to the `ImagePlus` display.

### ⚡ Performance Optimization
- **Asynchronous Execution**: Filter computations are handled on a background `SwingWorker` thread to keep the user interface responsive.
- **Debouncing**: A `200ms` debounce timer coalesces rapid parameter changes (e.g., while dragging a slider) into a single computation, preventing CPU thrashing.


## 📂 Source Reference
- `PreviewManager.java`: Core logic for managing snapshots and background threads.
- `FilterPanel.java`: UI integration and checkbox event handling.

---

> [!NOTE]
> **GSoC 2026 Milestone**
> The **Filter Preview** for the **GAUSS** filter was developed as a key project milestone for **Google Summer of Code 2026**, aimed at enhancing real-time interactivity within the Active Segmentation platform.
