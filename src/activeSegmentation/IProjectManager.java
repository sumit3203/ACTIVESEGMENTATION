package activeSegmentation;



import activeSegmentation.io.ProjectInfo;
import weka.core.Instances;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for data, It is responsible of doing all the major IO Operations
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
public interface IProjectManager {
	
	/**
	 * This method is used to write ARFF file into the directory
	 * It will store the training and test data in ARFF format
	 * @param  data
	 * @param  filename
	 * @return boolean
	 */
	public boolean writeDataToARFF(Instances data, String filename);
	
	/**
	 * This method is used to read the training and text instance from directory
	 * It will return the genric Idataset instance 
	 * @param  filename
	 * @return IDataset
	 */
	public IDataSet readDataFromARFF(String filename);

	
	
	
	/**
	 * This method will load training data in ARFF format
	 * @param  filename
	 * @return boolean
	 */
	public boolean loadProject(String fileName);
	
	/**
	 * This method will write session file in Meta info format
	 * @param  filename
	 * @return boolean
	 */
	public void writeMetaInfo(ProjectInfo metaInfo);
	
	/**
	 * This method will load session file in Meta info format
	 * @param  filename
	 * @return MetaInfo
	 */
	public ProjectInfo getMetaInfo();
	
	
	/**
	 * This method will set the dataset
	 * @param  data
	 * 
	 */
	public void setData(IDataSet data);
	
	/**
	 * This method will return  the dataset
	 */
	public IDataSet getDataSet();
	

	public ProjectInfo createProject(String projectName, String projectType, String projectDirectory, String projectDescription,String trainingImage,String testImage);
	
	
	
}
