package activeSegmentation;

import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import java.awt.Image;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ijaux.scale.SUtils;

/*
 * interface for filter visualization;
 */
public interface IFilterViz {

	/**
	 *  returns the plot of the filter kernel
	 * @return Image
	 */
	public default Image getImage(){
		final XYSeries series = new XYSeries("Data");
		final double [][] data2= kernelData();
		final double [] x= data2[0];
		final double [] y= data2[1];
		for(int i=0; i<y.length; i++) 
			series.add(x[i], y[i]);

		final XYSeriesCollection data = new XYSeriesCollection(series);
		final JFreeChart chart = ChartFactory.createXYLineChart(
				"",
				"", 
				"", 
				data,
				PlotOrientation.VERTICAL,
				false,
				false,
				false
				);

		return chart.createBufferedImage(200, 200);
	}
	
	/**
	 * provides the data points to be visualized
	 * double[0][] - x points
	 * double[1][] - y points
	 * @return double[][]
	 */
	public default double[][] kernelData() {
		final int n=40;
		double [][] data=new double[2][n];
		data[0]=SUtils.linspace(-10.0, 10.0, n);
		for(int i=0; i<n; i++){
			final double x2=data[0][i]*data[0][i];
			data[1][i]= exp(-x2/2.0) / (2.0*sqrt(PI));
		}
		return data;
	}
}