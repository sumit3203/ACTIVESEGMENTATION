package activeSegmentation.session;

public class FeatureDetail {
    private int sessionId;
    private String featureName;
    private String featureParameter;

    /**
     * 
     * @param sessionId
     * @param featureName
     * @param featureParameter
     */
    public FeatureDetail(int sessionId, String featureName, String featureParameter) {
        this.sessionId = sessionId;
        this.featureName = featureName;
        this.featureParameter = featureParameter;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureParameter() {
        return featureParameter;
    }
}

