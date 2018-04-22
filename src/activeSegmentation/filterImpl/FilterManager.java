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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;







import activeSegmentation.Common;
import activeSegmentation.FeatureType;
import activeSegmentation.filterImpl.ApplyZernikeFilter;
import activeSegmentation.IProjectManager;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterManager;
import activeSegmentation.ProjectType;
import activeSegmentation.io.ProjectInfo;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ijaux.scale.Pair;
import ijaux.scale.ZernikeMoment.Complex;

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
	private Map<Integer,FeatureType> featurStackMap= new HashMap<Integer, FeatureType>();
	private IProjectManager projectManager;
	private ProjectInfo projectInfo;

	private ProjectType projectType;



	public FilterManager(IProjectManager projectManager){
		super(new URL[0], IJ.class.getClassLoader());
		this.projectManager= projectManager;
		this.projectInfo=projectManager.getMetaInfo();
		projectType=ProjectType.valueOf(this.projectInfo.getProjectType());
		System.out.println(ProjectType.valueOf(this.projectInfo.getProjectType()));
		IJ.log("Loading Filters");
		//System.out.println(projectManager.getMetaInfo().getTrainingStack());
		try {
			System.out.println(this.projectInfo.getPluginPath());
			loadFilters(this.projectInfo.getPluginPath());
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IJ.log("Filters Loaded");
	}


	public  void loadFilters(String home) throws InstantiationException, IllegalAccessException, 
	IOException, ClassNotFoundException {

        System.out.println("home"+home);
		File f=new File(home);
		String[] plugins = f.list();
		List<String> classes=new ArrayList<String>();
		for(String plugin: plugins){
			//System.out.println(FilterManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			//System.out.println(plugin);
			//System.out.println(installJarPlugins(home+"/"+plugin));
			if(plugin.endsWith(Common.JAR))
			{ 
				classes.addAll(installJarPlugins(home,plugin));
				//addFile(home+"/"+plugin);
				String cp=System.getProperty("java.class.path");
				cp+=";"+ home + plugin;
				System.setProperty("java.class.path", cp);
				System.out.println("classpath:  "+cp);
				File g = new File(home,plugin);
				if (g.isFile())
					addJar(g);
			}
			else if (plugin.endsWith(Common.DOTCLASS)){
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
		for(IFilter filter: filterMap.values()){
			System.out.println("filter applied"+filter.getName());
			if(filter.isEnabled()){
				if(filter.getName().equals("Zernike Moments")){
					//BUG for CLASSASIFICATION WILL BE FIXED LATER
					ImagePlus originalImage=null;
					ArrayList<Pair<Integer,Complex>> arr=ApplyZernikeFilter.ComputeValues(originalImage, filter);
					for(Pair<Integer, Complex> pr:arr){
						FeatureType featureType;
						if(!featurStackMap.containsKey(pr.first))
							featureType = new FeatureType();
						else
							featureType = featurStackMap.get(pr.first);
						featureType.add(pr.second);
						featurStackMap.put(pr.first, featureType);
					}
				}
				else{


					List<String>images= loadImages(projectString);
					for(String image: images) {
						filter.applyFilter(new ImagePlus(projectString+image).getProcessor(),filterString+image.substring(0, image.lastIndexOf(".")));
					}

				}

			}

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

	/**
	 * Get a specific label of the reference stack
	 * @param index slice index (>=1)
	 * @return label name
	 */
	public String getLabel(int index)
	{
		return  featurStackMap.get(featurStackMap.size()).getfinalStack().getSliceLabel(index);
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

	public ImageStack getImageStack(int sliceNum)
	{
		return featurStackMap.get(sliceNum).getfinalStack();
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
		System.out.println("IN SET DEFAULT");
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
		System.out.println("meta Info"+projectInfo.toString());
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

	//

	/**
	 * Parameters of the method to add an URL to the System classes. 
	 */
//	private static final Class<?>[] parameters = new Class[]{URL.class};

	/**
	 * Adds a file to the classpath.
	 * @param s a String pointing to the file
	 * @throws IOException
	 */
/*	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}*/

	/**
	 * Adds a file to the classpath
	 * @param f the file to be added
	 * @throws IOException
	 */
/*	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}*/

	/**
	 * Adds the content pointed by the URL to the classpath.
	 * @param u the URL pointing to the content to be added
	 * @throws IOException
	 */
	/*public static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u }); 
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}        
	}*/
}
