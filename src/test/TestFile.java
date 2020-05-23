package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import activeSegmentation.prj.FeatureInfo;
import activeSegmentation.prj.ProjectInfo;

public class TestFile {

	public static void main(String args[]) {
	TestFile testFile= new TestFile();
	testFile.run();
		
	}
	
	
	private void run() {
		ObjectMapper mapper = new ObjectMapper();

		ProjectInfo metaInfo = createDummyObject();

		try {
			// Convert object to JSON string and save into a file directly
			mapper.writeValue(new File("C://Program Files//ImageJ//plugins//activeSegmentation//data.json"), metaInfo);

			// Convert object to JSON string
			String jsonInString = mapper.writeValueAsString(metaInfo);
			System.out.println(jsonInString);

			// Convert object to JSON string and pretty print
			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaInfo);
			System.out.println(jsonInString);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ProjectInfo createDummyObject(){
		ProjectInfo metaInfo= new ProjectInfo();
		//metaInfo.setComment("Dummy");
		metaInfo.projectDescription="Dummy";
		metaInfo.setCreatedDate("12/06/2016");
		metaInfo.setModifyDate("12/07/2016");
		metaInfo.setProjectPath("C://Program Files//ImageJ//plugins//activeSegmentation//");
		
		List<Map<String,String>> filters= new ArrayList<Map<String,String>>();
		Map<String,String> filterMap= new HashMap<String, String>();
		filterMap.put("Filter", "LOG1");
		filterMap.put("G_SEP", "true");
		filterMap.put("G_len", "2");
		filterMap.put("G_SCNORM", "false");
		filterMap.put("G_MAX", "16");
		Map<String,String> filterMap1= new HashMap<String, String>();
		filterMap1.put("Filter", "ALOG");
		filterMap1.put("G_SEP", "true");
		filterMap1.put("G_len", "2");
		filterMap1.put("G_SCNORM", "false");
		filterMap1.put("G_MAX", "16");
		
		filters.add(filterMap);
		filters.add(filterMap1);
		FeatureInfo featureInfo= new FeatureInfo();
		featureInfo.setZipFile("Roiset1.zip");
		Map<String,List<String>> sliceList= new HashMap<String, List<String>>();
		List<String> rois=new ArrayList<String>();
		rois.add("0002-0295-0626");
		rois.add("0002-0295-0627");
		rois.add("0002-0295-0628");		
		List<String> rois1=new ArrayList<String>();
		rois1.add("0002-0295-0624");
		rois1.add("0002-0295-0622");
		rois1.add("0002-0295-0623");	
		sliceList.put("slice1", rois);	
		sliceList.put("slice2", rois1);
		featureInfo.setTrainingList(sliceList);
		metaInfo.setFilters(filters);
		
		
		List<FeatureInfo> featureInfos= new ArrayList<FeatureInfo>();
		featureInfos.add(featureInfo);
		metaInfo.setFeatureList(featureInfos);
		
		Map<String,String> learning= new HashMap<String, String>();
		learning.put("classifier", "classifier.model");
		learning.put("arff", "data.arff");
		learning.put("setting", "activelearning");
		metaInfo.setLearning(learning);
		
		return metaInfo;
	}

}
