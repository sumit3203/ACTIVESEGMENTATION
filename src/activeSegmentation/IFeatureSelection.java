package activeSegmentation;

public interface IFeatureSelection {

	public IDataSet selectFeatures(IDataSet trainingData);
	public IDataSet applyOnTestData(IDataSet data);
	/**
	 * Returns a Name of the filter
	 * 
	 * @return Integer
	 */
	public String getName();
}
