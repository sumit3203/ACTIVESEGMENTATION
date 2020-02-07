package activeSegmentation.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import activeSegmentation.ASCommon;
import activeSegmentation.IFilter;
import activeSegmentation.filterImpl.FilterManager;

public class ClassLoaderHelper {

	private Map<String,String> featureList ;
	private Map<String,String> filterList;
	
	
	public ClassLoaderHelper( String home) {
		featureList= new HashMap<String,String>();
		filterList= new HashMap<String,String>();
	}

	public  void loadFilters(String home) throws InstantiationException, IllegalAccessException, 
	IOException, ClassNotFoundException {

		// IN ORIGINAL WILL BE LOADED FROM PROPERTY FILE

		File f=new File(home);
		String[] plugins = f.list();
		List<String> classes=new ArrayList<String>();
		for(String plugin: plugins){
			System.out.println(plugin);
			System.out.println(installJarPlugins(home+plugin));
			if(plugin.endsWith(ASCommon.JAR))
			{ 
				classes.addAll(installJarPlugins(home+plugin));
			}
			else if (plugin.endsWith(ASCommon.DOTCLASS)){
				classes.add(plugin);
			}
			break;
		}
		ClassLoader classLoader= FilterManager.class.getClassLoader();
		for(String plugin: classes){
			System.out.println(plugin);
			Class<?>[] classesList=(classLoader.loadClass(plugin)).getInterfaces();
			for(Class<?> cs:classesList){
				if(cs.getSimpleName().equals(ASCommon.IFILTER)){
					System.out.println(cs.getSimpleName());
					IFilter	thePlugIn =(IFilter) (classLoader.loadClass(plugin)).newInstance(); 
					//filterMap.put(thePlugIn.getKey(), thePlugIn);
				}
			}

		}


	}

	private  List<String> installJarPlugins(String home) throws IOException {
		List<String> classNames = new ArrayList<String>();
		ZipInputStream zip = new ZipInputStream(new FileInputStream(home));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (!entry.isDirectory() && entry.getName().endsWith(ASCommon.DOTCLASS)) {
				String className = entry.getName().replace('/', '.'); // including ".class"
				classNames.add(className.substring(0, className.length() - ASCommon.DOTCLASS.length()));
			}
		}

		return classNames;
	}
}
