package activeSegmentation.filterImpl;


import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;







import activeSegmentation.Common;
import activeSegmentation.FeatureType;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.LearningType;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterManager;
import activeSegmentation.ProjectType;
import activeSegmentation.io.ProjectInfo;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ijaux.scale.Pair;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Filter manager is responsible of loading  new filter from jar, 
 * change the setting of filter, generate the filter results
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
public class FilterManager extends URLClassLoader implements IFilterManager {

	private Map<String,IFilter> filterMap= new HashMap<String, IFilter>();
	//private Map<Integer,FeatureType> featurStackMap= new HashMap<Integer, FeatureType>();
	private IProjectManager projectManager;
	private ProjectInfo projectInfo;

	private ProjectType projectType;

	private IFeatureManagerNew  featureManager;

	public FilterManager(IProjectManager projectManager,IFeatureManagerNew  featureManager){
		super(new URL[0], IJ.class.getClassLoader());
		this.projectManager= projectManager;
		this.projectInfo=projectManager.getMetaInfo();
		projectType=ProjectType.valueOf(this.projectInfo.getProjectType());
		System.out.println("PT: " +ProjectType.valueOf(this.projectInfo.getProjectType()));
		IJ.log("Loading Filters");
		//System.out.println(projectManager.getMetaInfo().getTrainingStack());
		try {
			String path=projectInfo.getPluginPath();
			System.out.println(path);
			if (path!=null)
				loadFilters(path);
			IJ.log("Filters Loaded");
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IJ.log("Filters NOT Loaded. Check path");
		
		this.featureManager= featureManager;
	}


	public  void loadFilters(String home) throws InstantiationException, IllegalAccessException, 
	IOException, ClassNotFoundException {

		System.out.println("home: "+home);
		File f=new File(home);
		String[] plugins = f.list();
		List<String> classes=new ArrayList<String>();
		for(String plugin: plugins){
			//System.out.println(FilterManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			//System.out.println(plugin);
			//System.out.println(installJarPlugins(home+"/"+plugin));
			if(plugin.endsWith(Common.JAR))	{ 
				classes.addAll(installJarPlugins(home,plugin));
				//addFile(home+"/"+plugin);
				String cp=System.getProperty("java.class.path");
				cp+=";"+ home + plugin;
				System.setProperty("java.class.path", cp);
				System.out.println("classpath:  "+cp);
				File g = new File(home,plugin);
				if (g.isFile())
					addJar(g);
			} else if (plugin.endsWith(Common.DOTCLASS)){
				classes.add(plugin);
			}
			//break;
		}
		ClassLoader classLoader= FilterManager.class.getClassLoader();
		for(String plugin: classes){
			//System.out.println(plugin);
			Class<?>[] classesList=(classLoader.loadClass(plugin)).getInterfaces();
			for(Class<?> cs:classesList){
				if(cs.getSimpleName().equals(Common.IFILTER)){
					//System.out.println(cs.getSimpleName());
					//IJ.log(plugin);
					//IJ.debugMode=true;
					IFilter	thePlugIn =(IFilter) (classLoader.loadClass(plugin)).newInstance(); 
					if(thePlugIn.getFilterType()==projectType.getProjectType()){
						System.out.println(thePlugIn.getKey());
						filterMap.put(thePlugIn.getKey(), thePlugIn);
					}

				}
			}

		}

		setFiltersMetaData();

	}

	private void addJar(File f) throws IOException {
		if (f.getName().endsWith(".jar")) {

			try {
				addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
				System.out.println("PluginClassLoader: "+e);
			}
		}
	}
	private List<String> loadImages(String directory){
		List<String> imageList= new ArrayList<String>();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				imageList.add(file.getName());
			}
		}
		return imageList;
	}
	public void applyFilters(){

		String projectString=this.projectInfo.getProjectDirectory().get(Common.IMAGESDIR);
		String filterString=this.projectInfo.getProjectDirectory().get(Common.FILTERSDIR);
		//List<Pair<String,Pair<String[],Double[]>>> featureList= new ArrayList<Pair<String,Pair<String[],Double[]>>>();
		Map<String,List<Pair<String,double[]>>> featureList= new HashMap<>();
		List<String>images= loadImages(projectString);
        Map<String,Set<String>> features= new HashMap<String,Set<String>>();
		for(IFilter filter: filterMap.values()){
			//System.out.println("filter applied"+filter.getName());
			if(filter.isEnabled()){
				if(filter.getFilterType()==ProjectType.CLASSIFICATION.getProjectType()){
					for(String image: images) {
						for(String key: featureManager.getClassKeys()) {
							List<Roi> rois=featureManager.getExamples(key, LearningType.BOTH.name(), image);
							if(rois!=null && !rois.isEmpty()) {
								filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),
										filterString+image.substring(0, image.lastIndexOf(".")),
										rois);
								if(filter.getFeatures()!=null) {
									features.put(filter.getKey(),filter.getFeatureNames());
									List<Pair<String,double[]>> featureL=filter.getFeatures();
									featureList.put(filter.getKey(),featureL);
								}

							}

						}
					}
					
				}
				else{

					for(String image: images) {
						//IJ.log(image);
						filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),filterString+image.substring(0, image.lastIndexOf(".")), null);
					}

				}

			}

		}
		if(featureList!=null && featureList.size()>0) {
			//System.out.println(featureList.size());
			IJ.log("Features computed"+featureList.size());
			//System.out.println(features.size());
			projectInfo.setFeatures(featureList);
			projectInfo.setFeatureNames(features);
			/*	for(Pair<String,Double[]> featureL: featureList.values()) {
				System.out.println(featureL.first);
				System.out.println(Arrays.toString(featureL.second));
			}*/
		}

	}


	public Set<String> getFilters(){
		return filterMap.keySet();
	}

	public Map<String,String> getFilterSetting(String key){

		return filterMap.get(key).getDefaultSettings();
	}


	public boolean isFilterEnabled(String key){

		return filterMap.get(key).isEnabled();
	}


	public boolean updateFilterSetting(String key, Map<String,String> settingsMap){

		return filterMap.get(key).updateSettings(settingsMap);
	}

	public int getNumOfFeatures(String featureName) {
		/*	if(featureName.equals("classLevel"))
			return filterMap.get("ZMC").getDegree();*/
		return 0;
	}




	private  List<String> installJarPlugins(String home,String plugin) throws IOException {
		List<String> classNames = new ArrayList<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(home+plugin));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(Common.DOTCLASS)) {
				String className = entry.getName().replace('/', '.'); // including ".class"
				classNames.add(className.substring(0, className.length() - Common.DOTCLASS.length()));
			}
		}

		return classNames;
	}



	/*	public Instance createInstance(String featureName, int x, int y, int classIndex, int sliceNum) {
		return filterUtil.createInstance(x, y, classIndex,
				featurStackMap.get(sliceNum).getfinalStack(), colorFeatures, oldColorFormat);
	}

	public Instance createInstance(String featureName, int classIndex, int sliceNum){
		try {
			return filterUtil.createInstance(featurStackMap.get(sliceNum).getzernikeMoments(), classIndex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/


	@Override
	public boolean setDefault(String key) {
		// TODO Auto-generated method stub
		//System.out.println("IN SET DEFAULT");
		if(filterMap.get(key).reset())
			return true;

		return false;
	}


	@Override
	public void enableFilter(String key) {
		// TODO Auto-generated method stub
		if(filterMap.get(key).isEnabled()){
			filterMap.get(key).setEnabled(false);	
		}
		else{
			filterMap.get(key).setEnabled(true);	
		}
	}


	@Override
	public void saveFiltersMetaData(){	
		projectInfo= projectManager.getMetaInfo();
		//System.out.println("meta Info"+projectInfo.toString());
		List<Map<String,String>> filterObj= new ArrayList<Map<String,String>>();
		for(String key: getFilters()){
			Map<String,String> filters = new HashMap<String,String>();
			Map<String,String> filtersetting =getFilterSetting(key);
			filters.put(Common.FILTER, key);
			for(String setting: filtersetting.keySet()){
				filters.put(setting, filtersetting.get(setting));		
			}
			filters.put("enabled","false" );
			if(isFilterEnabled(key)){
				filters.put("enabled","true" );	
			}

			filterObj.add(filters);
		}

		projectInfo.setFilters(filterObj);
		projectManager.writeMetaInfo(projectInfo);
	}


	@Override
	public void setFiltersMetaData(){
		projectInfo= projectManager.getMetaInfo();
		List<Map<String,String>> filterObj= projectInfo.getFilters();
		for(Map<String, String> filter: filterObj){
			String filterName=filter.get(Common.FILTER);
			updateFilterSetting(filterName, filter);
			if(filter.get("enabled").equalsIgnoreCase("true")){
				filterMap.get(filterName).setEnabled(true);
			}else{
				filterMap.get(filterName).setEnabled(false);
			}
		}

	}

	@Override
	public Image getFilterImage(String key) {

		return filterMap.get(key).getImage();
	}


}
