package activeSegmentation;

import java.util.ArrayList;
import java.util.List;




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
public interface IFeatureManager {

	/**
	 * This method is add example or marked ROI to class(classNum) with trace number n
	 * @param classNum
	 * @param roi
	 * @param nSlice
	 */
	public void addExample(int classNum, Roi roi, int nSlice) ;

	/**
	 * This method is add list of example or marked ROI to class(classNum)
	 *  with trace number n in feature manager
	 * @param classNum
	 * @param roi
	 * @param nSlice
	 */
	public void addExampleList(int classNum, List<Roi> roi, int nSlice) ;

	/**
	 * This method is to delete the particular example from feature manager
	 * @param classNum
	 * @param nSlice
	 * @param index
	 */
	public void deleteExample(int classNum, int nSlice, int index);

	/**
	 * This method is to delete the particular slice from from particular dataset.
	 * @param SliceNum
	 */
	public void deleteImageType(int classNum, int sliceNum);

	/**
	 * This method is adding marked images in Training or Testing set
	 * @param classNum
	 * @param nSlice
	 */
	public void addImageType(int dataImageTypeId, int nSlice);

	/**
	 * This method is adding marked images in Training or Testing set
	 * @param nSlice
	 */
	public void addTestImageType(int dataImageTypeId, int nSlice);

	/**
	 * This method return list of ROi for 
	 * particular class and Image Slice
	 * @param classNum
	 * @param nSlice
	 * @return List<Roi>
	 */
	public List<Roi> getExamples(int classNum, int n);

	/**
	 * 
	 * @return list of index number of test images
	 */
	public ArrayList<Integer> getImageTestType();

	/**
	 * This method return key used for particular class
	 * @param classNum
	 * @return key
	 */
	public int  getclassKey(String classNum);

	/**
	 * This method will return names of all the classes for GUI
	 * @return List<String>
	 */
	public List<String> getClassLabels();

	/** 
	 * find the class id of this currentSlice in the training DataSet
	 * @param currentSlice
	 * @return the classId in which this slice present, it return -1 if doesn't find the slice
	 * in the training DataSet
	 */
	public int getClassIdofCurrentSlicetraining(int currentSlice);

	/** 
	 * find the class id of this currentSlice in the testing DataSet
	 * @param currentSlice
	 * @return the classId in which this slice present, it return -1 if doesn't find the slice
	 * in the testing DataSet
	 */
	public int getClassIdofCurrentSlicetesting(int currentSlice);

	/**
	 * This method will return names of the classes according to key or index
	 * @return String
	 */
	public String getClassLabel(int index);

	public int getSize(int i, int currentSlice);

	/**
	 * 
	 * @param ClassNum 
	 * @return Training image indexes which is selected under given ClassNum
	 */
	public ArrayList<Integer> getDataImageTypeId(int ClassNum);

	/**
	 * 
	 * @param ClassNum
	 * @return Testing image indexes which is selected under given ClassNum
	 */
	public ArrayList<Integer> getDataImageTestTypeId(int ClassNum);

	/**
	 * This method will set class name 
	 * @param classNum
	 * @param label
	 */
	public void setClassLabel(int classNum, String label);

	/**
	 * This method will set number of classes, Default-2 
	 * @param numOfClasses
	 */
	public void setNumOfClasses(int numOfClasses);

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
	 * This method will set feature meta data for MetaInfo
	 * 
	 */
	public void setFeatureMetadata();

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
	public boolean saveExamples(String filename,List<Roi> roi);

	/**
	 * This method is used to open the Zip files of ROI
	 * It will return the list of roi's stored inside the zip file
	 * @param  filename
	 * @return List<Roi>
	 */
	public List<Roi> openZip(String filename);
	public ImagePlus compute(String featureType);
}
