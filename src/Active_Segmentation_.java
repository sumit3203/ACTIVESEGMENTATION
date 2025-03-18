import java.io.File;

import javax.swing.SwingUtilities;

import activeSegmentation.ASCommon;
import activeSegmentation.gui.CreateOpenProjectGUI;
import activeSegmentation.prj.ProjectManager;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;


public class Active_Segmentation_ implements PlugIn, ASCommon {

	/**
	 * 
	 */
	public Active_Segmentation_(){

	}


	/**
	 * This method is the main entry point into the plugin. 
	 * This method is written according to 
	 * ImageJ plugin loading requirements
	 * @param parameter for imageJ
	 *
	 */
	@Override
	public void run(String arg0) {
	
		System.out.println( System.getProperty("java.classpath"));
		 
		ProjectManager dataManager= new ProjectManager();
		CreateOpenProjectGUI creatProject= new CreateOpenProjectGUI(dataManager);
		SwingUtilities.invokeLater(creatProject);
		IJ.log(arg0);
		IJ.log("AS version "+version);

	}



	/**
	 * Stand alone operation
	 * @param args
	 */
	public static void main(String[] args) {
		 try {
			// Check if args are provided
			if (args.length > 0) {
				File f = new File(args[0]);
				if (f.exists() && f.isDirectory() ) {
					System.setProperty("plugins.dir", args[0]);
					new ImageJ();
					Active_Segmentation_ as = new Active_Segmentation_();
					as.run("");
				} else {
					throw new IllegalArgumentException("Invalid plugins directory");
				}
			} else {
				// Run with default ImageJ plugins directory
				System.out.println("No plugins directory specified, using default location");
				new ImageJ();
				Active_Segmentation_ as = new Active_Segmentation_();
				as.run("");
			}
		} catch (Exception ex) {
			IJ.error("Error running plugin. \n"
					+ "To run the platform in stand-alone mode please specify plugins.dir"
					+ " as a command line argument");
			ex.printStackTrace();
		} 
	}
}