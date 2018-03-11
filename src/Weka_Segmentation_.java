import java.awt.Panel;
import java.io.File;

import javax.swing.SwingUtilities;

import activeSegmentation.IProjectManager;
import activeSegmentation.IFilterManager;
import activeSegmentation.ILearningManager;
import activeSegmentation.feature.PixelInstanceCreator;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.feature.RoiInstanceCreator;
import activeSegmentation.filterImpl.FilterManager;
import activeSegmentation.gui.CreatProject;
import activeSegmentation.gui.Gui;
import activeSegmentation.gui.GuiController;
import activeSegmentation.io.ProjectManagerImp;
import activeSegmentation.learning.ClassifierManager;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
 

public class Weka_Segmentation_ implements PlugIn {

	private ImagePlus trainingImage;
	

	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
	Panel all = new Panel();
	public Weka_Segmentation_(){
		
	}
	
	public void getImage(){
		if (null == WindowManager.getCurrentImage())
			trainingImage= IJ.openImage();
		else
			trainingImage = WindowManager.getCurrentImage();
	}
	
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
		 new ImageJ();
		IJ.log(System.getProperty("plugins.dir"));
		String home = System.getProperty("plugins.dir")+jarpath;//+"\\plugins\\activeSegmentation\\";
		System.out.println("jars home:  "+home);	
		System.out.println(home);
		//String home = System.getProperty("plugins.dir");

		try {
	 
		 		IProjectManager dataManager= new ProjectManagerImp();
			
			//System.out.println(home);
	       CreatProject creatProject= new CreatProject(dataManager);
	       SwingUtilities.invokeLater(creatProject);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public ImagePlus getTrainingImage() {
		return trainingImage;
	}

	public void setTrainingImage(ImagePlus trainingImage) {
		this.trainingImage = trainingImage;
	}
	
	public static void main(String[] args) {
		System.out.println(args[0]);
		try {
			File f=new File(args[0]);
			if (f.exists() && f.isDirectory() ) {

				System.setProperty("plugins.dir", args[0]);
				new Weka_Segmentation_().run("");

			} else {
				throw new IllegalArgumentException();
			}
		} catch (Exception ex) {
			IJ.log("plugins.dir misspecified\n");
			ex.printStackTrace();
		}

	}

}