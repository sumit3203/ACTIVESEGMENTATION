package activeSegmentation.evaluation;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import activeSegmentation.ASCommon;
//import activeSegmentation.IProjectManager;
import ijaux.scale.Pair;
import activeSegmentation.IEvaluation;
import activeSegmentation.prj.ProjectManager;

public class EvaluationPanel  implements Runnable {


	  private ProjectManager projectManager=null;
	  private IEvaluation evaluation=null;
	  public static final Font FONT = new Font("Arial", 0, 10);
	  final ActionEvent REFRESH_BUTTON_PRESSED = new ActionEvent(this, 2, "Compute");
	  final ActionEvent LOAD_BUTTON_PRESSED = new ActionEvent(this, 3, "Load");
	  final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 4, "Save");
	 private EvaluationCurve evaluationCurve=new  EvaluationCurve();
	  
	  
	  public EvaluationPanel(ProjectManager dataManager, IEvaluation evaluation)
	  {
	    this.projectManager = dataManager;
	    this.evaluation = evaluation;
	  }
	  
	  public void doAction(ActionEvent event) {}
	  
	  public void run()
	  {
	    JFrame frame = new JFrame("EVALUATION");
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	    
	    JPanel controlsBox = new JPanel();
	    
	    JPanel curvesJPanel = new JPanel();
	    curvesJPanel.setBorder(BorderFactory.createTitledBorder("PLOTS"));
	    curvesJPanel.setBounds(10,10,780,300);
	    double[] tpsN = { 0.0D, 0.0D, 0.5D, 0.5D, 1.0D };
		double[] fps = { 0.0D, 0.5D, 0.5D, 1.0D, 1.0D };
		double[] tpsN1 = { 0.0D, 0.0D, 0.7D, 0.5D, 1.0D };
		double[] fps1 = { 0.0D, 0.5D, 0.5D, 1.0D, 1.0D };
		List<Pair<double[], double[]>> data = new ArrayList<>();

		Pair<double[], double[]> rocIter1 = new Pair<double[], double[]>(tpsN, fps);
		Pair<double[], double[]> rocIter2 = new Pair<double[], double[]>(tpsN1, fps1);
		data.add(rocIter1);
		data.add(rocIter2);
		ChartPanel rocPanel=evaluationCurve.createChart("ROC Curve", "False Positive Rate", "True Positive Rate", data);
	   curvesJPanel.add(rocPanel);
	    ChartPanel prPanel=evaluationCurve.createChart("PR Curve", "Precision", "Recall", data);
	   curvesJPanel.add(prPanel);
	    JPanel resultJPanel = new JPanel();
	    resultJPanel.setBorder(BorderFactory.createTitledBorder("RESULTS"));
	    resultJPanel.setBounds(10,330,100,80);
	    
	    
	   // controlsBox.add(addMetrics(), Util.getGbc(1, 0, 1, false, true));
	    controlsBox.add(curvesJPanel);
	  //  controlsBox.add(resultJPanel);
	   // controlsBox.add(resetJPanel, Util.getGbc(0, 2, 1, false, true));
	    
	    frame.add(controlsBox);
	    frame.setSize(800, 600);
	    frame.setLocationRelativeTo(null);
	    //frame.setResizable(false);
	    frame.setVisible(true);
	  }
	  
	  /*
	  private JPanel addMetrics()
	  {
	    JPanel controlJPanel = new JPanel(new GridBagLayout());
	    controlJPanel.setBorder(BorderFactory.createTitledBorder("METRICS"));
	    List<JCheckBox> jCheckBoxList = new ArrayList<JCheckBox>();
	    int j = 0;int k = 0;
	    for (String metrics : this.evaluation.getMetrics())
	    {
	      JCheckBox checkBox = new JCheckBox(metrics);
	      jCheckBoxList.add(checkBox);
	      controlJPanel.add(checkBox, Util.getGbc(k, j, 1, false, false));
	      k++;
	      if (k == 4)
	      {
	        k = 0;
	        j++;
	      }
	    }
	    return controlJPanel;
	  }
	  */
	  /*
	  private JButton addButton(String label, Icon icon, JComponent panel, final ActionEvent action, Dimension dimension, GridBagConstraints labelsConstraints)
	  {
	    JButton button = new JButton();
	    panel.add(button, labelsConstraints);
	    button.setText(label);
	    button.setIcon(icon);
	    button.setFont(Common.FONT);
	    button.setPreferredSize(dimension);
	    button.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent e)
	      {
	        EvaluationPanel.this.doAction(action);
	      }
	    });
	    return button;
	  }
	  */
}
