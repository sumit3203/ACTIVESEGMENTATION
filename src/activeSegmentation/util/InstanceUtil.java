package activeSegmentation.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import activeSegmentation.ASCommon;
import activeSegmentation.IDataSet;
import activeSegmentation.learning.WekaDataSet;
import activeSegmentation.prj.ProjectInfo;
import ij.IJ;
import ij.ImageStack;
import ijaux.datatype.ComplexArray;
import weka.classifiers.AbstractClassifier;
//import ijaux.moments.ZernikeMoment.ComplexWrapper;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov, IMEC
 *
 *
 * @contents
 * Utils class for general uses
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
public class InstanceUtil implements ASCommon {

	/**
	 * Create instance (feature vector) of a specific coordinate
	 * 
	 * @param x x- axis coordinate
	 * @param y y- axis coordinate
	 * @param classValue class value to be assigned
	 * @return corresponding instance
	 */
	public DenseInstance createInstance(
			int x, 
			int y, 
			int classValue,
			ImageStack stack,
			boolean colorFeatures,
			boolean oldColorFormat )
	{
		
		final int size=stack.getSize();
		final double[] values = new double[ size + 1 ];
		int n = 0;

		if( colorFeatures == false || oldColorFormat == true)
		{
			for (int z=0; z<size; z++, n++)		
				values[ z ] = stack.getVoxel( x, y, z );
		}
		else
		{
			for (int z=0; z <  size; z++, n++)		
			{
				int c  = (int) stack.getVoxel( x, y, z );
				int r = (c&0xff0000)>>16;
				int g = (c&0xff00)>>8;
				int b = c&0xff;
				values[ z ] = (r + g + b) / 3.0;
			}
		}

		// Assign class
		values[values.length-1] = (double) classValue;
		return new DenseInstance(1.0, values);
	}

	/**
	 * Create instance (feature vector) of a specific slice index
	 * @param rv zernike values of specific slice
	 * @param classValue
	 * @return corresponding instance
	 * @throws Exception
	 */
	public DenseInstance createInstance(ComplexArray rv, int classValue) throws Exception{
		int size=0;
		
		for(int i=0;i<rv.length();i++){
			size++;
			if(rv.Im(i)!=0.0)
				size++;
		}
		double[] final_result = new double[size+1];

		int t=0;
		for(int i=0;i<rv.length();i++){
			final_result[t++] = rv.Re(i);
			if(rv.Im()[i]!=0){
				final_result[t++] = rv.Im(i);
			}
			
		}
		
		//System.out.println("Zernike Values Checking:");
		for(int i=0;i<final_result.length;i++){
			System.out.println(final_result[i]);
		}
		
		
		// Assign class
		final_result[final_result.length-1] = (double) classValue;
		return new DenseInstance(1.0,final_result);
		
	}
	
	
	/**
	 * Read ARFF file
	 * @param filename ARFF file name
	 * @return set of instances read from the file
	 */
	@SuppressWarnings("unused")
	public static IDataSet readDataFromARFF(String filename){
		try{
			BufferedReader reader = new BufferedReader(
					new FileReader(filename));
			try{
				Instances data = new Instances(reader);
				// setting class attribute
				data.setClassIndex(data.numAttributes() - 1);
				reader.close();
				return new WekaDataSet(data);
			} catch(IOException e){
				e.printStackTrace();
				IJ.showMessage("IOException");
				}
		} catch(FileNotFoundException e){
			IJ.showMessage("Arff file not found!");
			}
		return null;
	}
	
	
	//TODO use the default from the interface
		//@Override
		public static boolean writeDataToARFF(Instances data, ProjectInfo projectInfo)	{
			BufferedWriter out = null;
			final String filename=projectInfo.getProjectPath()+fs+
					learnDir+"trainingdata.arff" ;
			System.out.println("Saving "+filename);
			try{
				out = new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream( filename ) ) );

				final Instances header = new Instances(data, 0);
				out.write(header.toString());
				
				for(int i = 0; i < data.numInstances(); i++)			{
					out.write(data.get(i).toString()+"\n");
				}
			}	catch(Exception e)		{
				IJ.log("Error: couldn't write instances into .ARFF file.");
				IJ.showMessage("Exception while saving data as ARFF file");
				e.printStackTrace();
				return false;
			}	finally{
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return true;

		}
		
		
		/**
		 * Load a Weka model (classifier) from a file
		 * @param filename complete path and file name
		 * @return classifier
		 */
		public static AbstractClassifier readClassifier(String filename)
		{
			AbstractClassifier cls = null;
			// deserialize model
			try {
				cls = (AbstractClassifier) SerializationHelper.read(filename);
			} catch (Exception e) {
				System.out.println("Error when loading classifier from " + filename);
				e.printStackTrace();
			}
			return cls;
		}

		 

		/**
		 * Write classifier into a file
		 *  based on TWS saveClassifier
		 *
		 * @param classifier classifier
		 * @param trainHeader train header containing attribute and class information
		 * @param filename name (with complete path) of the destination file
		 * @return false if error
		 */
		public static boolean saveClassifier(
				AbstractClassifier classifier,
				Instances trainHeader,
				String filename)
		{
			File sFile = null;
			boolean saveOK = true;
 		 

			try {
				sFile = new File(filename);
				OutputStream os = new FileOutputStream(sFile);
				if (sFile.getName().endsWith(".gz"))
				{
					os = new GZIPOutputStream(os);
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
				objectOutputStream.writeObject(classifier);
				if (trainHeader != null)
					objectOutputStream.writeObject(trainHeader);
				objectOutputStream.flush();
				objectOutputStream.close();
			}
			catch (Exception e)	{
				System.out.println("Error saving classifier into a file");
				saveOK = false;
				e.printStackTrace();
			}
			if (saveOK)
				System.out.println("Saved model into the file " + filename);

			return saveOK;
		}

		/**
		 * Write classifier into a file
		 *  based on TWS saveClassifier
		 * @param cls classifier
		 * @param filename name (with complete path) of the destination file
		 * @return false if error
		 */
		public static boolean writeClassifier(AbstractClassifier cls, String filename){
			try {
				SerializationHelper.write(filename, cls);
			} catch (Exception e) {
				System.out.println("Error while writing classifier into a file");
				e.printStackTrace();
				return false;
			}
			return true;
		}


}
