package activeSegmentation.session;

// Session class
public class Session {
    private int ss_id;
    private int sessionId;
    private String startTime;
    private String endTime;
    private String datasetPath;
    private String classifierOutput;

	/**
	 * 
	 * @param ss_id
	 * @param sessionId
	 * @param startTime
	 * @param endTime
	 * @param datasetPath
	 * @param classifierOutput
	 */
    public Session(int ss_id, int sessionId, String startTime, String endTime, String datasetPath, String classifierOutput) {
        this.ss_id = ss_id;
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.datasetPath = datasetPath;
        this.classifierOutput = classifierOutput;
    }

    public int getSSId() {
        return ss_id;
    }
    
    public int getSessionId() {
        return sessionId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public String getClassifierOutput() {
        return classifierOutput;
    }
}
