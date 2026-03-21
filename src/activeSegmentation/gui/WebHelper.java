package activeSegmentation.gui;

import ij.IJ;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

/**
 * Opens help resources without JavaFX.
 */
public class WebHelper {

    private static String webhlp = "";

    public static void openHelp(String helpPathOrUrl) {
        String target = resolveHelpTarget(helpPathOrUrl);
        if (target == null || target.isEmpty()) {
            IJ.log("Help resource not found: " + helpPathOrUrl);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(target));
            } else {
                IJ.log("Desktop browser not supported. Help URL: " + target);
            }
        } catch (Exception e) {
            IJ.log("Unable to open help URL: " + target);
        }
    }

    private static String resolveHelpTarget(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")) {
            return path;
        }
        URL local = WebHelper.class.getResource(path);
        if (local != null) {
            return local.toExternalForm();
        }
        return path;
    }

    public String getWebHelp() {
        return webhlp;
    }

    public void setWebHelp(String path) {
        String resolved = resolveHelpTarget(path);
        if (resolved != null) {
            webhlp = resolved;
            IJ.log("help " + resolved);
        }
    }
}
