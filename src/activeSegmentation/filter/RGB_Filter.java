package activeSegmentation.filter;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import activeSegmentation.IFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.TypeConverter;

/*
 *  What is the function of this? - please clarify
 *  for the time being I will exclude it.
 */


public class RGB_Filter implements IFilter {

	/** A string key identifying this factory. */
	private final  String FILTER_KEY = "Basic";

	/** The pretty name of the target detector. */
	private final String FILTER_NAME = "Basic";
	
	// segmentation
	private final int TYPE=1;
	private boolean isEnabled=true;
	private Map< String, String > settings= new HashMap<String, String>();
	public  String SIGMA="LOG_sigma", LEN="G_len",MAX_LEN="G_MAX", ISSEP="G_SEP", SCNORM="G_SCNORM";

	private  int sz= Prefs.getInt(LEN, 2);
	private  int max_sz= Prefs.getInt(MAX_LEN, 8);
	
	
	
	public static void main(String[] args) {
		RGB_Filter filter= new RGB_Filter();
		
		IJ.run("Blobs (25K)");
		ImagePlus currentImage=IJ.getImage();
		// TODO Auto-generated method stub
	
		String filterPath="C:\\Users\\sumit\\Documents\\SEZEG\\preprocessed1\\";
	//	ImagePlus currentImage=IJ.openImage("C:\\Users\\sumit\\Documents\\SEZEG\\preprocessed1\\testpanel-01.tif");
		filter.applyFilter(currentImage.getProcessor(), filterPath);

	}

