package activeSegmentation.filter;

import java.awt.AWTEvent;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import activeSegmentation.IFilter;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;
import ijaux.scale.ZernikeMoment;
import ijaux.scale.ZernikeMoment.Complex;

public class Zernike_Filter_ implements ExtendedPlugInFilter, DialogListener, IFilter {

	final int flags=DOES_ALL+KEEP_PREVIEW+ NO_CHANGES;
	public final static String DEG="Degree";
	private int degree= Prefs.getInt(DEG, 4);
	private int position_id=-1;
	

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "ZMC";
	
	private ImagePlus img;
	
	/** It is the result stack*/
	private ImageStack imageStack;
	
	/** It stores the settings of the Filter. */
	private Map< String, String > settings= new HashMap<String, String>();
	
	private boolean isEnabled=true;

	private int nPasses=1;
	
	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Zernike Moments";
	private final int TYPE=2;
	
	ZernikeMoment zm = null;
	
	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		imageStack=new ImageStack(ip.getWidth(),ip.getHeight());

		//img.show();
	}

	@Override
	public int setup(String arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		this.img=arg1;
		return flags;
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		// TODO Auto-generated method stub
		settings.put(DEG, Integer.toString(4));
		return this.settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		// TODO Auto-generated method stub
		degree = Integer.parseInt(settingsMap.get(DEG));
		return true;
	}

	
	@Override
	public void applyFilter(ImageProcessor image, String path,List<Roi> roiList) {
		// TODO Auto-generated method stub
	
		filter(image.duplicate());
	}

	/**
	 * 
	 * This method is helper function for both applyFilter and run method
	 * @param ip input image
	 */
	private Pair<Integer,Complex> filter(ImageProcessor ip){
		int index = position_id;
		ip.snapshot();
		ip.getDefaultColorModel();
		if(zm==null){
			zm=new ZernikeMoment(degree); 
		}
		return new Pair<Integer,Complex>(index, zm.extractZernikeMoment(ip));
		        		
	}

	public int getDegree(){
		return degree;
	}
	
	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return FILTER_KEY;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return FILTER_NAME;
	}



	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return isEnabled;
	}

	@Override
	public boolean reset() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
		this.isEnabled= isEnabled;
	}


	@Override
	public boolean dialogItemChanged(GenericDialog arg0, AWTEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		this.nPasses = nPasses;
	}

	@Override
	public int showDialog(ImagePlus arg0, String arg1, PlugInFilterRunner arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFilterType() {
		// TODO Auto-generated method stub
		return this.TYPE;
	}

	@Override
	public <T> T getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}




}
