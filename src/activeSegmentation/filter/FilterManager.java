package activeSegmentation.filter;


import activeSegmentation.*;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import ij.IJ;
import ij.ImagePlus;
import ijaux.datatype.Pair;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra, ZIB and Dimiter Prodanov, IMEC
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
public class FilterManager extends URLClassLoader implements IFilterManager, IUtil {

	private Map<String, IFilter> filterMap= new HashMap<>();
	private ProjectManager projectManager;
	private ProjectInfo projectInfo;
	private ProjectType projectType;

	/**
	 * 
	 * @param projectManager
	 * @param featureManager
	 */
	public FilterManager(ProjectManager projectManager, FeatureManager  featureManager){
		super(new URL[0], IJ.class.getClassLoader());

		this.projectManager= projectManager;
		this.projectInfo=projectManager.getMetaInfo();
		this.projectType=this.projectInfo.getProjectType();


		System.out.println("Project Type: "+projectType +" pt ");
		IJ.log("Loading Filters");

		try {
			List<String> jars=projectInfo.getPluginPath();
			System.out.println("filter plugin path: "+jars);
			if (jars!=null)
				loadFilters(jars);
			IJ.log("Filters loaded");
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException e) {
			e.printStackTrace();
			IJ.log("Filters NOT loaded. Check pluginPath variable");
		}
 
	}

	/**
	 * @param plugins
	 */
	@Override
	public  void loadFilters(List<String> plugins) throws 
	InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {

		//System.out.println("home: "+home);
		List<String> classes=new ArrayList<>();
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

		
		for(String plugin: classes){
			//System.out.println("checking "+ plugin);
			//try {
				Class<?>[] classesList=(classLoader.loadClass(plugin)).getInterfaces();
				final boolean isInterface=classLoader.loadClass(plugin).isInterface();
				
				for(Class<?> cs:classesList){
					// we load only IFilter classes
					//System.out.println(cs.getSimpleName());
					
					if (cs.getSimpleName().equals(ASCommon.IFILTER) && !isInterface){

						//IAnnotated	ianno =(IAnnotated) (classLoader.loadClass(plugin)).newInstance(); 
						IAnnotated	ianno = inewInstance(plugin); //  (IAnnotated) (classLoader.loadClass(plugin)).newInstance(); 
						Pair<String, String> p=ianno.getKeyVal();
						String pkey=p.first;
						System.out.println(" IFilter " + pkey);
						
						FilterType ft=ianno.getAType();
						//System.out.println(ft);
						if (projectType==ProjectType.SEGM  ) {
							if (ft==FilterType.SEGM) {
								IFilter	filter =(IFilter) ianno;
								Map<String, String> fmap=filter.getAnotatedFileds();
								annotationMap.put(pkey, fmap);
								filterMap.put(pkey, filter);
							} // end if
	
						} // end if
						
	
					} // end if
	
				} // end for
			//} catch (  ClassNotFoundException ex) {
			//	System.out.println("error:" + plugin +" not found");
			//}

		} // end for
		
		System.out.println("filter list ");
		System.out.println(filterMap);

		if (filterMap.isEmpty()) 
			throw new RuntimeException("filter list empty ");
		else
			setFiltersMetaData();

	}

	private void addJar(File f) {
		if (f.getName().endsWith(".jar")) {
			try {
				addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
				System.out.println("PluginClassLoader: "+e);
			}
		}
	}


	@Override
	public void applyFilters(ProgressCallback callback) throws InterruptedException {
		String projectString=projectInfo.getProjectDirectory().get(ASCommon.K_IMAGESDIR);
		String filterString=projectInfo.getProjectDirectory().get(ASCommon.K_FILTERSDIR);

		Map<String,List<Pair<String,double[]>>> featureList= new HashMap<>();
		Map<String,Set<String>> features= new HashMap<>();
			
		List<String>images= loadImages(projectString, false);

		int totalSteps = images.size() * filterMap.size();
		int step = 0;

		for(IFilter filter: filterMap.values()){
			System.out.println("FeatureManager: filter applied "+filter.getName());

			//check interruption
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Computation was canceled.");
			}

			if(filter.isEnabled()){
				for(String image: images) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException("Computation was canceled.");
					}

					//IJ.log(image);
					filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),filterString+image.substring(0, image.lastIndexOf(".")), null);

					// Update progress
					step++;
					if (callback != null) {
						callback.onProgress(step * 100 / totalSteps);
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


	@Override
	public void applyFilters() throws InterruptedException {
		applyFilters(null);
	}

	
	@Override
	public Set<String> getAllFilters(){
		return filterMap.keySet();
	}


	@Override
	public Map<String,String> getDefaultFilterSettings(String key){
		return filterMap.get(key).getDefaultSettings();
	}


	@Override
	public boolean isFilterEnabled(String key){
		return filterMap.get(key).isEnabled();
	}


	@Override
	public boolean updateFilterSettings(String key, Map<String,String> settingsMap){
		try {
			IFilter filter=filterMap.get(key);
			return filter.updateSettings(settingsMap);
		} catch (NumberFormatException ex) {
			System.out.println("Exception for filter: " + key);
			ex.printStackTrace();
			return false;
		}
	}



	private  List<String> installJarPlugins(String plugin) throws IOException {
		List<String> classNames = new ArrayList<>();
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
		}else{
			filterMap.get(key).setEnabled(true);	
		}
	}


	@Override
	public void saveFiltersMetaData(){	
		projectInfo= projectManager.getMetaInfo();
		System.out.println("meta Info"+projectInfo.toString());
		List<Map<String,String>> filterObj= new ArrayList<>();
		for(String key: getAllFilters()){
			Map<String,String> filters = new HashMap<>();
			Map<String,String> filtersetting =getDefaultFilterSettings(key);
			filters.put(ASCommon.FILTER, key);
			for(String setting: filtersetting.keySet()){
				filters.put(setting, filtersetting.get(setting));		
			}
			filters.put("enabled", "false" );
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
				IFilter instance=getInstance(filterName);
				if (filter.get("enabled").equalsIgnoreCase("true"))
					instance.setEnabled(true);
				else
					instance.setEnabled(false);
			} catch (RuntimeException e) {
				IJ.log("error enabling " +filterName);
				e.printStackTrace();
			}
		}

	}

	@Override
	public Image getFilterImage(String key) {
		IFilter filter=getInstance(key);
		try {
			return ((IFilterViz) filter).getImage();
		} catch (Exception e) {
			IJ.log(key+" not an IFilterViz");
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public IFilter getInstance(String key) {
		return filterMap.get(key);
	}
	
	
	@Override
	public String getHelpInfo(String key) {
		IFilter instance=filterMap.get(key);
		String url=instance.getHelpResource();
		IJ.log("help url: "+url);
		String path=projectInfo.helpURL;
		IJ.log("help path: "+path);
		return url;
	}
}
