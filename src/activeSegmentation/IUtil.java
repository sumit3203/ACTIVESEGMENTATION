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
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;

import activeSegmentation.feature.FeatureManager;
import ij.IJ;
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
		List<String> imageList = loadImagesProjectPath(directory, sortFiles);
		for (int i = 0; i < imageList.size(); i++) {
			imageList.set(i, imageList.get(i).substring(imageList.get(i).indexOf("\\") + 1));
		}
		return imageList;
	}

	default public List<String> loadImagesProjectPath(String directory, boolean sortFiles){
		List<String> imageList= new ArrayList<>();
		File folder = new File(directory);
		if (!folder.exists()) {
			IJ.log(directory+" does not exist ");
			throw new RuntimeException(directory+" does not exist ");
		
		}
		// File[] images = folder.listFiles();
		// if (sortFiles && images.length>1) 
		// 	images=sortFiles(images);
		// if (images==null) {
		// 		IJ.log("no files found in "+directory);
		// 		throw new RuntimeException("no files found in "+directory);
		// }
		// for (File file : images) {
		// 	if (file.isFile()) {
		// 		imageList.add(file.getName());
		// 	}

		Stack<File> stack = new Stack<>();
        stack.push(folder);

		while(!stack.isEmpty()) {
			File current = stack.pop();
			if (current.isFile()) {
				String relativePath = getRelativePath(folder, current);
               	String newFileName = replaceDotInFileName(current);
               if (newFileName != null) {
                //    imageList.add(newFileName);
				if (relativePath.isEmpty()) {
					imageList.add(newFileName);
				} else {
					imageList.add(relativePath + File.separator + newFileName);
				}
               }
				// imageList.add(current.getName());
            } else if (current.isDirectory()) {
            	File[] files = current.listFiles();
				if (files != null) {
	                if (sortFiles && files.length > 1) {
	                    files = sortFiles(files);
	                }
	
	                for (File file : files) {
	                    if (file.isFile()) {
							String relativePath = getRelativePath(folder, current);
	                        String newFileName = replaceDotInFileName(file);
	                        if (relativePath.isEmpty()) {
	        					imageList.add(newFileName);
	        				} else {
	        					imageList.add(relativePath + File.separator + newFileName);
	        				}	                        // imageList.add(newFileName);
							// imageList.add(file.getName());
	                    } else if (file.isDirectory()) {
	                        stack.push(file);
	                    }
	                }
	            } else {
					IJ.log("no files found in "+directory);
					throw new RuntimeException("no files found in "+directory);
				}
            } else {
            	IJ.log("no files or folder found in "+ directory);
				throw new RuntimeException("no files or folder found in "+directory);
            }
		}
		// for (int i = 0; i < imageList.size(); i++) {
		// 	imageList.set(i, imageList.get(i).substring(imageList.get(i).indexOf("\\") + 1));
		// }
		return imageList;
	}

	default String getRelativePath(File rootDir, File file) {
        String rootPath = rootDir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        String relativePath = filePath.substring(rootPath.length());
        return relativePath.startsWith(File.separator) ? relativePath.substring(1) : relativePath;
    }

	default String replaceDotInFileName(File file) {
		String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf(".");
        int firstDotIndex = fileName.indexOf(".");

        if (lastDotIndex != -1) {
            if (firstDotIndex == lastDotIndex) {
                return fileName; // no modifications needed
            }

            // Multiple dots found
            String baseName = fileName.substring(0, lastDotIndex);
            String extension = fileName.substring(lastDotIndex + 1);
            baseName = baseName.replaceAll("\\.", "-");

            // Rename the file in the OS
            String newFileName = baseName + '.' + extension;
            File newFile = new File(file.getParentFile(), newFileName);
            if (file.renameTo(newFile)) {
                System.out.println("Renamed file: " + file.getName() + " to " + newFileName);
                return newFileName;
            } else {
                System.out.println("Failed to rename file: " + file.getName());
            }
        } else {
            System.out.println("File has no extension: " + fileName);
        }

        return null;
	}

	default void renameFilesInDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    replaceDotInFileName(file);
                } else if (file.isDirectory()) {
                    renameFilesInDirectory(file);
                }
            }
        }
    }

	default public File[] sortFiles(File[] images) {
		final Pattern p = Pattern.compile("\\d+");
		if (images.length>1) {
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
		}
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
	
	
	/**
	 * 
	 * @param roi
	 * @return
	 */
	default public boolean processibleRoi(Roi roi) {
		boolean ret = (roi != null && !(roi.getType() == Roi.LINE || roi.getType() == Roi.POLYLINE
				|| roi.getType() == Roi.ANGLE || roi.getType() == Roi.FREELINE || roi.getType() == Roi.POINT));
		return ret;
	}


}
