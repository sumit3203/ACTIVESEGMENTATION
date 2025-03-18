package activeSegmentation.gui;

import javax.swing.JPanel;

/**
 * Simplified ABrowser class without JavaFX dependencies
 */
public class ABrowser extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor using awt/swing instead of JavaFX
     */
    public ABrowser() {
        super();
    }
    
    /**
     * Constructor that takes a URL string
     */
    public ABrowser(String url) {
        this();
    }
    
    /**
     * Load URL method that does nothing in this simplified version
     */
    public void loadURL(String url) {
        // Does nothing in simplified version
    }
}