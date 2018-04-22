package activeSegmentation;

import java.awt.Font;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Common Parameter for plugin, It will allow to use static 
 * Parameters all across the system
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
public class Common {

   // to check the class 
	public static final String CLASS="class";
	 // to check the .jar extension
	public static final String JAR=".jar";
	// to check the .class extension
	public static final String DOTCLASS=".class";
	// to look IFilter interface
	public static final String IFILTER="IFilter";
	// to check the .tif  extension
	public static final String TIFFORMAT=".tif";
	// to give common name to instance
	public static final String INSTANCE_NAME="segment";
	// to set the workload for worker thread
	public static Integer WORKLOAD = 100000000;
	// initial number of classes
	public static int DEFAULT_CLASSES=2;
	// To name the segmentation result
	public static final String FILTERRESULT="Classification result";
	// General font of plugin
	public static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );
	// This is used for Json storage
	public static final String ENABLED="Enabled";
	public static final String DISABLED="Disabled";
	public static final String FILTERS="Filters";
	public static final String FILTER="Filter";
	public static final String SLICE="Slice";
	public static final String ROI_ZIP_PATH="zipfile";
	public static final String FEATURESLIST="FeatureList";
	public static final String CLASSES="classes";
	public static final String IMAGE="image";
	public static final String ROISET="ROISET";
	public static final String ARFF="arff";
	public static final String CLASSIFIER="classifier";
	public static final String COMMENT="Comment";
	public static final String DEFAULTCOMMENT=" DefaultComment";
	public static final String CREATEDATE="CreateDate";
	public static final String  MODIFYDATE="ModifyDate";
	public static final String  PATH="Path";
	public static final String  FORMAT=".zip";
	public static final String ROILIST="RoiList";
	public static final String FILTERFILELIST="FilterFileList";
	// type of learning
	public static final String PASSIVELEARNING="Passive Learning";
	public static final String ACTIVELEARNING="Active Learning";
	// file name for training data used for storage
	public static final String ARFFFILENAME="trainingData";
	// classifier model name
	public static final String CLASSIFIERNAME="classifier.model";
	// to store the learning type in session file
	public static final String LEARNINGTYPE="learningType";
	// session file name
	public static final String FILENAME="project.json";
	
	public static final String TRAININGIMAGE="trainingstack";
	public static final String PROJECTDIR="PROJECTDIR";
	public static final String FEATURESDIR="FEATURESDIR";
	public static final String FILTERSDIR="FILTERSDIR";
	public static final String LEARNINGDIR="LEARNINGDIR";
	public static final String IMAGESDIR="IMAGESDIR";
	public static final String EVALUATIONDIR="EVALUATIONDIR";
	
}
