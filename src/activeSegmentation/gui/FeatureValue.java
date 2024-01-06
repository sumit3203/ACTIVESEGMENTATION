package activeSegmentation.gui;

public class FeatureValue {
    private int sessionId;
    private String featureName;
    private String featureValue;

    public FeatureValue(int sessionId, String featureName, String featureValue) {
        this.sessionId = sessionId;
        this.featureName = featureName;
        this.featureValue = featureValue;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureValue() {
        return featureValue;
    }
}
