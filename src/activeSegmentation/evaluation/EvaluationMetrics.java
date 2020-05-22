package activeSegmentation.evaluation;

import activeSegmentation.IDataSet;
import activeSegmentation.IEvaluation;
import weka.classifiers.Evaluation;

import java.util.List;

public class EvaluationMetrics implements IEvaluation {
	/*
	public static void main(String args[]) {
		//System.out.println(Evaluation.getAllEvaluationMetricNames().toString());
	}
	 */
	@Override
	public List<String> getMetrics() {
		// TODO Auto-generated method stub
		return Evaluation.getAllEvaluationMetricNames();
	}

	@Override
	public String testModel(IDataSet instances, List<String> selection) {
		// TODO Auto-generated method stub
		return null;
		
	}

}
