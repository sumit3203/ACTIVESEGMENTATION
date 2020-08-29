package test;

/**
 * ClassLoaderLoadClass.java
 * Copyright (c) 2010, HerongYang.com, All Rights Reserved.
 */
class ClassLoaderTest{
   public static void main(String[] a) {
      java.io.PrintStream out = System.out;
      Object o = null;
      Class<?> c = null;
      ClassLoader l = null;

 
      o=new ClassLoaderTest();
      
      out.println("");
      out.println("Loading with Class.forName() method...");
      try {
         c = Class.forName("test.ClassLoaderTest");
      } catch (Exception e) {
         e.printStackTrace();
      }

      out.println("");
      out.println("Loading with ClassLoader.loadClass() method...");
      l = ClassLoader.getSystemClassLoader();
      try {
         c = l.loadClass("test.ClassLoaderTest");
      } catch (Exception e) {
         e.printStackTrace();
      }

     
      
      String filesep = System.getProperty("file.separator");
      String jarpath=filesep+"jars" +filesep;
      System.setProperty("plugins.dir","C:\\Applications\\ImageJ\\plugins");

      String home = System.getProperty("plugins.dir")+jarpath; 
      System.out.println("jars home:  "+home);
      String cp=System.getProperty("java.class.path");
      cp+=";"+ home + "act_segm_filters.jar";
      System.setProperty("java.class.path", cp);
      System.out.println("classpath:  "+cp);
		
		 out.println("");
	      out.println("Loading the  class");
	      l = ClassLoader.getSystemClassLoader();
	      try {
	         c = l.loadClass("activeSegmentation.filter.LoG_Filter_");
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		
		
		
   }
}