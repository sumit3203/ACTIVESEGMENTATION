package test;

import java.io.File;
import java.security.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.print.DocFlavor.STRING;

import activeSegmentation.ASCommon;
import activeSegmentation.moment.Haralick_feature_;
import activeSegmentation.moment.IJstat_feature_;
import activeSegmentation.moment.Legendre_feature_;
import activeSegmentation.moment.Zernike_feature_;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ijaux.TestUtil;
import ijaux.datatype.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


/**
 * @author Aaryan Gautam, based on test Online Cells testing format
 *
 */
public class TestSQLCells {
	//features
	Legendre_feature_ legendre_filter_=new Legendre_feature_();
	Haralick_feature_ glcm_Filter_= new Haralick_feature_();
	Zernike_feature_ zernike_Filter_= new Zernike_feature_();
	IJstat_feature_ imageJfeatures= new IJstat_feature_();
	// create attribute list
	Map<String, Set<String>> featureNames= new HashMap<>();
	int numberOfFeatures=0;
	ArrayList<Attribute> attributes = new ArrayList<>();
	List<String> classlabels=new LinkedList<>();
	//classifier
	SMO randomForest= new SMO();
	//training Data
	Instances trainingData;
	public final static String pluginname="SQLite client";
    public final static String version="3.36.0.3";
    public final static String driver="org.sqlite.JDBC";
	public final String micro = "\u00B5";
	
	private Connection con;
	private int vid=1;
	private int imageID = 1;
	private int sessionID = 8; // session id to be created
	LocalDateTime trainingStartTime;
	LocalDateTime trainingEndTime;
	static String datasetPath = "C:\\Users\\aarya\\Desktop\\small_data"; // datasetPath to use
	private Map<Pair<String, String>, double[]> imageFeatureMap = new HashMap<>();
	

	public TestSQLCells() {
		// TODO Auto-generated constructor stub
		legendre_filter_.generateFeatures();			
		glcm_Filter_.generateFeatures();			
		zernike_Filter_.generateFeatures();
		featureNames.put(legendre_filter_.getKey(), legendre_filter_.getFeatureNames());
		featureNames.put(glcm_Filter_.getKey(), glcm_Filter_.getFeatureNames());
		featureNames.put(zernike_Filter_.getKey(), zernike_Filter_.getFeatureNames());	
		featureNames.put(imageJfeatures.getKey(), imageJfeatures.getFeatureNames());	
		
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new ImageJ();
		TestSQLCells test= new TestSQLCells();
		System.out.println("RUNNING SQL TEST");
		if (args.length != 0) {
		    datasetPath = args[0];
		}
		String directory = datasetPath;
		test.run(directory);
	}

	public void run(String directory) throws Exception {
		Map<String,List<CellType>>cells=loadCells(directory);
		List<Instance> trainInstances= new ArrayList<>();
		List<Instance> testInstances= new ArrayList<>();
		for(String key: cells.keySet()) {
			//System.out.println(key);
			List<CellType> cellsinside=cells.get(key);
			Collections.shuffle(cellsinside);
			for (CellType cell: cellsinside.subList(0, cellsinside.size()*4/5)) {
				trainInstances.add(cell.getInstances());
			}
			for (CellType cell: cellsinside.subList(cellsinside.size()*4/5,cellsinside.size())) {
				testInstances.add(cell.getInstances());
			}
		}			
		trainClassfier(trainInstances, testInstances,cells);		
		//Collections.shuffle(trainInstances);
	}
	
