package activeSegmentation.feature;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import activeSegmentation.Common;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeature;
import activeSegmentation.IFilterManager;
import activeSegmentation.io.ProjectInfo;
import activeSegmentation.learning.WekaDataSet;
import weka.core.Attribute;
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
	private ImagePlus originalImage;
	private String[] labels;
	private int numberOfFeatures;
	private InstanceUtil instanceUtil= new InstanceUtil();

	public PixelInstanceCreator( ProjectInfo projectInfo){
		originalImage= IJ.openImage(projectInfo.getTrainingStack());
		featurePath=projectInfo.getProjectPath()+"/"+projectInfo.getProjectName()+"/"+ "Training"+"/filters/";
		File[] featureImages=new File(featurePath+"SLICE-1/").listFiles();
		this.numberOfFeatures=featureImages.length;
		labels=new String[numberOfFeatures];
		for(int i=0; i< featureImages.length; i++){
			System.out.println(featureImages[i].getName());
			String[] featureName=featureImages[i].getName().split("\\.");
			System.out.println(featureName[0]);
			labels[i]=featureName[0];
		}
	}

	/**
	 * Create training instances out of the user markings
	 * @return set of instances (feature vectors in Weka format)
	 */
	@Override
	public void createTrainingInstance(List<String> classLabels, int classes, List<?> features) {
		// TODO Auto-generated method stub
		List<Vector<ArrayList<Roi>>> examples = (List<Vector<ArrayList<Roi>>>)features;
		ArrayList<Attribute> attributes = createFeatureHeader();
		attributes.add(new Attribute(Common.CLASS, addClasstoHeader(classes, classLabels)));
		// create initial set of instances
		trainingData =  new Instances(Common.INSTANCE_NAME, attributes, 1 );
		// Set the index of the class attribute
		trainingData.setClassIndex(numberOfFeatures);
		for(int classIndex = 0; classIndex < classes; classIndex++)
		{
			int nl = 0;
			// Read all lists of examples
			for(int sliceNum = 1; sliceNum <= originalImage.getStackSize(); sliceNum ++){
				ImageStack featureStack=loadFeatureStack(sliceNum);
				//new ImagePlus("test", featureStack).show();
				for(int j=0; j < examples.get(sliceNum-1).get(classIndex).size(); j++)
				{       
					Roi r=  examples.get(sliceNum-1).get(classIndex).get(j);					
					nl += addRectangleRoiInstances( trainingData, classIndex, featureStack, r );
				}
			}
			IJ.log("# of pixels selected as " + classLabels.get(classIndex) + ": " +nl);
		}
		System.out.println(trainingData);
	}

	private ImageStack loadFeatureStack(int sliceNum){

		System.out.println(featurePath+"SLICE-"+sliceNum);
		File[] images=new File(featurePath+"SLICE-"+sliceNum).listFiles();
		ImageStack featureStack = new ImageStack(originalImage.getWidth(), originalImage.getHeight());
		for(File file : images){
			if (file.isFile()) {
				System.out.println(file.getName());
				ImagePlus image=IJ.openImage(featurePath+"SLICE-"+sliceNum+"/"+file.getName());
				for(int i=1; i<= image.getStackSize();i++){
					featureStack.addSlice(image.getImageStack().getSliceLabel(i), image.getImageStack().getProcessor(i));
				}

			}
		}
		return featureStack;
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
			System.out.println(labels[i]);
			attributes.add(new Attribute(labels[i]));
		}

		return attributes;
	}

	private ArrayList<String> addClasstoHeader(int numClasses,List<String> classLabels){
		ArrayList<String> classes = new ArrayList<String>();
		for(int i = 0; i < numClasses ; i ++)
		{			
			for(int n=0; n<originalImage.getStackSize(); n++)
			{
				if(classes.contains(classLabels.get(i)) == false)
					classes.add(classLabels.get(i));
			}
		}			
		return classes;

	}

	@Override
	public List<IDataSet> createAllInstance(List<String> classLabels,
			int classes)
	{
		List<IDataSet> dataSets= new ArrayList<IDataSet>();
		// Read all lists of examples
		for(int sliceNum = 1; sliceNum <= originalImage.getStackSize(); sliceNum ++){
			ImageStack stack=loadFeatureStack(sliceNum);
			System.out.println("sliceNUm"+sliceNum);
			dataSets.add(new WekaDataSet(addRectangleRoiInstances(stack, classLabels, classes)));
		}
		return dataSets;
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
			ImageStack featureStack,List<String> classLabels,
			int classes) 
	{		

		Instances testingData;
		ArrayList<Attribute> attributes = createFeatureHeader();
		attributes.add(new Attribute(Common.CLASS, addClasstoHeader(classes, classLabels)));
		System.out.println(attributes.toString());
		// create initial set of instances
		testingData =  new Instances(Common.INSTANCE_NAME, attributes, 1 );
		// Set the index of the class attribute
		testingData.setClassIndex(numberOfFeatures);

		for( int y = 0; y < originalImage.getHeight(); y++ )				
		{
			for( int x = 0; x < originalImage.getWidth(); x++ ){
				testingData.add( instanceUtil.createInstance(x, y, 0,featureStack ,colorFeatures, oldColorFormat) );

			}		
		}
		// increase number of instances for this class
		System.out.println(testingData.get(1).toString());
		return testingData;		
	}





	@Override
	public String getFeatureName() {
		// TODO Auto-generated method stub
		return featureName;
	}

	@Override
	public IDataSet getDataSet() {
		// TODO Auto-generated method stub

		System.out.println(trainingData.toString());
		return new WekaDataSet(trainingData);
	}

	@Override
	public void setDataset(IDataSet trainingData) {
		// TODO Auto-generated method stub
		this.trainingData= trainingData.getDataset();

	}

	@Override
	public List<IDataSet> createAllInstance(List<String> classLabels, int classes, List<ArrayList<Integer>> testimageindex) {
		// TODO Auto-generated method stub
		return null;
	}

}
