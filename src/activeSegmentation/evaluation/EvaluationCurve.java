package activeSegmentation.evaluation;

import java.awt.Dimension;
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

import ijaux.scale.Pair;

public class EvaluationCurve {

	public  ChartPanel  createChart(String title, String xLabel, String ylabel, List<Pair<double[], double[]>> tandFRateList){
		XYDataset dataset=createRealDataset(tandFRateList);
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				title, 
				xLabel, 
				ylabel, 
				dataset, 
				PlotOrientation.VERTICAL, 
				true, true, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		//chartPanel.setLayout(new GridBagLayout());

		chartPanel.setPreferredSize(new Dimension(350, 300));
		XYPlot plot = xylineChart.getXYPlot();

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		plot.setRenderer(renderer);
		return chartPanel;
	}

	private static XYDataset createRealDataset(List<Pair<double[], double[]>> tandFRateList){
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
}
