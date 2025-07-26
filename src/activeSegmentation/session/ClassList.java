package activeSegmentation.session;

/**
 * 
 *  used in Sessions ?
 *
 */
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

    /**
     * 
     * @param sessionId
     * @param imageName
     * @param imageLabel
     * @param imageId
     */
    public ClassList(int sessionId, String imageName, String imageLabel, int imageId) {
	    this.sessionId = sessionId;
	    this.imageName = imageName;
	    this.imageLabel = imageLabel;
	    this.imageId = imageId;
    }

    /**
     * 
     * @return
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * 
     * @return
     */
    public String getImageName() {
        return imageName;
    }

	/**
	 * 
	 * @return
	 */
    public String getImageLabel() {
        return imageLabel;
    }

    /**
     * 
     * @return
     */
    public int getImageId() {
    	return imageId;
    }
}