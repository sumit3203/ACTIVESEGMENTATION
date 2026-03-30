package activeSegmentation.gui;

import activeSegmentation.IFilter;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

/**
 * Manages real-time filter preview by applying filter operations
 * on a background thread and updating the ImagePlus display.
 *
 * <p>Uses the snapshot/restore pattern for non-destructive preview:
 * the original image data is preserved and restored when preview
 * mode is deactivated.</p>
 *
 * <p>Thread-safety: All ImagePlus updates are dispatched to the EDT
 * via SwingUtilities.invokeLater. A debounce timer coalesces rapid
 * parameter changes (e.g., slider drags) into a single computation.</p>
 *
 * @author [YOUR NAME]
 * @see IFilter
 * @see FilterPanel
 */
public class PreviewManager {

    /** The image being previewed */
    private final ImagePlus targetImage;

    /** Snapshot of the original image before preview started */
    private ImageProcessor originalSnapshot;

    /** The filter currently being previewed */
    private IFilter activeFilter;

    /** Current settings to apply during preview */
    private Map<String, String> currentSettings;

    /** Background worker for filter computation */
    private SwingWorker<ImageProcessor, Void> currentWorker;

    /** Whether preview mode is active */
    private boolean previewActive = false;

    /** Debounce timer to coalesce rapid parameter changes */
    private final Timer debounceTimer;

    /** Delay in ms before triggering a preview update after the last parameter change */
    private static final int DEBOUNCE_DELAY_MS = 200;

    /**
     * Creates a PreviewManager for the given image.
     *
     * @param targetImage the ImagePlus to apply preview filters on
     */
    public PreviewManager(ImagePlus targetImage) {
        this.targetImage = targetImage;
        this.debounceTimer = new Timer(DEBOUNCE_DELAY_MS, e -> executePreview());
        this.debounceTimer.setRepeats(false);
    }

    /**
     * Activates preview mode for the specified filter.
     * Takes a snapshot of the current image state.
     *
     * @param filter the IFilter to preview
     */
    public void activatePreview(IFilter filter) {
        if (targetImage == null || targetImage.getProcessor() == null) {
            System.err.println("PreviewManager: No image available for preview");
            return;
        }
        this.activeFilter = filter;
        this.originalSnapshot = targetImage.getProcessor().duplicate();
        this.previewActive = true;
        System.out.println("PreviewManager: Preview activated for " + filter.getName());
    }

    /**
     * Deactivates preview mode and restores the original image.
     */
    public void deactivatePreview() {
        cancelPendingWork();
        if (originalSnapshot != null && targetImage != null) {
            targetImage.setProcessor(originalSnapshot);
            targetImage.updateAndRepaintWindow();
        }
        this.previewActive = false;
        this.activeFilter = null;
        this.originalSnapshot = null;
        this.currentSettings = null;
        System.out.println("PreviewManager: Preview deactivated, image restored");
    }

    /**
     * Requests a preview update with the given settings.
     * Called when any filter parameter changes (slider, spinner, etc.).
     * Debounces rapid changes to avoid excessive computation.
     *
     * @param settings the current filter parameter values
     */
    public void requestPreviewUpdate(Map<String, String> settings) {
        if (!previewActive || activeFilter == null) return;
        this.currentSettings = settings;
        debounceTimer.restart();
    }

    /**
     * Returns whether preview mode is currently active.
     *
     * @return true if preview is active
     */
    public boolean isPreviewActive() {
        return previewActive;
    }

    /**
     * Executes the filter preview on a background SwingWorker thread.
     * Cancels any previously running preview computation.
     */
    private void executePreview() {
        if (!previewActive || activeFilter == null || originalSnapshot == null) return;

        cancelPendingWork();

        // Capture references for the worker thread
        final IFilter filter = this.activeFilter;
        final Map<String, String> settings = this.currentSettings;
        final ImageProcessor snapshotCopy = originalSnapshot.duplicate();

        currentWorker = new SwingWorker<ImageProcessor, Void>() {
            @Override
            protected ImageProcessor doInBackground() {
                // Update filter settings
                if (settings != null) {
                    filter.updateSettings(settings);
                }
                // Apply filter to a copy of the snapshot (not the original)
                filter.applyFilter(snapshotCopy, null, Collections.emptyList());
                return snapshotCopy;
            }

            @Override
            protected void done() {
                if (isCancelled() || !previewActive) return;
                try {
                    ImageProcessor result = get();
                    if (result != null && targetImage != null) {
                        targetImage.setProcessor(result);
                        targetImage.updateAndRepaintWindow();
                    }
                } catch (Exception ex) {
                    System.err.println("PreviewManager: Preview computation failed: " + ex.getMessage());
                }
            }
        };

        currentWorker.execute();
    }

    /**
     * Cancels any pending or running preview computation.
     */
    private void cancelPendingWork() {
        debounceTimer.stop();
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
    }

    /**
     * Cleanup method — should be called when the FilterPanel is closed.
     */
    public void dispose() {
        deactivatePreview();
        debounceTimer.stop();
    }
}
