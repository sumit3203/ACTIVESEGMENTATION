package test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

//import activeSegmentation.IProjectManager;
import activeSegmentation.learning.SMO;
import activeSegmentation.prj.ProjectManager;
import weka.core.Instance;
import weka.core.Instances;

public class TestCurves
{
  public static void main(String[] args)  {
    ProjectManager dataManager = new ProjectManager();
    // to change
    Instances trainingSet = dataManager.readDataFromARFF("C:\\Users\\sumit\\Documents\\GitHub\\ACTIVESEGMENTATION\\src\\activeSegmentation\\evaluation\\iris.arff").getDataset();
    SMO smo = new SMO();
    trainingSet.randomize(new Random(10L));
    int trainSize = (int)Math.round(trainingSet.numInstances() * 0.8D);
    int testSize = trainingSet.numInstances() - trainSize;
    Instances train = new Instances(trainingSet, 0, trainSize);
    Instances test = new Instances(trainingSet, trainSize, testSize);
    try {
		smo.buildClassifier(train);
		double[] scoreC = new double[test.numInstances()];
		double[] labels = new double[test.numInstances()];
		int j = 0;
		for (Instance i : test)   {
		  double[] score = smo.distributionForInstance(i);
		  scoreC[j] = score[0];
		  labels[j] = i.classValue();
		  j++;
		}
		System.out.println("ORIGINAL LABEL");
		for (int i = 0; i < labels.length; i++) {
		  System.out.print(labels[i] + " ");
		}
		System.out.println();
		System.out.println("ORIGINAL SCORE");
		for (int i = 0; i < labels.length; i++) {
		  System.out.print(scoreC[i] + " ");
		}
		System.out.println();
		int[] indexes = argsort(scoreC, true);
		Arrays.sort(scoreC);
		double[] sortLabels = new double[labels.length];
		for (int i = 0; i < indexes.length; i++) {
		  sortLabels[i] = labels[indexes[i]];
		}
		System.out.println("SORTED SCORE");
		for (int i = 0; i < labels.length; i++) {
		  System.out.print(scoreC[i] + " ");
		}
		System.out.println();
		System.out.println("SORTED LABEL");
		for (int i = 0; i < labels.length; i++) {
		  System.out.print(sortLabels[i] + " ");
		}
		int n = sortLabels.length;
		double[] score = new double[test.numInstances()];
		for (int i = 0; i < n; i++) {
		  if (i < n - 1) {
		    sortLabels[i] -= sortLabels[(i + 1)];
		  } else {
		    score[i] = sortLabels[(n - 1)];
		  }
		}
		System.out.println();
		for (int i = 0; i < score.length; i++) {}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public static int[] argsort(final double[] a,final boolean  ascending)
  {
    Integer[] indexes = new Integer[a.length];
    for (int i = 0; i < indexes.length; i++) {
      indexes[i] = Integer.valueOf(i);
    }
    Arrays.sort(indexes, new Comparator<Integer>() {
        @Override
        public int compare(final Integer i1, final Integer i2) {
            return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
        }
    });
    int[] ret = new int[indexes.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = indexes[i].intValue();
    }
    return ret;
  }
  
  private double evaluate(int TP, int FP, int TN, int FN)
  {
    double i = 0.0D;
    
    return i;
  }
}
