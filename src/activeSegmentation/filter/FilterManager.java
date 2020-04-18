package activeSegmentation.filter;


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

import activeSegmentation.ASCommon;
import activeSegmentation.FilterType;
import activeSegmentation.IProjectManager;
import activeSegmentation.LearningType;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterViz;
import activeSegmentation.IMoment;
import activeSegmentation.IFilterManager;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureContainer;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.prj.ProjectInfo;
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

	private Map<String, IFilter> filterMap= new HashMap<String, IFilter>();
	private Map<String, IMoment> momentMap= new HashMap<String, IMoment>();
	
	private Map<String, Map<String,String>> annotationMap= new HashMap<String, Map<String,String>>();
 
	private IProjectManager projectManager;
	private ProjectInfo projectInfo;

	private ProjectType projectType;

	private FeatureManager  featureManager;

	public FilterManager(IProjectManager projectManager, FeatureManager  featureManager){
		super(new URL[0], IJ.class.getClassLoader());
		
		this.projectManager= projectManager;
		this.projectInfo=projectManager.getMetaInfo();
		String pt=this.projectInfo.getProjectType();
	
		if (pt.equalsIgnoreCase("SEGMENTATION"))
			projectType=ProjectType.SEGM;
		else 
			projectType=ProjectType.CLASSIF;
		IJ.log("Project Type: " +  (projectInfo.getProjectType()));
		System.out.println("Project Type: "+projectType +" pt "+ pt);
		IJ.log("Loading Filters");
		
		try {
			List<String> jars=projectInfo.getPluginPath();
			System.out.println("plugin path: "+jars);
			if (jars!=null)
				loadFilters(jars);
			IJ.log("Filters Loaded");
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
			IJ.log("Filters NOT Loaded. Check path");
		}
		
		this.featureManager= featureManager;
	}


	public  void loadFilters(List<String> plugins) throws InstantiationException, IllegalAccessException, 
	IOException, ClassNotFoundException {

		//System.out.println("home: "+home);
		//File f=new File(home);
		//String[] plugins = f.list();
		List<String> classes=new ArrayList<String>();
		String cp=System.getProperty("java.class.path");
		
		for(String plugin: plugins){
			if(plugin.endsWith(ASCommon.JAR))	{ 
				classes.addAll(installJarPlugins(plugin));
				cp+=";" + plugin;
				System.setProperty("java.class.path", cp);
				File g = new File(plugin);
				if (g.isFile()) addJar(g);
			}
		}
		System.out.println("setting classpath:  "+cp);
		System.setProperty("java.class.path", cp);
		ClassLoader classLoader= FilterManager.class.getClassLoader();
		
		
			
		if (projectType==ProjectType.SEGM  ) {
			for(String plugin: classes){
				//System.out.println("checking "+ plugin);
				Class<?>[] classesList=(classLoader.loadClass(plugin)).getInterfaces();

				for(Class<?> cs:classesList){
					// we load only IFilter classes
					//System.out.println(cs.getSimpleName());
					if (cs.getSimpleName().equals(ASCommon.IFILTER)){

						IFilter	filter =(IFilter) (classLoader.loadClass(plugin)).newInstance(); 
						String pkey=filter.getKey();
						//System.out.println(" IFilter " + pkey);
						
					
							FilterType ft=filter.getAType();
							//System.out.println(ft);
							
							if (ft==FilterType.SEGM) {

								Map<String, String> fmap=filter.getAnotatedFileds();
								annotationMap.put(pkey, fmap);
								filterMap.put(pkey, filter);
							} else if (ft==FilterType.CLASSIF) {
								Map<String, String> fmap=filter.getAnotatedFileds();
								annotationMap.put(pkey, fmap);
								//momentMap.put(pkey, filter);
							}
					
					} 

				} // end for
				
			} // end for
			
		} // end if
					 
		 	System.out.println("filter list ");
			System.out.println(filterMap);
			System.out.println("moments list ");
			System.out.println(momentMap);
			if (filterMap.isEmpty() && momentMap.isEmpty()) 
				throw new RuntimeException("filter list empty ");
			else
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
		String projectString=this.projectInfo.getProjectDirectory().get(ASCommon.IMAGESDIR);
		String filterString=this.projectInfo.getProjectDirectory().get(ASCommon.FILTERSDIR);
 
		Map<String,List<Pair<String,double[]>>> featureList= new HashMap<>();
		List<String>images= loadImages(projectString);
        Map<String,Set<String>> features= new HashMap<String,Set<String>>();
		
        for(IFilter filter: filterMap.values()){
			//System.out.println("filter applied"+filter.getName());
			if(filter.isEnabled()){
				// classification case
				// if(filter.getFilterType()==ProjectType.CLASSIF.getProjectType()){
				if (filter.getFilterType()==FilterType.CLASSIF
						&& projectType==ProjectType.CLASSIF ){
					for(String image: images) {
						for(String key: featureManager.getClassKeys()) {
							List<Roi> rois=featureManager.getExamples(key, LearningType.TRAINING_TESTING.name(), image);
							if(rois!=null && !rois.isEmpty()) {
								filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),
										filterString+image.substring(0, image.lastIndexOf(".")),
										rois);
								/*
								if(filter.getFeatures()!=null) {
									features.put(filter.getKey(),filter.getFeatureNames());
									List<Pair<String,double[]>> featureL=filter.getFeatures();
									featureList.put(filter.getKey(),featureL);
								}
								 */
							}

						}
					}
					
				} else {
					for(String image: images) {
						//IJ.log(image);
						filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),filterString+image.substring(0, image.lastIndexOf(".")), null);
					}

				}

			}

		}
		if(featureList!=null && featureList.size()>0) {
	 
			IJ.log("Features computed "+featureList.size());
			projectInfo.setFeatures(featureList);
			projectInfo.setFeatureNames(features);
 
		}

	}


	public Set<String> getAllFilters(){
		return filterMap.keySet();
	}
	
	
	public IFilter getFilter(String key){
		return filterMap.get(key);
	}

	public Map<String,String> getDefaultFilterSettings(String key){
		return filterMap.get(key).getDefaultSettings();
	}


	public boolean isFilterEnabled(String key){

		return filterMap.get(key).isEnabled();
	}


	public boolean updateFilterSettings(String key, Map<String,String> settingsMap){
		return filterMap.get(key).updateSettings(settingsMap);
	}



	private  List<String> installJarPlugins(String plugin) throws IOException {
		List<String> classNames = new ArrayList<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(plugin));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(ASCommon.DOTCLASS)) {
				String className = entry.getName().replace('/', '.'); // including ".class"
				classNames.add(className.substring(0, className.length() - ASCommon.DOTCLASS.length()));
			}
		}
		zip.close();
		return classNames;
	}


	@Override
	public boolean setDefault(String key) {
		if(filterMap.get(key).reset())
			return true;

		return false;
	}


	@Override
	public void enableFilter(String key) {
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
		for(String key: getAllFilters()){
			Map<String,String> filters = new HashMap<String,String>();
			Map<String,String> filtersetting =getDefaultFilterSettings(key);
			filters.put(ASCommon.FILTER, key);
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
			String filterName=filter.get(ASCommon.FILTER);
			System.out.println("settings: name "+filterName);
			try {
				if (!updateFilterSettings(filterName, filter))
					IJ.log("error reading settings " +filterName);
			} catch (Exception e) {
				e.printStackTrace();
				IJ.log("error reading settings " +filterName);
			}
			try {
				if (filter.get("enabled").equalsIgnoreCase("true"))
					filterMap.get(filterName).setEnabled(true);
				else
					filterMap.get(filterName).setEnabled(false);
			} catch (RuntimeException e) {
				IJ.log("error enabling " +filterName);
				e.printStackTrace();
			}
		}

	}

	@Override
	public Image getFilterImage(String key) {
		IFilter filter=filterMap.get(key);
		try {
			return ((IFilterViz) filter).getImage();
		} catch (Exception e) {
			IJ.log(key+" not an IFilterViz");
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public Map<String, String> getFieldAnnotations(String key) {
		return annotationMap.get(key);
	}


}
