





import javax.swing.SwingUtilities;

import activeSegmentation.IProjectManager;
import activeSegmentation.gui.CreatProject;
import activeSegmentation.io.ProjectManagerImp;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class Active_Segmentation_ implements PlugIn {




	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
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
		IProjectManager dataManager= new ProjectManagerImp();

		CreatProject creatProject= new CreatProject(dataManager);
		SwingUtilities.invokeLater(creatProject);

	}




	public static void main(String[] args) {
		new ImageJ();
		new Active_Segmentation_().run("");
	}

}