	public void trainClassfier(List<Instance> trainInstances, List<Instance> testInstances,Map<String,List<CellType>> cells) throws Exception{
		List<Instance> trainInstances2=trainInstances;
		Collections.shuffle(trainInstances);

		//	List<Instance> trainInstances=instances.subList(0, instances.size()*4/5);
		//	List<Instance> testInstances=instances.subList(instances.size()*4/5, instances.size() );
		
			Instances testData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
			testData.setClassIndex(numberOfFeatures-1);
			for(Instance instance:testInstances) {
				testData.add(instance);
			}
			trainingData =  new Instances(ASCommon.INSTANCE_NAME, attributes, 1 );
			trainingData.setClassIndex(numberOfFeatures-1);
			for(Instance instance:trainInstances) {
				trainingData.add(instance);
			}
			//smo.setBatchSize("100");
		//	randomForest.setNumIterations(100);
		try {
			Instances newdata = trainingData;
			trainingStartTime = LocalDateTime.now();
			randomForest.buildClassifier(newdata);
			trainingEndTime = LocalDateTime.now();
			System.out.println(randomForest.toString());
			Evaluation eval = new Evaluation(newdata);
			//eval.evaluateModel(smo, test);

			eval.crossValidateModel(randomForest, newdata, 10,  new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());				

			startDB(trainingData,testData,trainInstances2,testInstances,cells,randomForest);	

			String insertQuery = "INSERT INTO sessions (session_id, start_time, end_time, dataset_path, classifier_output) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement insertStatement = con.prepareStatement(insertQuery);
			// Define the desired date-time format
			String formatPattern = "yyyy-MM-dd HH:mm:ss";

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
	
			// Format the LocalDateTime object using the formatter
			String formattedStartTime = trainingStartTime.format(formatter);
			String formattedEndTime = trainingEndTime.format(formatter);
			
			insertStatement.setInt(1, sessionID);
			insertStatement.setString(2, formattedStartTime);
			insertStatement.setString(3, formattedEndTime);
			insertStatement.setString(4, datasetPath);
			insertStatement.setString(5, eval.toSummaryString("\nResults\n======\n", false));
			insertStatement.executeUpdate();
			con.close();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Map<String,List<CellType>> loadCells(String directory){
		Map<String,List<CellType>> cells= new HashMap<>();
		Set<String> dirs=loadDirectory(directory);
		for(String cellType: dirs) {
			classlabels.add(cellType);
		}
		createHeader(classlabels);		
		Map<String, Integer> classIndexMap=new HashMap<>();
		int index=0;
		for(String label: classlabels) {
			classIndexMap.put(label, index);
			index++;
		}
		for(String cellType: dirs) {
			//System.out.println();
			Set<String> cellsDir=loadImages(directory+"\\"+cellType);
			List<CellType> cellList= new ArrayList<>();
			cells.put(cellType, cellList);
			for(String traincells: cellsDir) {
				String tiffile=directory + "\\" + cellType+"\\"+traincells;
				Instance instance= createInstance(tiffile, cellType,classIndexMap);
				CellType cell= new CellType(tiffile, cellType, instance);
				List<CellType> currentList= cells.get(cellType);
				currentList.add(cell);
				cells.put(cellType, currentList);
				System.out.println(tiffile);
				
			}
			//System.out.println(cellsDir);
		}
		return cells;
	}
	
	int c=0;

	boolean connStart(String dbName) {
		//connecting to database
    	String dbUrl="jdbc:sqlite:"+ dbName;
        try {
            //Class.forName("org.gjt.mm.mysql.Driver");
            Class.forName(driver);
        } catch(Exception ex) {
            IJ.log("Can't find Database driver class: " + ex);
            return false;
        }
        try {
            con = DriverManager.getConnection(dbUrl);
            IJ.log("Connected to " + dbUrl);
            return true;
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
            return false;
        }
    }
	
	private void startDB(Instances trainingData2, Instances testData,List<Instance> trainInstances,List<Instance> testInstances,Map<String,List<CellType>> cells,SMO randomforest) {
		try {
			//starting the database implementation
			trainingData2.addAll(testData);
			Enumeration<Instance> enums=trainingData2.enumerateInstances();			
//			connStart("C:\\Users\\billa\\Documents\\GitHub\\ACTIVESEGMENTATION\\classif.db");
			connStart("C:\\Users\\aarya\\Desktop\\gsoc23\\ACTIVESEGMENTATION\\sqliteTest.db");
			insert(enums,trainInstances,testInstances,cells,randomforest);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public List<String> shuffle4same(List<Instance> trainInstances,List<String> tifs,List<String> ct,List<String> toi){
		//shuffling 4 arrays the same way
		double random_number = Math.floor(Math.random() * 10);
		Collections.shuffle(trainInstances,new Random((int)random_number));
		Collections.shuffle(tifs.subList(0,tifs.size()*4/5),new Random((int)random_number));
		Collections.shuffle(ct.subList(0,ct.size()*4/5),new Random((int)random_number));
		Collections.shuffle(toi.subList(0,toi.size()*4/5),new Random((int)random_number));
		List<String> all = new ArrayList<>();
		for(int i = 0; i < tifs.size(); i++)
		{
		    all.add(tifs.get(i));
		    all.add(ct.get(i));
		    all.add(toi.get(i));
		}
		//returning an array containing tiff name,cell type and type of instance
		//all={tif1,ct1,toi1,tif2,ct2,toi2,....}
		return all;
	}	
	
	
	public static int[] findIndex(double arr[][][], double t)
    {
        // traverse in the array
        for (int i = 0; i < arr.length; i++) 
        {
        	outerLoop:
          for (int a = 0; a < arr[i].length; a++) 
          {
        	  if(arr[i][a]==null) {
        		  continue outerLoop;
        	  }
                 for (int b = 0; b < arr[i][a].length; b++) 
                 {
                    if(arr[i][a][b] ==t) {
                    	int[] pl= {i,a,b};
                    	return pl;
                    }
                 }
           }
        }
        return null;
    }
	
	public void insert(Enumeration<Instance> enums, List<Instance> trainInstances,List<Instance> testInstances,Map<String,List<CellType>> cells,SMO randomforest) throws Exception {
		List<String> tifs=new ArrayList<>();
		List<String> ct=new ArrayList<>();
		List<String> toi=new ArrayList<>();
		double[] overallClassProbabilities = new double[10001];
		int numClasses = 0;
		int totalInstances = 0;
		for(String key: cells.keySet()) {
			List<CellType> cellsinside=cells.get(key);
			Collections.shuffle(cellsinside);
			for (CellType cell: cellsinside.subList(0, cellsinside.size()*4/5)) {
				tifs.add(cell.getTiffile().replace(datasetPath+"\\" +cell.getCelltype()+"\\",""));
				ct.add(cell.getCelltype());
				toi.add("Train");
			}
			for (CellType cell: cellsinside.subList(cellsinside.size()*4/5,cellsinside.size())) {
				tifs.add(cell.getTiffile().replace(datasetPath+"\\"+cell.getCelltype()+"\\",""));
				ct.add(cell.getCelltype());
				toi.add("Test");
			}
		}
		List<String> all=shuffle4same(trainInstances,tifs,ct,toi);
		double [][][] w=randomforest.sparseWeights();//value of attributes
		String [][][] nm=randomforest.attributeNames();//name of attributes
		while (enums.hasMoreElements()) {
			Instance currentList=enums.nextElement();//trainingData.instance(0);
			try {
			double[] probv=randomforest.distributionForInstance(currentList);
			TestUtil.printvector(probv);
			
			double ss=0;
			numClasses = probv.length;
			for (double s: probv) {
				ss+=s;
			}
			System.out.println("\n"+ ss);
			//returning the sum of the probability vectors in order to check if sum=1

	        	
	        	String update="INSERT INTO class_list (session_id, image_name, class_label) "
	            		+ 				"VALUES  ( ?, ?, ?)";
	        	//inserting in class_list the sesion id, image name and the cell class label
	        	PreparedStatement ips=con.prepareStatement(update);
				ips.setInt(1, sessionID);
	        	ips.setString(2,all.get(c)); // image_name.png
	        	ips.setString(3,all.get(c+1)); // class_label
	        	ips.executeUpdate();
				
	        	update="INSERT INTO images ( session_id, image_id, image_name) "
	            		+ 				"VALUES  (  ?, ?, ?)";
	        	//inserting into images the session id, image id and image name        
		        PreparedStatement  vps = con.prepareStatement(update);   
				vps.setInt(1, sessionID);
				vps.setInt(2, imageID);    
				imageID++;
			    vps.setString(3, all.get(c));
			    vps.executeUpdate();
		
				for(int i = 0; i < probv.length; i++) {
					overallClassProbabilities[i] += probv[i];
				}
				totalInstances++;
			    ips.clearParameters();
			    vps.clearParameters();
			    ips.close();
			    vps.close();
			    vid++;
				update="INSERT INTO features_values (session_id, image_id, feature_name, feature_value) "
			        		+ 				"VALUES  (?,?,?,?)";
				    PreparedStatement fps=con.prepareStatement(update);
					String[] features = new String[] {"LM", "ZM", "GLCM"};
					String imageName = all.get(c);
					for(String featureName : features) {
						for(double featureValue : imageFeatureMap.get(new Pair<>(imageName, featureName))) {
							fps.setInt(1,sessionID);
							fps.setInt(2,imageID - 1);
							fps.setString(3, featureName);
							fps.setDouble(4, featureValue);
							fps.executeUpdate();
						}
					}
				    fps.clearParameters();
				    fps.close();
			    c=c+3;
			    if(c==3) {
				    update="INSERT INTO features (session_id, feature_name, feature_parameter) "
			        		+ 				"VALUES  (?, ?, ?)";
				    PreparedStatement fnps=con.prepareStatement(update);
				    String[] max={""};
				    for(String [][] array2d : nm){
			             for(String[] array : array2d){
			            	 if(array!=null) {
			            		 if(array.length>max.length) {
			            			 max=array;
			            		 }
			            	 }
			             }
			         }
				    Map<String, Integer> hm  = new HashMap<String, Integer>();
					for(int f = 0; f < max.length; f++) {
						hm.put(max[f],f);
						String[] featurePair = max[f].split("_", 2);
						String featureName = featurePair[0];
						String featureParameter = featurePair[1];
						fnps.setInt(1, sessionID);
						fnps.setString(2,featureName);
						fnps.setString(3,featureParameter);
						fnps.executeUpdate();
				    }
					fnps.clearParameters();
				    fnps.close();
			    }
		     } catch (SQLException E) {
		    	 IJ.log("SQL message: " + E.getMessage());
		        	
		     }
	        }

			for (int i = 0; i < numClasses; i++) {
				overallClassProbabilities[i] /= totalInstances;
			}

			String update="INSERT INTO class_probabilities ( session_id, class_label, probability) VALUES ( ?, ?, ?)";


			PreparedStatement vlps = con.prepareStatement(update);
			for(int i = 0; i < numClasses; i++) {
				vlps.setInt(1, sessionID);
				vlps.setString(2, classlabels.get(i));
				vlps.setDouble(3, overallClassProbabilities[i]);
				vlps.executeUpdate();
			}
			vlps.clearParameters();
			vlps.close();
			
			// Print the overall class label probabilities
			// for (int i = 0; i < numClasses; i++) {
			// 	System.out.println("Class " + i + " Probability: " + overallClassProbabilities[i]);
			// }
		System.out.println("DONE");
		// con.close();
	}
	
	public void createHeader(List<String> classlabels) {
		int featureSIndex=0;

		for(String key: featureNames.keySet()) {
			//featureIndex.put(key, featureSIndex);
			featureSIndex+=featureNames.get(key).size();
			for(String attribute: featureNames.get(key)) {
				attributes.add(new Attribute(attribute));
			}

		}
		numberOfFeatures=featureSIndex+1;
		attributes.add(new Attribute(ASCommon.CLASS,classlabels));

	}
	public  double[] combine(double[] a, double[] b){
		int length = a.length + b.length;
		double[] result = new double[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public Instance  createInstance(String tiff, String cellType, Map<String, Integer> classIndexMap) {

		ImagePlus currentImage=IJ.openImage(tiff);
		ImageProcessor ip_roi = currentImage.getProcessor();
		Roi roi=new Roi(0, 0, ip_roi.getWidth(), ip_roi.getHeight());
		Pair<String,double[]> features=legendre_filter_.apply(ip_roi,roi );
		Pair<String,double[]> glcmfeatures=glcm_Filter_.apply(ip_roi,roi);
		Pair<String,double[]> zfeatures=zernike_Filter_.apply(ip_roi,roi  );
		String imageName =  tiff.substring(tiff.lastIndexOf('\\') + 1);
		imageFeatureMap.put(new Pair<>(imageName, "LM"), features.second);
		imageFeatureMap.put(new Pair<>(imageName, "GLCM"), glcmfeatures.second);
		imageFeatureMap.put(new Pair<>(imageName, "ZM"), zfeatures.second);


		double[] featuresData=combine(features.second, glcmfeatures.second);
		double[] zfeaturesData=combine(featuresData, zfeatures.second);
		double[] ifeatures=imageJfeatures.apply(ip_roi );
		double[] finalfeatures=combine(zfeaturesData, ifeatures);
		double[] classData;
		double classIndex[]= {classIndexMap.get(cellType)};

		classData=combine(finalfeatures, classIndex);



		Instance instance=new DenseInstance(1.0,classData);

		return instance;


	}
	public Set<String> loadImages(String directory){
		Set<String> imageList= new HashSet<>();
		File folder = new File(directory);
		File[] images = folder.listFiles();

		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}

		return imageList;
	}

	public Set<String> loadDirectory(String directory){
		Set<String> imageList= new HashSet<>();
		File folder = new File(directory);
		File[] images = folder.listFiles();

		for (File file : images) {
			if (file.isDirectory()) {
				imageList.add(file.getName());
			}
		}

		return imageList;
	}


}