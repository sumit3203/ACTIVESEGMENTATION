package activeSegmentation;

public enum LearningType {

	TRAINING (1),  
	TESTING(2);

	private final int learningType;

	LearningType(int learningType) {
		this.learningType = learningType;
	}

	public int getLearningType() {
		return this.learningType;
	}
}
