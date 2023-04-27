package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;

/*
 * 
DONE 
weka.attributeSelection.CfsSubsetEval:
M. A. Hall (1998). Correlation-based Feature Subset Selection for
Machine Learning. Hamilton, New Zealand.


weka.attributeSelection.ConsistencySubsetEval:
H. Liu, R. Setiono: A probabilistic approach to feature selection - A
filter solution. In: 13th International Conference on Machine Learning,
319-327, 1996.


weka.attributeSelection.FCBFSearch:
Lei Yu, Huan Liu: Feature Selection for High-Dimensional Data: A Fast
Correlation-Based Filter Solution. In: Proceedings of the Twentieth
International Conference on Machine Learning, 856-863, 2003.


weka.attributeSelection.GeneticSearch:
David E. Goldberg (1989). Genetic algorithms in search, optimization and
machine learning. Addison-Wesley.


weka.attributeSelection.RaceSearch:
Andrew W. Moore, Mary S. Lee: Efficient Algorithms for Minimizing Cross
Validation Error. In: Eleventh International Conference on Machine
Learning, 190-198, 1994.


weka.attributeSelection.RandomSearch:
H. Liu, R. Setiono: A probabilistic approach to feature selection - A
filter solution. In: 13th International Conference on Machine Learning,
319-327, 1996.


weka.attributeSelection.ReliefFAttributeEval:
Kenji Kira, Larry A. Rendell: A Practical Approach to Feature Selection.
In: Ninth International Workshop on Machine Learning, 249-256, 1992.

Igor Kononenko: Estimating Attributes: Analysis and Extensions of
RELIEF. In: European Conference on Machine Learning, 171-182, 1994.

Marko Robnik-Sikonja, Igor Kononenko: An adaptation of Relief for
attribute estimation in regression. In: Fourteenth International
Conference on Machine Learning, 296-304, 1997.

DONE
weka.attributeSelection.SVMAttributeEval:
I. Guyon, J. Weston, S. Barnhill, V. Vapnik (2002). Gene selection for
cancer classification using support vector machines. Machine Learning.
46:389-422.


weka.attributeSelection.SymmetricalUncertAttributeSetEval:
Lei Yu, Huan Liu: Feature Selection for High-Dimensional Data: A Fast
Correlation-Based Filter Solution. In: Proceedings of the Twentieth
International Conference on Machine Learning, 856-863, 2003.


weka.attributeSelection.WrapperSubsetEval:
Ron Kohavi, George H. John (1997). Wrappers for feature subset
selection. Artificial Intelligence. 97(1-2):273-324.
 */
 
@AFilter(key="AAA", value="No Selection", type=FEATURE, help = "")
public class ID implements IFeatureSelection {

	@Override
	public IDataSet selectFeatures(IDataSet data) {
		return data.copy();
	}

	@Override
	public IDataSet filterData(IDataSet data) {
		return data.copy();
	}


}
