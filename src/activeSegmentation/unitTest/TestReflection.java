package activeSegmentation.unitTest;

import java.io.File;
import java.util.ServiceLoader;

import activeSegmentation.IFilter;

public class TestReflection {

	public static void main(String args[]) {
		File folder = new File("C:/Program Files/ImageJ/plugins/activeSegmentation/");
		File[] listOfFiles = folder.listFiles();
		for(File file: listOfFiles){
			System.out.println(file.getName());
		}
		System.out.println("-----------");
		String[] files= folder.list();
		for(String plugin: files){
			System.out.println(plugin);
		}
	}
}
