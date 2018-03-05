package activeSegmentation.filterImpl;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ijaux.scale.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import activeSegmentation.IFilter;

/**
 * 				
 *   
 * 
 * @author Mukesh Gupta, Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * ApplyFilter is responsible for apply filters on each slice of imageStack in parallel 
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
public class ApplyFilter extends RecursiveTask<Pair<Integer,ImageStack>>{

	ImageProcessor imp;
	private IFilter filter;
	int index=0;
	
	public ApplyFilter(IFilter filter, ImageProcessor imp, int index){
		this.imp=imp;
		this.filter=filter;
		this.index=index;
	}
	
	@Override
	protected Pair<Integer,ImageStack> compute() {
		// TODO Auto-generated method stub
		//filter.updatePosition(index);
		//return filter.applyFilter(imp);
		return null;
	}
	
	/**
	 * 
	 * @param originalImage
	 * @param filter
	 * @return ArrayList of imageStack getting after applying filter on each slice
 	 */
	public  static ArrayList<Pair<Integer,ImageStack>> ComputeFeatures(ImagePlus originalImage, IFilter filter) {
		ArrayList<Pair<Integer,ImageStack>> arr= new ArrayList<Pair<Integer,ImageStack>>();
    	List<ApplyFilter> tasks = new ArrayList<ApplyFilter>();
		
		long prevTime=System.currentTimeMillis();		
    	for(int i=1; i<=originalImage.getStackSize(); i++){
			ApplyFilter ezm =new ApplyFilter(filter, originalImage.getImageStack().getProcessor(i), i);
            tasks.add(ezm);
			ezm.fork();
		}
		
		if (tasks.size() > 0) {
			for (ApplyFilter task : tasks) {
                Pair<Integer,ImageStack> pr=task.join();
        		arr.add(pr);
            }
		}
		long currTime=System.currentTimeMillis();
		System.out.println("Time Taken after applying filter on each slice= "+(currTime-prevTime));
		return arr;
	}

}
