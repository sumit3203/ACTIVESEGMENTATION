package activeSegmentation.gui;

import ij.IJ;

import java.awt.Desktop;
import java.net.URI;

/**
 * Minimal non-JavaFX browser helper for environments without JavaFX.
 */
public class ABrowser {

    private String resource;

    public ABrowser(String resource) {
        this.resource = resource;
    }

    public void loadURL(final String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                IJ.log("Browser not supported for URL: " + url);
            }
        } catch (Exception e) {
            IJ.log("Unable to open URL: " + url);
        }
    }

    public String getResource() {
        return resource;
    }
}
