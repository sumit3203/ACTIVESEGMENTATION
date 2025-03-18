package activeSegmentation.gui;

import ij.IJ;

/**
 * Simplified WebHelper class without JavaFX dependencies
 */
public class WebHelper {
    
    private static final String HELP_URL = "https://github.com/sumit3203/ACTIVESEGMENTATION";
    
    public WebHelper() {
        // Empty constructor
    }
    
    /**
     * Show help in external browser instead of JavaFX browser
     */
    public static void openURL(String url) {
        try {
            IJ.showMessage("Help", "Opening browser to: " + url);
            IJ.log("Opening URL: " + url);
            
            // Use desktop browser instead
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            IJ.error("Could not open URL: " + url);
            ex.printStackTrace();
        }
    }
    
    /**
     * Launch help in default browser
     */
    public static void showHelp() {
        openURL(HELP_URL);
    }
}
