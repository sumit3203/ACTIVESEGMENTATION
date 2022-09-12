package activeSegmentation.learning;

import static activeSegmentation.FilterType.FEATURE;

import activeSegmentation.AFilter;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;

@AFilter(key="NONE", value="No Selection", type=FEATURE)
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
