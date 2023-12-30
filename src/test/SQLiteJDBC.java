package test;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class SQLiteJDBC   {
	
	
	

	private  void addJar(File f) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		if (f.getName().endsWith(".jar")) {
			try {
				//addURL(f.toURI().toURL());
				URL url=f.toURI().toURL();
				URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(classLoader, url);
			} catch (MalformedURLException e) {
				System.out.println("ClassLoader: "+e);
			}
		}
	}
	
	private void addJarMf(String str) throws Exception {
		System.out.println(str);
		JarFile jr=new JarFile(str);
		Manifest mf =  (jr).getManifest(); //: (new JarInputStream(System.in)).getManifest();
		String classPath = mf.getMainAttributes().getValue("Class-Path");
		if (classPath != null) {
			for (String dependency : classPath.split(" ")) {
					System.out.println(dependency);
					File g = new File(dependency);
					if (g.isFile()) addJar(g);
			}
		}
		jr.close();
		
	}
	
  public static void main( String args[] ) {
      
    
     String cp= System.getProperty("java.class.path");
     System.out.println("java.class.path="+cp);
     
      /*
     SQLiteJDBC sq=new SQLiteJDBC( );

    
	try {
		if (args.length >0)
			sq.addJarMf(args[0]);
	   
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
     
	try {
		System.out.println("Trying to load JDBC driver I");
		SQLiteJDBC.class.getClassLoader().loadClass("org.sqlite.JDBC");
	} catch (ClassNotFoundException e) {
		 
		e.printStackTrace();
		 System.exit(0);
	}
      try {
    	  System.out.println("Trying to load JDBC driver II");
         Class.forName("org.sqlite.JDBC");
         Connection c =  DriverManager.getConnection("jdbc:sqlite:test.db");
      } catch ( Exception e ) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         System.exit(0);
      }
      System.out.println("Opened database successfully");
   }
}