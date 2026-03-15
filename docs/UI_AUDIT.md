# UI Audit Report — Active Segmentation Plugin (ASP/IJ)

**Audit conducted by:** Amlan Dalai  
**Date:** March 2026  
**Tool:** Eclipse IDE with WindowBuilder Swing Designer  
**Repository:** https://github.com/sumit3203/ACTIVESEGMENTATION

---

## 1. Overview

This document presents a comprehensive audit of the graphical user interface
of the Active Segmentation Plugin for ImageJ (ASP/IJ), conducted as part of
GSoC 2026 preparation. The goal is to identify UI issues and establish a
baseline for the planned UI modernization effort using WindowBuilder for Eclipse.

---

## 2. Summary

| Metric | Value |
|--------|-------|
| Total GUI files audited | 10 |
| Files using `setLayout(null)` | 6 |
| Inline `Font` declarations bypassing ASCommon | 17 |
| Inline `Color` declarations bypassing ASCommon | 5 |
| WindowBuilder-fully-compatible panels | 2 / 10 |
| `ActionEvent ==` bug occurrences | 10+ |
| `System.out.println` debug logs in production | 15+ |
| Navigation panels (old + new) | 2 (GuiPanel, UIPanel) |

---

## 3. Panel-by-Panel Analysis

### 3.1 UIPanel.java — PRIMARY TARGET ⚠️

**Author:** Dimiter Prodanov (mentor)  
**Purpose:** Primary navigation dashboard — newer experimental design  
**WindowBuilder:** ✅ All components visible in Design view

**Issues found:**
- `setLayout(null)` with hardcoded `setBounds()` coordinates
- 6 inline `new Font("Tahoma")` declarations — inconsistent with
  `ASCommon` which defines `"Arial"` fonts throughout
- 4 `System.out.println` debug statements in constructor
- No button background/foreground styling (missing `buttonBGColor`)
- Missing buttons compared to GuiPanel: Visualization, Back, Exit
  (pending mentor confirmation on intended button set)

**Priority:** High — this is the target design direction

---

### 3.2 GuiPanel.java — LEGACY ✅ PARTIALLY FIXED

