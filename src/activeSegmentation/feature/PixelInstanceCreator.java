package activeSegmentation.feature;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import activeSegmentation.ASCommon;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeature;
import activeSegmentation.learning.WekaDataSet;
import activeSegmentation.prj.ClassInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.util.InstanceUtil;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 *  Feature extraction at Pixel Level
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

public class PixelInstanceCreator implements IFeature {



	private Instances trainingData;

	private String featureName="pixelLevel";
	private  boolean colorFeatures;
	private boolean oldColorFormat = false; 
	private String featurePath;
	private List<String> images;	
	private String[] labels;
	private List<String> classLabels;
	private int numberOfFeatures;
	private InstanceUtil instanceUtil= new InstanceUtil();
	private ProjectInfo projectInfo;
	private String projectString;
	public PixelInstanceCreator( ProjectInfo projectInfo){
		this.projectInfo=projectInfo;
		this.projectString=this.projectInfo.getProjectDirectory().get(ASCommon.K_IMAGESDIR);
		this.images=new ArrayList<String>();
		loadImages(this.projectString);
		this.classLabels=new ArrayList<String>();
		featurePath=this.projectInfo.getProjectDirectory().get(ASCommon.K_FILTERSDIR);
		updateFeatures();
	}

	private void updateFeatures() {
		//System.out.println("pixel creator"+featurePath);
		//File[] featureImages=new File(featurePath+images.get(0).substring(0, images.get(0).lastIndexOf("."))).listFiles();

		File[] featureImages=sortImages(new File(featurePath+images.get(0).substring(0, images.get(0).lastIndexOf("."))).listFiles());

		this.numberOfFeatures=featureImages.length;
		//System.out.println(this.numberOfFeatures);
		labels=new String[numberOfFeatures];
		for(int i=0; i< featureImages.length; i++){
			//System.out.println(featureImages[i].getName());
			String[] featureName=featureImages[i].getName().split("\\.");
			//System.out.println(featureName[0]);
			labels[i]=featureName[0];
		}
	}
	private int loadImages(String directory){
		this.images.clear();
		File folder = new File(directory);
		File[] images = sortImages(folder.listFiles());

		for (File file : images) {
			if (file.isFile()) {
				this.images.add(file.getName());
			}
		}
		return this.images.size();
	}

	/**
	 * Create training instances out of the user markings
	 * @return set of instances (feature vectors in Weka format)
	 */
	@Override
	public void createTrainingInstance(Collection<ClassInfo> classInfos) {
		// TODO Auto-generated method stub
		//IJ.debugMode=false;
		updateFeatures();
		ArrayList<Attribute> attributes = createFeatureHeader();
		attributes.add(new Attribute(ASCommon.CLASS, getCLassLabels(classInfos)));
		// create initial set of instances
		trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		// Set the index of the class attribute
		trainingData.setClassIndex(numberOfFeatures);	

		// Read all lists of examples
		for(String image: images){
			//IJ.log(image);
			ImageStack featureStack=loadFeatureStack(image);
			//System.out.println(featureStack.size());
			//IJ.log(featureStack.size());
			int index=0;
			for(ClassInfo classInfo : classInfos){
				if(classInfo.getTrainingRois(image)!=null) {
					for(Roi roi:classInfo.getTrainingRois(image)) {
						//new ImagePlus("test", featureStack).show();								
						addRectangleRoiInstances( trainingData, index, featureStack, roi );
					}
					//IJ.log(trainingData.toString());
					index++;
				}
			}

		}
		IJ.log(trainingData.toSummaryString());
		//System.out.println(trainingData);
	}


	private List<String> getCLassLabels(Collection<ClassInfo>  classInfos) {

		List<String> labels= new ArrayList<String>();
		for(ClassInfo classInfo:classInfos) {
			labels.add(classInfo.getLabel());
		}
		this.classLabels=labels;
		return labels;
	}

