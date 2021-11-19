/**
 * 
 */
package activeSegmentation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;

import activeSegmentation.feature.FeatureManager;
import ij.gui.Roi;

/**
 * @author prodanov
 *
 */
public interface IUtil {
	
	/**
	 * Image loader
	 * @param directory
	 * @return
	 */
	default public List<String> loadImages(String directory, boolean sortFiles){
		List<String> imageList= new ArrayList<>();
		File folder = new File(directory);
		if (!folder.exists())
			throw new RuntimeException(directory+" does not exist ");
		File[] images = folder.listFiles();
		if (sortFiles) 
			images=sortFiles(images);
		if (images==null) 
				throw new RuntimeException("no files found in "+directory);
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		return imageList;
	}

	default public File[] sortFiles(File[] images) {
		final Pattern p = Pattern.compile("\\d+");
		Arrays.sort(images, new  Comparator<File>(){
			@Override public int compare(File o1, File o2) {
				Matcher m = p.matcher(o1.getName());
				Integer number1 = null;
				if (!m.find()) {
					return o1.getName().compareTo(o2.getName());
				}
				else {
					Integer number2 = null;
					number1 = Integer.parseInt(m.group());
					m = p.matcher(o2.getName());
					if (!m.find()) {
						return o1.getName().compareTo(o2.getName());
					}
					else {
						number2 = Integer.parseInt(m.group());
						int comparison = number1.compareTo(number2);
						if (comparison != 0) {
							return comparison;
						}
						else {
							return o1.getName().compareTo(o2.getName());
						}
					}
				}
			}}
				);
		return images;
	}
	
	
	default public void updateExampleLists(FeatureManager featureManager, LearningType type, Map<String, JList<String>> exampleList)	{
		final Set<String> keyset=featureManager.getClassKeys();
		for(String key:keyset){
			exampleList.get(key).removeAll();
			Vector<String> listModel = new Vector<>();
			final int slicenum=featureManager.getCurrentSlice();
			final String stype=type.toString();
			final List<Roi> lst= featureManager.getRoiList(key, stype ,slicenum);
			int n=0;
			if (lst!=null)
				n=lst.size();
			for(int j=0; j<n; j++){	
				listModel.addElement(key+ " "+ j + " " +
						featureManager.getCurrentSlice()+" "+type.getLearningType());
			}
			exampleList.get(key).setListData(listModel);
			exampleList.get(key).setForeground(featureManager.getClassColor(key));
		}
	}	

}
