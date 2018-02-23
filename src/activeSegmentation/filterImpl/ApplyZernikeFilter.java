package activeSegmentation.filterImpl;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import activeSegmentation.IFilter;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;
import ijaux.scale.ZernikeMoment.Complex;

/**
 * 				
 *   
 * 
 * @author Mukesh Gupta, Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * ApplyZernikeFilter is responsible for apply Zernike Polynomial Filter on each slice of imageStack in parallel 
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
public class ApplyZernikeFilter extends RecursiveTask<Pair<Integer,Complex>>{
	ImageProcessor imp;
	private IFilter filter;
	private int index;
	
	public ApplyZernikeFilter(IFilter filter, ImageProcessor imp, int index){
		this.imp=imp;
		this.filter=filter;
		this.index=index;
	}
	
	@Override
	protected Pair<Integer,Complex> compute() {
		//Update index of a given image 
		filter.updatePosition(index);
		return filter.applyFilter(imp);
	}
	
	/**
	 * 
	 * @param originalImage
	 * @param filter
	 * @return ArrayList of Zernike Complex Values getting after applying filter on each slice
	 */
	public static ArrayList<Pair<Integer,Complex>> ComputeValues(ImagePlus originalImage, IFilter filter) {
    	ArrayList<Pair<Integer,Complex>> arr= new ArrayList<Pair<Integer,Complex>>();    	
    	synchronized(filter) {
    		filter.updatePosition(1);
    		Pair<Integer,Complex> rv = filter.applyFilter(originalImage.getImageStack().getProcessor(1));
    		arr.add(rv);
    		filter.notifyAll();
    	}
		long prevTime=System.currentTimeMillis();		
		List<ApplyZernikeFilter> tasks = new ArrayList<>();
		for(int i=2; i<originalImage.getStackSize(); i++){
			ApplyZernikeFilter ezm =new ApplyZernikeFilter(filter, originalImage.getImageStack().getProcessor(i),i);
            tasks.add(ezm);
			ezm.fork();
		}
		if (tasks.size() > 0) {
			for (ApplyZernikeFilter task : tasks) {
				Pair<Integer,Complex> rv=task.join();
        		arr.add(rv);
            }
		}
		long currTime=System.currentTimeMillis();
		System.out.println("Time Taken after applying Zernike filter on each slice= "+(currTime-prevTime));
		return arr;
	}

}