	public void applyFilter(ImageProcessor image, String filterPath) {

		int [] redImage= new int[image.getWidth()*image.getHeight()];
		int [] blueImage= new int[image.getWidth()*image.getHeight()];
		int [] greenImage= new int[image.getWidth()*image.getHeight()];
		float [] hImage= new float[image.getWidth()*image.getHeight()];
		float [] sImage= new float[image.getWidth()*image.getHeight()];
		float [] vImage= new float[image.getWidth()*image.getHeight()];
		// 1D iteration will do fine here - to change
		for(int y=0; y<image.getHeight();y++) {

			for(int x=0; x<image.getWidth();x++) {

				int pixel=image.get(x, y);
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;
				float[] hsv = new float[3];
				Color.RGBtoHSB(red,green,blue,hsv);
				redImage[y*image.getWidth()+x]=red;
				blueImage[y*image.getWidth()+x]=blue;
				greenImage[y*image.getWidth()+x]=green;		   
				hImage[y*image.getWidth()+x]=hsv[0];
				sImage[y*image.getWidth()+x]=hsv[1];
				vImage[y*image.getWidth()+x]=hsv[2];

			}
		}

		ImageProcessor redProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), redImage);	
		ImageProcessor greenProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), greenImage);	
		ImageProcessor blueProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), blueImage);	
		ImageProcessor hProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), hImage);	
		ImageProcessor sProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), sImage);	
		ImageProcessor vProcessor = new FloatProcessor(image.getWidth(),
				image.getHeight(), vImage);	
		System.out.println(filterPath);
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "R", redProcessor),filterPath+"/"+FILTER_KEY+"_"+"R"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "G", greenProcessor),filterPath+"/"+FILTER_KEY+"_"+"G"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "B", blueProcessor),filterPath+"/"+FILTER_KEY+"_"+"B"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "H", hProcessor),filterPath+"/"+FILTER_KEY+"_"+"H"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "S", sProcessor),filterPath+"/"+FILTER_KEY+"_"+"S"+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", vProcessor),filterPath+"/"+FILTER_KEY+"_"+"V"+".tif" );

	}

	@Override
	public String getKey() {
		return this.FILTER_KEY;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.FILTER_NAME;
	}


	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return isEnabled;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
		this.isEnabled= isEnabled;
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



	@Override
	public Set<String> getFeatureNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getDefaultSettings() {
		// TODO Auto-generated method stub
		settings.put(LEN, Integer.toString(sz));
		settings.put(MAX_LEN, Integer.toString(max_sz));
		return this.settings;
	}

	@Override
	public boolean updateSettings(Map<String, String> settingsMap) {
		// TODO Auto-generated method stub
		sz=Integer.parseInt(settingsMap.get(LEN));
		max_sz=Integer.parseInt(settingsMap.get(MAX_LEN));
		return true;
	}

	@Override
	public void applyFilter(ImageProcessor image, String path, List<Roi> roiList) {
		// TODO Auto-generated method stub
		//applyFilter(image, path);
		for (int sigma=sz; sigma<= max_sz; sigma *=2){	
			addMean(image, path, sigma);
			addMax(image, path, sigma);
			addMedian(image, path, sigma);
			addVariance(image, path, sigma);
			//addSobel(image, path, sigma);
			addGaussianBlur(image, path, sigma);
			addDoG(image, path,sigma);


		}
		//addKuwhara(image, path, 19);
		//addlipschitz(image, path);
	}


	public void addDoG(ImageProcessor image, String path, int sigma1)
	{
		for (int j=1; j<sz; j*=2)
		{
			GaussianBlur gs = new GaussianBlur();

			// Get channel(s) to process

			ImageProcessor ip_1 = image.duplicate();
			//gs.blur(ip_1, sigma1);
			gs.blurGaussian(ip_1, 0.4 * sigma1, 0.4 * sigma1,  0.0002);
			ImageProcessor ip_2 =image.duplicate();			
			//gs.blur(ip_2, sigma2);
			gs.blurGaussian(ip_2, 0.4 * j, 0.4 * j,  0.0002);

			ImageProcessor ip = new FloatProcessor(image.getWidth(), image.getHeight());

			for (int x=0; x<image.getWidth(); x++){
				for (int y=0; y<image.getHeight(); y++){
					float v1 = ip_1.getf(x,y);
					float v2 = ip_2.getf(x,y);
					ip.setf(x,y, v2-v1);
				}
			}
			IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip),path+"/"+FILTER_KEY+"_"+"dog_"+sigma1+"_"+j+".tif" );
		}

	}
	

	private void addGaussianBlur(ImageProcessor image, String path, float sigma) {
		final ImageProcessor ip = image.duplicate();
		GaussianBlur gs = new GaussianBlur();
		//gs.blur(ip, sigma);
		gs.blurGaussian(ip, 0.4 * sigma, 0.4 * sigma,  0.0002);	
		ImageProcessor ip1=ip.convertToFloat();
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "VG", ip1),path+"/"+FILTER_KEY+"_"+"gaus_"+sigma+".tif" );

	}
	private void addMean(ImageProcessor image, String path, float radius) {
		final ImageProcessor ip = image.duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.MEAN);
		ImageProcessor ip1=ip.convertToFloat();
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip1),path+"/"+FILTER_KEY+"_"+"min_"+radius+".tif" );
	}

	private void addMax(ImageProcessor image, String path, float radius) {
		final ImageProcessor ip = image.duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.MAX);
		ImageProcessor ip1=ip.convertToFloat();
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip1),path+"/"+FILTER_KEY+"_"+"max_"+radius+".tif" );
	}

	private void addMedian(ImageProcessor image, String path, float radius) {
		final ImageProcessor ip = image.duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.MEDIAN);
		ImageProcessor ip1=ip.convertToFloat();
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip1),path+"/"+FILTER_KEY+"_"+"median_"+radius+".tif" );
	}

	private void addVariance(ImageProcessor image, String path, float radius) {
		final ImageProcessor ip = image.duplicate();
		final RankFilters filter = new RankFilters();
		filter.rank(ip, radius, RankFilters.VARIANCE);
		ImageProcessor ip1=ip.convertToFloat();
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip1),path+"/"+FILTER_KEY+"_"+"variance_"+radius+".tif" );
	}

	

	private void addSobel(ImageProcessor image, String path, int radius) {
		Convolver c = new Convolver();
		float[] sobelFilter_y = {1f,0f,-1f,2f,0f,-2f,1f,0f,-1f};
		final ImageProcessor ip = image.duplicate().toFloat(0, null);
		c.convolveFloat(ip, sobelFilter_y, 3, 3);

		ImageProcessor fp = new FloatProcessor(image.getWidth(), image.getHeight());

		for (int x=0; x<image.getWidth(); x++){
			for (int y=0; y<image.getHeight(); y++){
				float s_x = ip.getf(x,y);
				float s_y = ip.getf(x,y);
				fp.setf(x,y, (float) Math.sqrt(s_x*s_x + s_y*s_y));
			}
		}
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", fp),path+"/"+FILTER_KEY+"_"+"Sobel"+radius+".tif" );
	}


	/** Helper method to addHessian and getHessian */
	private void calculateHessianOnChannel(ImageProcessor channel, float sigma, String path)
	{
		float[] sobelFilter_x = {1f,2f,1f,0f,0f,0f,-1f,-2f,-1f};
		float[] sobelFilter_y = {1f,0f,-1f,2f,0f,-2f,1f,0f,-1f};

		Convolver c = new Convolver();
		GaussianBlur gs = new GaussianBlur();

		int width = channel.getWidth();
		int height = channel.getHeight();

		ImageProcessor ip_x = channel.duplicate().convertToFloat();
		gs.blurGaussian(ip_x, 0.4 * sigma, 0.4 * sigma,  0.0002);
		c.convolveFloat(ip_x, sobelFilter_x, 3, 3);

		ImageProcessor ip_y = channel.duplicate().convertToFloat();
		gs.blurGaussian(ip_y, 0.4 * sigma, 0.4 * sigma,  0.0002);
		c.convolveFloat(ip_y, sobelFilter_y, 3, 3);

		ImageProcessor ip_xx = ip_x.duplicate();
		c.convolveFloat(ip_xx, sobelFilter_x, 3, 3);

		ImageProcessor ip_xy = ip_x.duplicate();
		c.convolveFloat(ip_xy, sobelFilter_y, 3, 3);

		ImageProcessor ip_yy = ip_y.duplicate();
		c.convolveFloat(ip_yy, sobelFilter_y, 3, 3);

		ImageProcessor ip = new FloatProcessor(width, height);
		ImageProcessor ipTr = new FloatProcessor(width, height);
		ImageProcessor ipDet = new FloatProcessor(width, height);
		//ImageProcessor ipRatio = new FloatProcessor(width, height);
		ImageProcessor ipEig1 = new FloatProcessor(width, height);
		ImageProcessor ipEig2 = new FloatProcessor(width, height);
		ImageProcessor ipOri = new FloatProcessor(width, height);
		ImageProcessor ipSed = new FloatProcessor(width, height);
		ImageProcessor ipNed = new FloatProcessor(width, height);

		final double t = Math.pow(1, 0.75);

		for (int x=0; x<width; x++){
			for (int y=0; y<height; y++)
			{
				// a
				float s_xx = ip_xx.getf(x,y);
				// b, c
				float s_xy = ip_xy.getf(x,y);
				// d
				float s_yy = ip_yy.getf(x,y);
				// Hessian module: sqrt (a^2 + b*c + d^2)
				ip.setf(x,y, (float) Math.sqrt(s_xx*s_xx + s_xy*s_xy+ s_yy*s_yy));
				// Trace: a + d
				final float trace = s_xx + s_yy;
				ipTr.setf(x,y,  trace);
				// Determinant: a*d - c*b
				final float determinant = s_xx*s_yy-s_xy*s_xy;
				ipDet.setf(x,y, determinant);

				// Ratio
				//ipRatio.setf(x,y, (float)(trace*trace) / determinant);
				// First eigenvalue: (a + d) / 2 + sqrt( ( 4*b^2 + (a - d)^2) / 2 )
				ipEig1.setf(x,y, (float) ( trace/2.0 + Math.sqrt((4*s_xy*s_xy + (s_xx - s_yy)*(s_xx - s_yy)) / 2.0 ) ) );
				// Second eigenvalue: (a + d) / 2 - sqrt( ( 4*b^2 + (a - d)^2) / 2 )
				ipEig2.setf(x,y, (float) ( trace/2.0 - Math.sqrt((4*s_xy*s_xy + (s_xx - s_yy)*(s_xx - s_yy)) / 2.0 ) ) );
				// Orientation
				if (s_xy < 0.0) // -0.5 * acos( (a-d) / sqrt( 4*b^2 + (a - d)^2)) )
				{
					float orientation =(float)( -0.5 * Math.acos((s_xx	- s_yy)
							/ Math.sqrt(4.0 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) ));
					if (Float.isNaN(orientation))
						orientation = 0;
					ipOri.setf(x, y,  orientation);
				}
				else 	// 0.5 * acos( (a-d) / sqrt( 4*b^2 + (a - d)^2)) )
				{
					float orientation =(float)( 0.5 * Math.acos((s_xx	- s_yy)
							/ Math.sqrt(4.0 * s_xy * s_xy + (s_xx - s_yy) * (s_xx - s_yy)) ));
					if (Float.isNaN(orientation))
						orientation = 0;
					ipOri.setf(x, y,  orientation);
				}
				// Gamma-normalized square eigenvalue difference
				ipSed.setf(x, y, (float) ( Math.pow(t,4) * trace*trace * ( (s_xx - s_yy)*(s_xx - s_yy) + 4*s_xy*s_xy ) ) );
				// Square of Gamma-normalized eigenvalue difference
				ipNed.setf(x, y, (float) ( Math.pow(t,2) * ( (s_xx - s_yy)*(s_xx - s_yy) + 4*s_xy*s_xy ) ) );
			}
		}

		ImageStack hessianStack = new ImageStack(width, height);
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "V", ip),path+"/"+FILTER_KEY+"_"+"H"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "TR", ipTr),path+"/"+FILTER_KEY+"_"+"HTR"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "DET", ipDet),path+"/"+FILTER_KEY+"_"+"HDET"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "EIG1", ipEig1),path+"/"+FILTER_KEY+"_"+"H_Eigenvalue_1_"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "EIG2", ipEig2),path+"/"+FILTER_KEY+"_"+"H_Eigenvalue_2_"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "OIR", ipOri),path+"/"+FILTER_KEY+"_"+"Orientation"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "OIR", ipSed),path+"/"+FILTER_KEY+"_"+"_Square_ED_"+sigma+".tif" );
		IJ.save(new ImagePlus(FILTER_KEY+"_" + "OIR", ipNed),path+"/"+FILTER_KEY+"_"+"_Normal_ED_"+sigma+".tif" );

	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reset() {
		// TODO Auto-generated method stub
		sz= Prefs.getInt(LEN, 2);
		max_sz= Prefs.getInt(MAX_LEN, 8);
		return true;
	}

}
