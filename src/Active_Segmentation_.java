
import java.awt.Panel;


import javax.swing.SwingUtilities;

import activeSegmentation.IProjectManager;
import activeSegmentation.IFeatureManager;
import activeSegmentation.IFilterManager;
import activeSegmentation.ILearningManager;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.feature.PixelInstanceCreator;
import activeSegmentation.feature.RoiInstanceCreator;
import activeSegmentation.filterImpl.FilterManager;
import activeSegmentation.gui.CreatProject;
import activeSegmentation.gui.GuiController;
import activeSegmentation.io.ProjectManagerImp;
import activeSegmentation.learning.ClassifierManager;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class Active_Segmentation_ implements PlugIn {




	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
	Panel all = new Panel();
	public Active_Segmentation_(){

	}

	
	/**
	 * This method will be an entry point into the Plugin. All the
	 * dependency are inject through this class. This method is written according to 
	 * ImageJ plugin loading requirements
	 * @param parameter for imageJ
	 *
	 */
	@Override
	public void run(String arg0) {
		 new ImageJ();
		IJ.log(System.getProperty("plugins.dir"));
		String home = System.getProperty("plugins.dir")+"\\plugins\\activeSegmentation\\";
		
       // System.out.println(Active_Segmentation_.class.getProtectionDomain().getCodeSource().getLocation());
		IProjectManager dataManager= new ProjectManagerImp();
		
	
		
		
		//ILearningManager  learningManager= new ClassifierManager(dataManager);
		
		//System.out.println(home);
       CreatProject creatProject= new CreatProject(dataManager);
       SwingUtilities.invokeLater(creatProject);

	}


	

	public static void main(String[] args) {
		// new ij.ImageJ();

		new Active_Segmentation_().run("");
	}

}