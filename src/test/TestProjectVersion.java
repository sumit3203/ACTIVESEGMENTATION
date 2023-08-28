package test;

import activeSegmentation.prj.ProjectInfo; 

public class TestProjectVersion {

	public static void main(String[] args)  {
		ProjectInfo prj=new ProjectInfo();
		
		System.out.println(prj.getVersion());
		
		System.out.println("1.0.8 "+prj.lesserVersion("1.0.8"));
		
		System.out.println("1.0.8 "+prj.greaterVersion("1.0.8"));
		
		System.out.println("1.0.7 "+prj.greaterVersion("1.0.7"));
		
		System.out.println("1.0.6 "+prj.greaterVersion("1.0.6"));
	}

	
}
