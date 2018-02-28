
import java.awt.Panel;
import java.io.File;

import javax.swing.SwingUtilities;

import activeSegmentation.IProjectManager;
import activeSegmentation.gui.CreatProject;
import activeSegmentation.io.ProjectManagerImp;
import ij.IJ;
import ij.plugin.PlugIn;

public class Active_Segmentation_ implements PlugIn {

	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
	Panel all = new Panel();
	
	private final String filesep = System.getProperty("file.separator");
	private String jarpath=filesep+"jars"+filesep;
	
	/**
	 * This method will be an entry point into the Plugin. All the
	 * dependency are inject through this class. This method is written according to 
	 * ImageJ plugin loading requirements
	 * @param parameter for imageJ
	 *
	 */
	@Override
	public void run(String arg0) {
		//new ImageJ();
		IJ.log(System.getProperty("plugins.dir"));
		String home = System.getProperty("plugins.dir")+jarpath;//+"\\plugins\\activeSegmentation\\";
		System.out.println("jars home:  "+home);
		//String home = System.getProperty("plugins.dir")+"\\plugins\\activeSegmentation\\";
		
       // System.out.println(Active_Segmentation_.class.getProtectionDomain().getCodeSource().getLocation());
		IProjectManager dataManager= new ProjectManagerImp();
		
		//System.out.println(home);
       CreatProject creatProject= new CreatProject(dataManager);
       SwingUtilities.invokeLater(creatProject);

	}


	

	public static void main(String[] args) {
		System.out.println(args[0]);
		try {
			File f=new File(args[0]);
			if (f.exists() && f.isDirectory() ) {

				System.setProperty("plugins.dir", args[0]);
				new Active_Segmentation_().run("");

			} else {
				throw new IllegalArgumentException();
			}
		} catch (Exception ex) {
			IJ.log("plugins.dir misspecified\n");
			ex.printStackTrace();
		}


	}

}