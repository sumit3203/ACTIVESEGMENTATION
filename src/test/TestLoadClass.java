package test;

import java.lang.reflect.*;


public class TestLoadClass<E> {

	public static void main(String[] args) {
		
		TestLoadClass<String> loader= new TestLoadClass<>();
		String s=  loader.loadClass("java.lang.String" );
		s=s+" test";
		System.out.println(s);
	}
	
	 /**
	   * 
	   * @param cname
	 * @return 
	   * @return
	   */
	  public  E loadClass (String cname ) {
		  try {
			    // Assuming cname is the fully qualified class name
			    Class<?> clazz = Class.forName(cname);
			    
			    // Use getDeclaredConstructor() to obtain the constructor
			    Constructor<?> constructor = clazz.getDeclaredConstructor();
			    
			    // Ensure that the constructor is accessible, especially if it's private
			    constructor.setAccessible(true);
			    
			    // Use newInstance() to create an instance
			    @SuppressWarnings("unchecked")
				E instance = (E) constructor.newInstance();
			    
			    return instance;
			    // Now you have an instance of the class
			} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			     e.printStackTrace();
			}
		  return null;
	  }

}
