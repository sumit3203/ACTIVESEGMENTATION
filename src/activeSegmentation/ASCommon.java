package activeSegmentation;

import java.awt.Font;
import java.io.File;

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
public interface ASCommon {

   // to check the class 
	public static final String CLASS="class";
	 // to check the .jar extension
	public static final String JAR=".jar";
	// to check the .class extension
	public static final String DOTCLASS=".class";
	// to look IFilter interface
	public static final String IFILTER="IFilter";
	// to look IMoment interface
	public static final String IMOMENT="IMoment";
	// to look IAnnotated interface
	public static final String IANNO="IAnnotated";
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
	
	// This is used for JSON storage
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
	//public static final String CREATEDATE="CreateDate";
	//public static final String MODIFYDATE="ModifyDate";
	public static final String PATH="Path";
	public static final String FORMAT=".zip";
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
	/*  
	 * folder structure
	 */
	/* keys */
	public static final String K_TRAININGIMAGE="trainingstack";
	public static final String K_PROJECTDIR="PROJECTDIR";
	public static final String K_FEATURESDIR="FEATURESDIR";
	public static final String K_FILTERSDIR="FILTERSDIR";
	public static final String K_LEARNINGDIR="LEARNINGDIR";
	public static final String K_IMAGESDIR="IMAGESDIR";
	public static final String K_EVALUATIONDIR="EVALUATIONDIR";
	public static final String K_TESTIMAGESDIR="TESTIMAGESDIR";
	public static final String K_TESTFILTERDIR="TESTFILTERSDIR";
	/* default folders */
	
	public static final String fs=File.separator;
	
	public static final String filterDir= fs+"filters"+fs;
	public static final String testfilterDir=fs+"testfilters"+fs;
	public static final String featureDir=fs+"features"+fs;
	public static final String learnDir=fs+"learning"+fs;
	public static final String evalDir=fs+"evaluation"+fs;
	public static final String imagDir=fs+"images"+fs;
	public static final String testimagDir=fs+"testimages"+fs;
	
	
	/*
	 *  GUI constants
	 */
	public static int  frameWidth=550;
	public static int  frameHeight=450;
	
	public static int  largeframeWidth=1000;
	public static int  largeframeHight=600;
	
	public static final int IMAGE_CANVAS_DIMENSION = 512; //same width and height	
	
	/*
	 * Fonts
	 */
	public static final Font labelFONT = new Font("Arial", Font.BOLD, 13);
	public static final Font panelFONT = new Font("Arial", Font.BOLD, 10);
	// default font for plugin
	public static final Font FONT = new Font("Arial", Font.PLAIN, 10);
	
}