	private ImageStack loadFeatureStack(String imageName){
		String localPath=imageName.substring(0, imageName.lastIndexOf("."));
		//System.out.println(featurePath+images.get(0).substring(0, images.get(0).lastIndexOf(".")));
		IJ.log(featurePath);
		IJ.log(localPath);
		File[] images=sortImages(new File(featurePath+localPath).listFiles());

		ImagePlus firstImage=IJ.openImage(featurePath+localPath+"/"+images[0].getName());
		ImageStack featureStack = new ImageStack(firstImage.getWidth(), firstImage.getHeight());
		for(File file : images){
			if (file.isFile()) {
				//System.out.println(file.getName());
				IJ.log(file.getName());
				ImagePlus image=IJ.openImage(featurePath+localPath+"/"+file.getName());

				featureStack.addSlice(image.getTitle(), image.getProcessor());

			}
		}
		return featureStack;
	}

	private File[] sortImages(File[] images) {
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
	/**
	 * Add training samples from a rectangular roi
	 * 
	 * @param trainingData set of instances to add to
	 * @param classIndex class index value
	 * @param sliceNum number of 2d slice being processed
	 * @param r shape roi
	 * @return number of instances added
	 */
	private int addRectangleRoiInstances(
			final Instances trainingData, 
			int classIndex,
			ImageStack featureStack, 
			Roi r) 
	{		
		int numInstances = 0;
		final Rectangle rect = r.getBounds();
		final Polygon poly=r.getPolygon();
		final int x0 = rect.x;
		final int y0 = rect.y;

		final int lastX = x0 + rect.width;
		final int lastY = y0 + rect.height;

		for( int x = x0; x < lastX; x++ )
			for( int y = y0; y < lastY; y++ )				
			{

				if(poly.contains(new Point(x0, y0))){
					trainingData.add( instanceUtil.createInstance(x, y, classIndex,featureStack ,colorFeatures, oldColorFormat) );
				}				
				// increase number of instances for this class
				numInstances ++;
			}
		return numInstances;		
	}



	private  ArrayList<Attribute> createFeatureHeader(){
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (int i=0; i<numberOfFeatures; i++){
			//System.out.println(labels[i]);
			attributes.add(new Attribute(labels[i]));
		}

		return attributes;
	}



	@Override
	public IDataSet createAllInstances(String image)
	{
		// Read all lists of examples
		ImageStack stack=loadFeatureStack(image);
		return new WekaDataSet(addRectangleRoiInstances(stack));
	}

	/**
	 * Add training samples from a rectangular roi
	 * 
	 * @param trainingData set of instances to add to
	 * @param classIndex class index value
	 * @param sliceNum number of 2d slice being processed
	 * @param r shape roi
	 * @return number of instances added
	 */
	private Instances addRectangleRoiInstances(
			ImageStack featureStack) 
	{		

		Instances testingData;
		ArrayList<Attribute> attributes = createFeatureHeader();
		attributes.add(new Attribute(ASCommon.CLASS, classLabels));
		//System.out.println(attributes.toString());
		// create initial set of instances
		testingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
		// Set the index of the class attribute
		testingData.setClassIndex(numberOfFeatures);

		for( int y = 0; y < featureStack.getHeight(); y++ )				
		{
			for( int x = 0; x < featureStack.getWidth(); x++ ){
				testingData.add( instanceUtil.createInstance(x, y, 0,featureStack ,colorFeatures, oldColorFormat) );

			}		
		}
		// increase number of instances for this class
		//System.out.println(testingData.get(1).toString());
		return testingData;		
	}





	@Override
	public String getFeatureName() {
		return featureName;
	}

	@Override
	public IDataSet getDataSet() {
		//System.out.println(trainingData.toString());
		return new WekaDataSet(trainingData);
	}

	@Override
	public void setDataset(IDataSet trainingData) {
		this.trainingData= trainingData.getDataset();
	}

	@Override
	public Instance createInstance(Roi roi) {
		// TODO Auto-generated method stub
		return null;
	}



}
