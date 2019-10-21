package activeSegmentation.gui;



import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import activeSegmentation.IEvaluation;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.ILearningManager;
import activeSegmentation.IProjectManager;
import activeSegmentation.evaluation.EvaluationMetrics;
import activeSegmentation.feature.FeatureManagerNew;
import activeSegmentation.learning.ClassifierManager;


public class Gui2
{
	private JFrame mainFrame;
	private JPanel controlPanel;
	final ActionEvent FEATURE_BUTTON_PRESSED = new ActionEvent(this, 0, "Feature");
	final ActionEvent FILTER_BUTTON_PRESSED = new ActionEvent(this, 1, "Filter");
	final ActionEvent LEARNING_BUTTON_PRESSED = new ActionEvent(this, 2, "Learning");
	final ActionEvent EVALUATION_BUTTON_PRESSED = new ActionEvent(this, 3, "Evaluation");
	final ActionEvent FILTERVIS_BUTTON_PRESSED = new ActionEvent(this, 4, "FilterVis");
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanelNew featurePanel;
	private ViewFilterResults viewFilterResults;
	private static IFeatureManagerNew featureManager;
	public static final Font FONT = new Font("Arial", 1, 13);
	private IProjectManager projectManager;
	Panel all;
	ILearningManager learningManager;
	public Gui2(IProjectManager projectManager)
	{
		this.projectManager = projectManager;
		learningManager = new ClassifierManager(this.projectManager);
		featureManager=new FeatureManagerNew(this.projectManager, this.learningManager);
		prepareGUI();
	}

	public void doAction(ActionEvent event)
	{
		//System.out.println("IN DO ACTION");
		//System.out.println(event.toString());
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
			
		if (event == this.LEARNING_BUTTON_PRESSED)
		{
			if (this.learningPanel == null) {
				this.learningPanel = new LearningPanel(this.projectManager, this.learningManager);
			}
			SwingUtilities.invokeLater(this.learningPanel);
		}
		if (event == this.EVALUATION_BUTTON_PRESSED)
		{
			IEvaluation evaluation = new EvaluationMetrics();
			EvaluationPanel evaluationPanel = new EvaluationPanel(this.projectManager, evaluation);
			SwingUtilities.invokeLater(evaluationPanel);
		}
	}

	private void prepareGUI()
	{
		this.mainFrame = new JFrame("ACTIVE SEGMENTATION");
		this.mainFrame.getContentPane().setBackground(Color.GRAY);
		this.mainFrame.setLocationRelativeTo(null);
		 JPanel pipelineJPanel = new JPanel();
		pipelineJPanel.setBorder(BorderFactory.createTitledBorder("Pipeline"));
		GridBagLayout pipelineLayout = new GridBagLayout();
		GridBagConstraints pipeineConstraints = new GridBagConstraints();
		pipeineConstraints.anchor = GridBagConstraints.NORTHWEST;
		pipeineConstraints.fill = GridBagConstraints.HORIZONTAL;
		pipeineConstraints.gridwidth = 1;
		pipeineConstraints.gridheight = 1;
		pipeineConstraints.gridx = 0;
		pipeineConstraints.gridy = 0;
		pipeineConstraints.insets = new Insets(5, 5, 6, 6);
		pipelineJPanel.setLayout(pipelineLayout);
		
		this.mainFrame.setSize(550, 550);
		GridBagLayout layout = new GridBagLayout();
		 this.all = new Panel();
		GridBagConstraints allConstraints = new GridBagConstraints();
		this.all.setLayout(layout);
		//all.setBackground(Color.GRAY);
		allConstraints.anchor = GridBagConstraints.NORTHWEST;
		allConstraints.fill = GridBagConstraints.BOTH;
		allConstraints.gridwidth = 1;
		allConstraints.gridheight = 2;
		allConstraints.gridx = 0;
		allConstraints.gridy = 0;
		allConstraints.weightx = 0;
		allConstraints.weighty = 0;

		this.all.add(pipelineJPanel, allConstraints);

		//this.controlPanel = new JPanel();
		//this.controlPanel.setLayout(controlLayout);
		//this.controlPanel.setBackground(Color.GRAY);
		//JLabel label = new JLabel("Active Segmentation");
		//label.setFont(new Font("Arial", 1, 32));
		//label.setBounds(100, 50, 450, 100);
		//label.setForeground(Color.ORANGE);
		//this.controlPanel.add(label);
	/*	this.controlPanel.add(addButton("FILTERS", null, 25, 150, 200, 50, this.FILTER_BUTTON_PRESSED));
		this.controlPanel.add(addButton("FILTER VISUALIZATION", null, 275, 150, 200, 50, this.FILTERVIS_BUTTON_PRESSED));
		this.controlPanel.add(addButton("FEATURE EXTRACTION", null, 25, 250, 200, 50, this.FEATURE_BUTTON_PRESSED));
		this.controlPanel.add(addButton("LEARNING", null, 275, 250, 200, 50, this.LEARNING_BUTTON_PRESSED));
		this.controlPanel.add(addButton("EVALUATION", null, 25, 350, 200, 50, this.EVALUATION_BUTTON_PRESSED));
    */
		pipelineJPanel.add(addButton("FILTERS", null, 25, 150, 200, 50, this.FILTER_BUTTON_PRESSED), pipeineConstraints);
		pipeineConstraints.gridy++;
		pipelineJPanel.add(addButton("FILTER VISUALIZATION", null, 275, 150, 200, 50, this.FILTERVIS_BUTTON_PRESSED),pipeineConstraints);
		pipeineConstraints.gridy++;
		pipelineJPanel.add(addButton("FEATURE EXTRACTION", null, 25, 250, 200, 50, this.FEATURE_BUTTON_PRESSED),pipeineConstraints);
		pipeineConstraints.gridy++;
		pipelineJPanel.add(addButton("LEARNING", null, 275, 250, 200, 50, this.LEARNING_BUTTON_PRESSED),pipeineConstraints);
		pipeineConstraints.gridy++;
		pipelineJPanel.add(addButton("EVALUATION", null, 25, 350, 200, 50, this.EVALUATION_BUTTON_PRESSED),pipeineConstraints);
		pipeineConstraints.gridy++;
		
		//this.controlPanel.setLocation(0, 0);
		this.all.add(pipelineJPanel, allConstraints);
		this.mainFrame.setContentPane(this.all);
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);
	}

	private ImageIcon createImageIcon(String path, String description)
	{
		URL imgURL = CreatProject.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		}
		//System.err.println("Couldn't find file: " + path);
		return null;
	}

	private JButton addButton(String label, ImageIcon icon, int x, int y, int width, int height, final ActionEvent action)
	{
		JButton button = new JButton(label, icon);
		button.setFont(FONT);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBackground(new Color(192, 192, 192));
		//button.setForeground(Color.WHITE);
		//button.setBounds(x, y, width, height);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Gui2.this.doAction(action);
			}
		});
		return button;
	}
}