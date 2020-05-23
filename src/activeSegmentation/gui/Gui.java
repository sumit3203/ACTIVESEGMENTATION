package activeSegmentation.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import activeSegmentation.ASCommon;
import activeSegmentation.IEvaluation;
//import activeSegmentation.ILearningManager;
//import activeSegmentation.IProjectManager;
import activeSegmentation.evaluation.EvaluationMetrics;
import activeSegmentation.evaluation.EvaluationPanel;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ProjectManager;


public class Gui implements ASCommon {
	
	private JFrame mainFrame;
	private JPanel controlPanel;
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanelNew featurePanel;
	//private ViewFilterResults viewFilterResults;
	
	final ActionEvent FEATURE_BUTTON_PRESSED = new ActionEvent(this, 0, "Feature");
	final ActionEvent FILTER_BUTTON_PRESSED = new ActionEvent(this, 1, "Filter");
	final ActionEvent LEARNING_BUTTON_PRESSED = new ActionEvent(this, 2, "Learning");
	final ActionEvent EVALUATION_BUTTON_PRESSED = new ActionEvent(this, 3, "Evaluation");
	final ActionEvent FILTERVIS_BUTTON_PRESSED = new ActionEvent(this, 4, "FilterVis");

	
	private FeatureManager featureManager;
	private ClassifierManager learningManager;
	private ProjectManager projectManager;
	


	public Gui(ProjectManager projectManager)	{
		this.projectManager = projectManager;
		learningManager = new ClassifierManager(this.projectManager);
		featureManager=new FeatureManager(this.projectManager, this.learningManager);
		
		prepareGUI();
	}

	public void doAction(ActionEvent event) 	{
		if ((event == this.FILTER_BUTTON_PRESSED)) {
			if(this.filterPanel == null) {
				// for time being feature manager is passed , will think
				// of better design later
				this.filterPanel = new FilterPanel(this.projectManager,featureManager);
			}	
			SwingUtilities.invokeLater(this.filterPanel);
		}
		
		if(event==this.FILTERVIS_BUTTON_PRESSED){
		      // filterManager.getFinalImage().show();		
				new ViewFilterResults(this.projectManager,featureManager);

			}

		if ((event == this.FEATURE_BUTTON_PRESSED)) {
			if (this.featurePanel == null) {
				new FeaturePanelNew(featureManager);
			}	
		}
			
		if (event == this.LEARNING_BUTTON_PRESSED)	{
			if (this.learningPanel == null) {
				this.learningPanel = new LearningPanel(this.projectManager, this.learningManager);
			}
			SwingUtilities.invokeLater(this.learningPanel);
		}
		if (event == this.EVALUATION_BUTTON_PRESSED) {
			IEvaluation evaluation = new EvaluationMetrics();
			EvaluationPanel evaluationPanel = new EvaluationPanel(this.projectManager, evaluation);
			SwingUtilities.invokeLater(evaluationPanel);
		}
	}
	
	

	private void prepareGUI()	{
		this.mainFrame = new JFrame("Active Segmentation");
		this.mainFrame.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.mainFrame.setLocationRelativeTo(null);

		this.mainFrame.setSize(frameWidth, frameHeight);

		this.controlPanel = new JPanel();
		this.controlPanel.setLayout(null);
		this.controlPanel.setBackground(Color.GRAY);
		JLabel label = new JLabel("Active Segmentation");
		label.setFont(new Font("Arial", 1, 32));
		label.setBounds(100, 50, 450, 100);
		label.setForeground(Color.ORANGE);
		this.controlPanel.add(label);
		this.controlPanel.add(addButton("Select Filters", null, 25, 150, 200, 50, this.FILTER_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Filter Visualization", null, 275, 150, 200, 50, this.FILTERVIS_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Feature Extraction", null, 25, 250, 200, 50, this.FEATURE_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Model Learning", null, 275, 250, 200, 50, this.LEARNING_BUTTON_PRESSED));
		//this.controlPanel.add(addButton("EVALUATION", null, 25, 350, 200, 50, this.EVALUATION_BUTTON_PRESSED));

		this.controlPanel.setLocation(0, 0);
		this.mainFrame.add(this.controlPanel);
		this.mainFrame.setVisible(true);
	}


	private JButton addButton(String label, ImageIcon icon, int x, int y, int width, int height, final ActionEvent action)	{
		JButton button = new JButton(label, icon);
		button.setFont(labelFONT);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		button.setBounds(x, y, width, height);
		button.addActionListener(new ActionListener()		{
			public void actionPerformed(ActionEvent e)			{
				Gui.this.doAction(action);
			}
		});
		return button;
	}
}
