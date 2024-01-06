package activeSegmentation.gui;

// ClassList class
public class ClassList {
    private int sessionId;
    private String imageName;
    private String imageLabel;
    private int imageId; // Add the imageId field

    /**
     * 
     * @param sessionId
     * @param imageName
     * @param imageLabel
     */
    public ClassList(int sessionId, String imageName, String imageLabel) {
        this.sessionId = sessionId;
        this.imageName = imageName;
        this.imageLabel = imageLabel;
    }

    public ClassList(int sessionId, String imageName, String imageLabel, int imageId) {
    this.sessionId = sessionId;
    this.imageName = imageName;
    this.imageLabel = imageLabel;
    this.imageId = imageId;
}

    public int getSessionId() {
        return sessionId;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageLabel() {
        return imageLabel;
    }

    public int getImageId() {
    	return imageId;
    }
}