**Purpose:** Legacy navigation dashboard (older design)  
**WindowBuilder:** ✅ All components visible (fixed in PR #98)

**Issues fixed:**
- Replaced `setLayout(null)` with `GridBagLayout` ✅
- Named button fields for WindowBuilder compatibility ✅
- Removed `System.out.println` debug statements ✅
- Fixed `ActionEvent ==` comparison bug ✅
- Fixed missing `SessionGUI.setVisible(true)` ✅
- Added Javadoc ✅

---

### 3.3 FeaturePanel.java — COMPLEX ❌

**Purpose:** Main interaction panel for feature extraction and ROI labeling  
**WindowBuilder:** ❌ Complete parsing failure — 0 components visible

**Issues found:**
- Extends `ImageWindow` (ImageJ class) — intentional, required for
  live image canvas; cannot be changed to `JFrame`
- UI built inside `showPanel()` method, not in constructor
- `setLayout(null)` on line 198
- 3 inline `Font` declarations
- Complex conditional UI construction spread across 10+ private methods
- 998 lines — largest GUI file in the codebase

**Note:** The `ImageWindow` architecture is intentional and must be preserved.
Only safe targeted fixes (inline fonts, debug logs) are appropriate here.

**Priority:** Medium — targeted fixes only, no architectural changes

---

### 3.4 SessionGUI.java ⚠️

**Purpose:** Session management panel — view/delete training sessions  
**WindowBuilder:** Partial parse

**Issues found:**
- `setLayout(null)` on line 650
- Locally redefines `labelFONT` — shadows `ASCommon` constant
- Multiple `System.out.println` debug statements

**Priority:** Medium

---

### 3.5 LearningPanel.java ✅ PARTIALLY FIXED

**Purpose:** Weka classifier configuration and model training panel  
**WindowBuilder:** Partial parse (UI built in `showPanel()` method)

**Issues fixed:**
- `ActionEvent ==` replaced with `.equals()` ✅
- `cname != ""` replaced with `!cname.isEmpty()` ✅
- `System.out.println` debug statements removed ✅

**Remaining issues:**
- `setLayout(null)` on line 172
- UI built in `showPanel()` — not WindowBuilder compatible

**Priority:** Medium

---

### 3.6 CreateOpenProjectGUI.java ✅ PARTIALLY FIXED

**Purpose:** Project creation and opening wizard  
**WindowBuilder:** Uses `CardLayout` — partial parse

**Issues fixed:**
- `ActionEvent ==` replaced with `.equals()` for all comparisons ✅
- `System.out.println` debug statement removed ✅

**Remaining issues:**
- `setLayout(null)` in `createProjectPanel()` method
- Form layout not responsive on different screen resolutions

**Priority:** Medium

---

### 3.7 FilterPanel.java ⚠️

**Purpose:** Filter selection and configuration panel  
**WindowBuilder:** Partial parse

**Issues found:**
- `setLayout(null)` on lines 105, 188, 256
- 2 inline `new Color` declarations
- JavaFX dependency (`import javafx.*`) causing compilation
  errors when JavaFX SDK is not on the build path

**Priority:** Medium

---

### 3.8 ViewFilterOutputPanel.java ⚠️

**Purpose:** Filter output visualization with ROI overlay support  
**WindowBuilder:** Partial parse

**Issues found:**
- `setLayout(null)` on line 204
- 4 inline `Font` declarations bypassing ASCommon
- 1 inline `Color` declaration

**Priority:** Medium

---

### 3.9 VisualizationPanel.java ⚠️

**Purpose:** ROC curve and precision-recall visualization panel  
**WindowBuilder:** Partial parse

**Issues found:**
- Locally redefines `labelFONT` — shadows `ASCommon` constant
- Locally redefines `buttonBGColor` with same value as `ASCommon`
- 1 inline `Font` declaration

**Priority:** Low

---

### 3.10 EvaluationPanel.java ✅ ACCEPTABLE

**Purpose:** Model evaluation panel — wraps Weka Explorer UI  
**WindowBuilder:** Partial parse — frame visible, no inner components

**Analysis:**
- UI built in `showPanel()` called from constructor
- No `setLayout(null)` ✅
- No inline Font or Color declarations ✅
- Does not implement `ASCommon` interface
- Weka Explorer manages its own UI internally — blank
  WindowBuilder parse is expected and not a problem

**Priority:** Low — Weka Explorer UI is intentionally delegated

---

## 4. ASCommon Design System Analysis

### 4.1 Current Constants (ASCommon.java)
```java
// Fonts
Font mediumFONT = new Font("Arial", Font.BOLD,  16);
Font labelFONT  = new Font("Arial", Font.BOLD,  13);
Font panelFONT  = new Font("Arial", Font.BOLD,  10);
Font FONT       = new Font("Arial", Font.PLAIN, 10);
Font largeFONT  = new Font("Arial", Font.BOLD,  32);

// Colors
Color buttonColor   = Color.BLUE;
Color buttonBGColor = new Color(192, 192, 192);
Color panelColor    = Color.GRAY;
```

### 4.2 Problems

1. **Incomplete** — no small font, no standard button dimensions
2. **Ignored by UIPanel** — uses `"Tahoma"` instead of `"Arial"`
3. **Shadowed locally** — `SessionGUI` and `VisualizationPanel`
   redefine constants locally instead of using the interface

### 4.3 Proposed Extensions
```java
// Additional fonts needed
Font smallFONT  = new Font("Arial", Font.PLAIN, 11);
Font titleFONT  = new Font("Arial", Font.BOLD,  24);

// Standard dimensions
int BUTTON_HEIGHT   = 35;
int BUTTON_WIDTH    = 150;
int PANEL_PADDING   = 10;

// Additional colors
Color ACCENT_COLOR  = Color.ORANGE;
Color PANEL_BG      = new Color(240, 240, 240);
```

---

## 5. Bug Summary

### Bug 1 — ActionEvent Reference Comparison
**Severity:** Medium  
**Files affected:** GuiPanel, LearningPanel, CreateOpenProjectGUI  
**Description:** `ActionEvent` objects compared using `==` (reference
equality) instead of `.equals()`. Can cause button actions to silently
fail under certain JVM conditions.  
**Status:** Fixed in PRs #97, #100, #101 ✅

### Bug 2 — SessionGUI Never Visible
**Severity:** High  
**File:** GuiPanel.java  
**Description:** `SessionGUI` instance created in `doAction()` but
`setVisible(true)` never called — panel never appears on screen.  
**Status:** Fixed in PR #95 ✅

### Bug 3 — UIPanel Uses Wrong Font Family
**Severity:** Low  
**File:** UIPanel.java  
**Description:** 6 inline `new Font("Tahoma")` declarations override
the `ASCommon` design system which standardizes on `"Arial"`.  
**Status:** Pending

---

## 6. WindowBuilder Compatibility Rules

Based on analysis of working vs broken panels, the following rules
ensure full WindowBuilder compatibility:

1. UI must be built in the constructor — not in `showPanel()` or
   other helper methods
2. Components must be assigned to named instance fields — not
   local variables or factory method return values
3. Each component needs its own `GridBagConstraints` instance —
   sharing a single instance causes parsing failures
4. No conditional UI construction — WindowBuilder cannot parse
   `if/else` blocks that add different components

---

## 7. Recommended Fix Priority

| Priority | Panel | Fix Required |
|----------|-------|-------------|
| 1 | UIPanel | Replace `setLayout(null)` → `GridBagLayout`, fix fonts, add missing buttons |
| 2 | SessionGUI | Replace `setLayout(null)`, fix local font redefinition |
| 3 | LearningPanel | Replace `setLayout(null)`, move UI to constructor |
| 4 | CreateOpenProjectGUI | Replace form layout with `GridBagLayout` |
| 5 | ViewFilterOutputPanel | Replace `setLayout(null)`, fix inline fonts |
| 6 | FilterPanel | Replace `setLayout(null)`, fix inline colors |
| 7 | VisualizationPanel | Fix local constant redefinitions |
| 8 | FeaturePanel | Targeted fixes only — preserve `ImageWindow` architecture |

---

## 8. References

- [GSoC 2026 Project Idea #26](https://summerofcode.withgoogle.com)
- [Eclipse WindowBuilder Documentation](https://www.eclipse.org/windowbuilder/)
- [ImageJ Plugin Development Guide](https://imagej.nih.gov/ij/developer/)
- [Active Segmentation Research Paper](https://pmc.ncbi.nlm.nih.gov/articles/PMC8699732/)
