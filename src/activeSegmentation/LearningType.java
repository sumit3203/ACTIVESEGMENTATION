package activeSegmentation;

/*
 * Enumeration for different learning stages 
 */
public enum LearningType {

	TRAINING (1),  
	TESTING(2),
    TRAINING_TESTING(3);
	
	private final int learningType;

	LearningType(int learningType) {
		this.learningType = learningType;
	}

	public int getLearningType() {
		return this.learningType;
	}
}
