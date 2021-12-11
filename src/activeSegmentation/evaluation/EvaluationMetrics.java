package activeSegmentation.evaluation;

import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import activeSegmentation.IEvaluation;
import weka.classifiers.Evaluation;

import java.util.List;

public class EvaluationMetrics implements IEvaluation {
	
	@Override
	public List<String> getMetrics() {
		// TODO Auto-generated method stub
		return Evaluation.getAllEvaluationMetricNames();
	}

	@Override
	public IEvaluation testModel(IClassifier classifier, IDataSet iData) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
