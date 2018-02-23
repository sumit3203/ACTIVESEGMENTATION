package activeSegmentation.featureSelection;

import java.util.ArrayList;
import java.util.List;

import activeSegmentation.IFilter;
import activeSegmentation.filter.Gaussian_Derivative_;
import activeSegmentation.filter.LoG_Filter_;
import activeSegmentation.filterImpl.ApplyFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ijaux.scale.Pair;

public class ImageClassfication {
	//private ImagePlus trainingImage;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<IFilter> filterList= new ArrayList<IFilter>();
		filterList.add(new LoG_Filter_());
		// filter.add(new Gaussian_Derivative_());	
		ImageClassfication imageClassfication= new ImageClassfication();
		ImagePlus trainingImage=imageClassfication.getImage();
		ArrayList<Pair<Integer, ImageStack>> arr=(ArrayList<Pair<Integer, ImageStack>>) ApplyFilter
				.ComputeFeatures(trainingImage, new LoG_Filter_());
		System.out.println(arr.size());
		for(Pair<Integer,ImageStack> pr:arr){
			System.out.println(pr.first);
			System.out.println(pr.second.getSize());
			
		}

	}


	public ImagePlus getImage(){
		//WindowManager.getImage("D:/DATASET/competition/Training_set/healthy.tiff")
		ImagePlus trainingImage;
		trainingImage= IJ.openImage();


		System.out.println(trainingImage.getStackSize());

		return trainingImage;
	}
}
