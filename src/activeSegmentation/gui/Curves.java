package activeSegmentation.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ijaux.scale.Pair;

public class  Curves extends ApplicationFrame
{


	public Curves(String title, List<Pair<double[], double[]>> data)
	{
		super(title);
		createChart(title, "False Positive Rate", "True Positive Rate", createRealDataset(data));
	}
	


	private void createChart(String title, String xLabel, String ylabel, XYDataset dataset)
	{
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				title, 
				xLabel, 
				ylabel, 
				dataset, 
				PlotOrientation.VERTICAL, 
				true, true, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new Dimension(560, 367));
		XYPlot plot = xylineChart.getXYPlot();

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		setContentPane(chartPanel);
	}

	private static XYDataset createRealDataset(List<Pair<double[], double[]>> tandFRateList)
	{
		XYSeriesCollection dataset = new XYSeriesCollection();
		int iter = 0;
		for (Pair<double[], double[]> tandFRate : tandFRateList)
		{
			XYSeries series = new XYSeries("Iteration-" + iter);
			double[] tRate = (double[])tandFRate.first;
			double[] fRate = (double[])tandFRate.second;
			for (int i = 0; i < tRate.length; i++) {
				series.add(fRate[i], tRate[i]);
			}
			iter++;
			dataset.addSeries(series);
		}
		return dataset;
	}

	public static void main(String[] args)
	{
		double[] tpsN = { 0.0D, 0.0D, 0.5D, 0.5D, 1.0D };
		double[] fps = { 0.0D, 0.5D, 0.5D, 1.0D, 1.0D };
		double[] tpsN1 = { 0.0D, 0.0D, 0.7D, 0.5D, 1.0D };
		double[] fps1 = { 0.0D, 0.5D, 0.5D, 1.0D, 1.0D };
		List<Pair<double[], double[]>> data = new ArrayList<>();

		Pair<double[], double[]> rocIter1 = new Pair<double[], double[]>(tpsN, fps);
		Pair<double[], double[]> rocIter2 = new Pair<double[], double[]>(tpsN1, fps1);
		data.add(rocIter1);
		data.add(rocIter2);
		Curves demo = new Curves("ROC", data);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
}