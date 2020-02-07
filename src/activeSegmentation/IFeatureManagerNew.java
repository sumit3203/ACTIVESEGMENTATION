package activeSegmentation;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ij.ImagePlus;
import ij.gui.Roi;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Feature manager is responsible for loading , saving Features, It is also responsible 
 * for storing , Updating ROIS
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
public interface IFeatureManagerNew {

	/**
	 * This method is add example or marked ROI to class(classNum) with trace number n
	 * @param classNum
	 * @param roi
	 * @param nSlice
	 */
	public boolean addExample(String classKey, Roi roi, String type, int sliceNum) ;

	/**
	 * This method is add list of example or marked ROI to class(classNum)
	 *  with trace number n in feature manager
	 * @param classNum
	 * @param roi
	 * @param nSlice
	 */
	public void addExampleList(String classKey, List<Roi> roi, String type, int sliceNum) ;

	/**
	 * This method is to delete the particular example from feature manager
	 * @param classNum
	 * @param nSlice
	 * @param index
	 */
	public void deleteExample(String classKey, int index, String type);

	

	/**
	 * This method return list of ROi for 
	 * particular class and Image Slice
	 * @param classNum
	 * @param nSlice
	 * @return List<Roi>
	 */
	public List<Roi> getExamples(String classKey, String type, int sliceNum);
	
	public List<Roi> getExamples(String key, String type,String image);


	/**
	 * This method will return names of all the classes for GUI
	 * @return List<String>
	 */
	public  Set<String> getClassKeys();

	

	/**
	 * This method will return names of the classes according to key or index
	 * @return String
	 */
	public String getClassLabel(String key);
	
	public Color getClassColor(String key);

	public int getRoiListSize(String key,String learningType, int sliceNum);


	/**
	 * This method will set class name 
	 * @param classNum
	 * @param label
	 */
	public void setClassLabel(String key, String label);


	/**
	 * This method will give number of classes
	 * @return numOfClasses
	 */
	public int getNumOfClasses();

	/**
	 * This method will add new Class for the current problem
	 * 
	 */
	public void addClass();
	
	/**
	 * This method will delete a class
	 * 
	 */
	public void deleteClass(String label);

	/**
	 * This method will set feature parameter of MetaInfo
	 * and store feature data on session file
	 * 
	 */
	public void saveFeatureMetadata();

	/**
	 * This method will set extract features depending on type of
	 * feature like pixel level segmentation, object level classification etc
	 * @param featureType
	 * @return dataset
	 */
	public IDataSet extractFeatures(String featureType);

	/**
	 * This method will give list of available features
	 * @return Set of available features
	 */
	public Set<String> getFeatures();

	/**
	 * This method will add new feature to the platform
	 * @param feature
	 */
	public void addFeatures(IFeature feature);

	/**
	 * This method will set extract features depending on type of
	 * feature like pixel level segmentation, object level classification etc
	 * and apply on lIst of features
	 * @param featureType
	 * @return List of extracted dataset
	 */
	public List<IDataSet> extractAll(String featureType);

	/**
	 * This method will store Zip files of ROI
	 * It will return true if examples are stored successfully
	 * @param  filename
	 * @return boolean
	 */
	public boolean saveExamples(String filename,String classKey,String type, int sliceNum);

	/**
	 * This method is used to open the Zip files of ROI
	 * It will return the list of roi's stored inside the zip file
	 * @param  filename
	 * @return List<Roi>
	 */
	public void uploadExamples(String filename,String classKey,String type,int sliceNum);
	public ImagePlus compute();

	void updateColor(String key, Color value);
	public int getCurrentSlice();
	public int getTotalSlice();
	public ImagePlus getCurrentImage();
	public ImagePlus getNextImage();
	public ImagePlus getPreviousImage();
	public Roi getRoi(String classKey, int index, String type);

	public List<Color> getColors();
	public Map<String,Integer> getClassificationResultMap();
	public String getProjectType();

	public ImagePlus getClassifiedImage();
	public ImagePlus stackedClassifiedImage();
}
