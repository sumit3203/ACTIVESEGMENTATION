package test;

import java.io.File;
import java.sql.*;
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
import java.util.StringTokenizer;
import java.util.Arrays;

import activeSegmentation.ASCommon;
import activeSegmentation.moment.Haralick_feature_;
import activeSegmentation.moment.IJstat_feature_;
import activeSegmentation.moment.Legendre_feature_;
import activeSegmentation.moment.Zernike_feature_;
import activeSegmentation.learning.ClassifierManager;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;
import ijaux.TestUtil;
import ijaux.datatype.Pair;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;



public class TestOnlineCells {
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
	

	public TestOnlineCells() {
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
		TestOnlineCells test= new TestOnlineCells();
		String directory="C:\\Users\\billa\\Documents\\hela_test2\\";
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
			
			randomForest.buildClassifier(newdata);
			System.out.println(randomForest.toString());
			Evaluation eval = new Evaluation(newdata);
			//eval.evaluateModel(smo, test);

			eval.crossValidateModel(randomForest, newdata, 10,  new Random(1));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			System.out.println(eval.toClassDetailsString());				

			startDB(trainingData,testData,trainInstances2,testInstances,cells,randomForest);	
			
			//Evaluation eval1 = new Evaluation(newdata);
			//Instances testNewData = testData;
			//eval1.evaluateModel(randomForest, testNewData);
			//System.out.println(eval1.toSummaryString("\nResults\n======\n", false));
			//System.out.println(eval1.toClassDetailsString());
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
			Set<String> cellsDir=loadImages(directory+cellType);
			List<CellType> cellList= new ArrayList<>();
			cells.put(cellType, cellList);
			for(String traincells: cellsDir) {
				String tiffile=directory+cellType+"\\"+traincells;
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
		//creates connection with the database
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
			//starts the implementation process
			trainingData2.addAll(testData);
			Enumeration<Instance> enums=trainingData2.enumerateInstances();			
			connStart("C:\\Users\\billa\\Documents\\GitHub\\ACTIVESEGMENTATION\\classif.db");		
			insert(enums,trainInstances,testInstances,cells,randomforest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public List<String> shuffle4same(List<Instance> trainInstances,List<String> tifs,List<String> ct,List<String> toi){
		//shuffle 4 arrays the same way
		double random_number = Math.floor(Math.random() * 10);
		Collections.shuffle(trainInstances,new Random((int)random_number));
		Collections.shuffle(tifs.subList(0,tifs.size()*4/5),new Random((int)random_number));
		Collections.shuffle(ct.subList(0,ct.size()*4/5),new Random((int)random_number));
		Collections.shuffle(toi.subList(0,toi.size()*4/5),new Random((int)random_number));
		List<String> all = new ArrayList<>();
		//gets a final array that contains tif images,cell types, type of instances
		for(int i = 0; i < tifs.size(); i++)
		{
		    all.add(tifs.get(i));
		    all.add(ct.get(i));
		    all.add(toi.get(i));
		}	
		//it is like [tif1,ct1,toi1,tif2,ct2,toi2,...]
		return all;
	}
	
	public void insert(Enumeration<Instance> enums, List<Instance> trainInstances,List<Instance> testInstances,Map<String,List<CellType>> cells,SMO randomforest) throws Exception {
		List<String> tifs=new ArrayList<>();
		List<String> ct=new ArrayList<>();
		List<String> toi=new ArrayList<>();
		for(String key: cells.keySet()) {
			List<CellType> cellsinside=cells.get(key);
			Collections.shuffle(cellsinside);
			for (CellType cell: cellsinside.subList(0, cellsinside.size()*4/5)) {
				tifs.add(cell.getTiffile().replace("C:\\Users\\billa\\Documents\\hela_test2\\"+cell.getCelltype()+"\\",""));
				ct.add(cell.getCelltype());
				toi.add("Train");
			}
			for (CellType cell: cellsinside.subList(cellsinside.size()*4/5,cellsinside.size())) {
				tifs.add(cell.getTiffile().replace("C:\\Users\\billa\\Documents\\hela_test2\\"+cell.getCelltype()+"\\",""));
				ct.add(cell.getCelltype());
				toi.add("Test");
			}
		}
		List<String> all=shuffle4same(trainInstances,tifs,ct,toi);
		//shuffle them the same way for training
		double [][][] w=randomforest.sparseWeights();//contains the attribute weights
		String [][][] nm=randomforest.attributeNames();//contains the attribute names
		while (enums.hasMoreElements()) {
			Instance currentList=enums.nextElement();//trainingData.instance(0);
			try {
			double[] probv=randomforest.distributionForInstance(currentList);
			TestUtil.printvector(probv);
			
			double ss=0;
			for (double s: probv) {
				ss+=s;
			}
			System.out.println("\n"+ ss); //prints out that the sum of the probability values are 1
	        
	        	//insert to image_list the name of the image,the instance type,the cell type and the id of the vector
	        	String update="INSERT INTO image_list (image_name, instance_type, cell_type,v_id) "
	            		+ 				"VALUES  ( ?, ?,?,?)";
	        	
	        	PreparedStatement ips=con.prepareStatement(update);
	        	ips.setString(1,all.get(c));
	        	ips.setString(3,all.get(c+1));
	        	ips.setString(2,all.get(c+2));
	        	ips.setInt(4, vid);
	        	ips.executeUpdate();
	        	//inserts to vectors list the image names
	        	update="INSERT INTO vectors ( image_name) "
	            		+ 				"VALUES  (  ?)";
	        		        
		        PreparedStatement  vps = con.prepareStatement(update);       
			    vps.setString(1, all.get(c));
			    vps.executeUpdate();
			    //inserts to the vector_list the vector id and the probability values for this vector id(10 for each)
			    update="INSERT INTO vector_list ( v_id, value) VALUES ( ?, ?)";
			    // for looping into the prob vectors
			    PreparedStatement vlps = con.prepareStatement(update);
			    
			    for(double vec:probv) {
			    	vlps.setInt(1, vid);
				    vlps.setDouble(2, vec);
				    vlps.executeUpdate();
			    }
			    ips.clearParameters();
			    vps.clearParameters();
			    vlps.clearParameters();
			    ips.close();
			    vps.close();
			    vlps.close();
			    vid++;
			    c=c+3;
			    //do it once
			    if(c==3) {
			    	//inserts in the class list the class names
				    update="INSERT INTO class_list (cl_name) "
			        		+ 				"VALUES  (?)";	    	
					PreparedStatement clps=con.prepareStatement(update);
					for(int cl = 0; cl < classlabels.size(); cl++) {
						clps.setString(1,classlabels.get(cl));
						clps.executeUpdate();
				    }
					clps.clearParameters();
				    clps.close();
				    //inserts into the features_names list the names of each feature
				    update="INSERT INTO features_names (f_name) "
			        		+ 				"VALUES  (?)";
				    PreparedStatement fnps=con.prepareStatement(update);
				    String[] max={""};
				    //below we get the max features that may be presented for each vector
				    for(String [][] array2d : nm){
			             for(String[] array : array2d){
			            	 if(array!=null) {
			            		 if(array.length>max.length) {
			            			 max=array;
			            		 }
			            	 }
			             }
			         }
					for(int f = 0; f < max.length; f++) {
						fnps.setString(1,max[f]);
						fnps.executeUpdate();
				    }
					fnps.clearParameters();
				    fnps.close();
				    //insterts into the features_values the value of each feature along with 
				    // the feature name id and the class id
				    update="INSERT INTO features_values (f_value,fn_id,cl_id) "
			        		+ 				"VALUES  (?,?,?)";
				    PreparedStatement fps=con.prepareStatement(update);
				    int cl=1;
				    for (double[][] array_2D: w) {
				    	outerLoop:
						   for (double[] array_1D: array_2D) {
							    int fn=1;
						        if(array_1D!=null) {
						        	for(double elem:array_1D) {
						        		fps.setDouble(1,elem);
						        		fps.setInt(2,fn);
							        	fps.setInt(3,cl);
						        		fps.executeUpdate();
						        		fn++;
						        		}
						        }else {
						        	fps.setInt(1,0);
						        	fps.setInt(2,0);
						        	fps.setInt(3,cl);
						        	fps.executeUpdate();
						        	continue outerLoop;						        	
						        }						        							        	
						      }
				    	cl++;
						   }
				    fps.clearParameters();
				    fps.close();
			    }
		     } catch (SQLException E) {
		    	 IJ.log("SQL message: " + E.getMessage());
		        	
		     }
	        }
		System.out.println("DONE");
		con.close();
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