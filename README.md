# 🔬 Active Segmentation

![License](https://img.shields.io/github/license/sumit3203/ACTIVESEGMENTATION)
![Java](https://img.shields.io/badge/language-Java-orange)
![ImageJ](https://img.shields.io/badge/platform-ImageJ-blue)

Active Segmentation is an interactive image segmentation and classification plugin for ImageJ that leverages both machine learning and geometric features. It enables biologists and researchers to perform pixel-level classification and whole image segmentation without requiring deep machine learning expertise.

---

## 📌 Table of Contents
- [About](#about)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## 📖 About

Active Segmentation integrates:
- **Expert domain knowledge** via partial ground truth labeling
- **Geometric feature extraction** based on multi-scale signal processing
- **Machine learning classifiers** (Random Forest, SVM via Weka)

It supports two main workflows:
1. **Image Segmentation** — pixel-level classification of microscopic images
2. **Image Classification** — whole image classification using the same pipeline

Originally developed at Zuse Institute Berlin (ZIB) and published in:
> Vohra, S.K.; Prodanov, D. *The Active Segmentation Platform for Microscopic Image Classification and Segmentation.* Brain Sci. 2021, 11, 1645.

---

## ✨ Features

- Interactive segmentation using machine learning
- Support for multiple classifiers via Weka (Random Forest, SVM, etc.)
- Geometric and differential invariant feature extraction
- Multi-scale signal processing pipeline
- SQLite-based feature model persistence
- Extensible filter and transform plugin architecture
- Works as an ImageJ plugin — no separate installation needed

---

## 🛠 Prerequisites

Before setting up the project, make sure you have the following installed:

| Tool | Version | Link |
|------|---------|------|
| Java JDK | 8 or later | https://www.oracle.com/java/technologies/downloads/ |
| Eclipse IDE | Latest | https://www.eclipse.org/downloads/ |
| ImageJ | Latest | https://imagej.net/ij/index.html |
| Weka | 3.8+ | https://www.cs.waikato.ac.nz/ml/weka/ |
| Git | Latest | https://git-scm.com/ |

---

## 🚀 Installation

### Step 1: Fork the Repository

1. Go to [sumit3203/ACTIVESEGMENTATION](https://github.com/sumit3203/ACTIVESEGMENTATION)
2. Click the **"Fork"** button in the top right corner
3. This creates a copy under your account at `https://github.com/YOUR_USERNAME/ACTIVESEGMENTATION`

### Step 2: Clone Your Fork

Open a terminal and run:
```bash
git clone https://github.com/YOUR_USERNAME/ACTIVESEGMENTATION
cd ACTIVESEGMENTATION
git remote add upstream https://github.com/sumit3203/ACTIVESEGMENTATION
git remote -v
```

### Step 3: Import into Eclipse

1. Open Eclipse IDE
2. Go to **File → Import → Existing Projects into Workspace**
3. Select the cloned `ACTIVESEGMENTATION` folder
4. Eclipse will auto-detect the project using `.project` and `.classpath`

### Step 4: Add Dependencies

1. Right-click the project → **Build Path → Add External JARs**
2. Add all `.jar` files from the `jars/` folder in the project root

### Step 5: Build the Plugin JAR

1. Right-click `ActSeg.jardesc` → **Create JAR**
2. This will generate the plugin JAR file

### Step 6: Install in ImageJ

1. Copy the generated JAR into ImageJ's `plugins/` directory
2. Launch ImageJ
3. Find **Active Segmentation** under the **Plugins** menu

---

## 🖥 Usage

### Image Segmentation Mode
1. Open a microscopic image in ImageJ
2. Launch Active Segmentation from Plugins menu
3. Draw ROIs (Regions of Interest) on the image to provide ground truth labels
4. Select desired features and a classifier
5. Click **Train** to train the model
6. Click **Segment** to apply segmentation

### Image Classification Mode
1. Open a set of images in ImageJ
2. Launch Active Segmentation
3. Label sample images for each class
4. Train and classify the full dataset

---

## 📁 Project Structure
```
ACTIVESEGMENTATION/
├── src/                          # Java source code
│   ├── Active_Segmentation_.java # Main ImageJ plugin entry point
│   ├── FAbout.java               # About dialog UI
│   ├── activeSegmentation/       # Core segmentation logic
│   ├── dsp/                      # Digital signal processing / filters
│   ├── ijaux/                    # ImageJ auxiliary utilities
│   └── test/                     # Test classes
├── jars/                         # External dependency JARs (Weka, ImageJ)
├── docs/                         # Project documentation
├── resources/                    # Icons and UI resources
├── plugins.config                # ImageJ plugin registration
├── ActSeg.jardesc                # JAR build descriptor
└── README.md                     # This file
```

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create a new branch for your feature or fix:
```bash
git checkout -b feature/your-feature-name
```
3. Make your changes
4. Commit with a clear message:
```bash
git commit -m "feat: describe your change here"
```
5. Push to your fork:
```bash
git push origin feature/your-feature-name
```
6. Open a **Pull Request** against the `master` branch

### Contribution Ideas
- Add unit tests using JUnit
- Improve Javadoc on core interfaces (`IFilter`, `IMoment`)
- Add support for new image filters
- Improve UI/UX of the plugin panels
- Add support for 3D volumetric images

---

## 📄 License

This project is licensed under the terms found in the [LICENSE](LICENSE) file.

---

## 📬 Contact

For questions or suggestions, please open an [issue](https://github.com/sumit3203/ACTIVESEGMENTATION/issues) on GitHub.
