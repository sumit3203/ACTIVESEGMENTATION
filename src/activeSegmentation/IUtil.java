/**
 * 
 */
package activeSegmentation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	default public List<String> loadImages(String directory){
		List<String> imageList= new ArrayList<>();
		File folder = new File(directory);
		if (!folder.exists())
			throw new RuntimeException(directory+" does not exist ");
		File[] images = folder.listFiles();
		if (images==null) 
				throw new RuntimeException("no files found in "+directory);
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		return imageList;
	}


}
