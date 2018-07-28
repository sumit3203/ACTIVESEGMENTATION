package activeSegmentation.gui;



import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import activeSegmentation.IEvaluation;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.evaluation.EvaluationMetrics;
import activeSegmentation.feature.FeatureManagerNew;


public class Gui
{
	private JFrame mainFrame;
	private JPanel controlPanel;
	final ActionEvent FEATURE_BUTTON_PRESSED = new ActionEvent(this, 0, "Feature");
	final ActionEvent FILTER_BUTTON_PRESSED = new ActionEvent(this, 1, "Filter");
	final ActionEvent LEARNING_BUTTON_PRESSED = new ActionEvent(this, 2, "Learning");
	final ActionEvent EVALUATION_BUTTON_PRESSED = new ActionEvent(this, 3, "Evaluation");
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanelNew featurePanel;
	private static IFeatureManagerNew featureManager;
	public static final Font FONT = new Font("Arial", 1, 13);
	private IProjectManager projectManager;

	public Gui(IProjectManager projectManager)
	{
		this.projectManager = projectManager;
		featureManager=new FeatureManagerNew(this.projectManager);
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

		if ((event == this.FEATURE_BUTTON_PRESSED)) {
			if (this.featurePanel == null) {
				new FeaturePanelNew(featureManager);
			}	
		}
			
		if (event == this.LEARNING_BUTTON_PRESSED)
		{
			if (this.learningPanel == null) {
				this.learningPanel = new LearningPanel(this.projectManager);
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

		this.mainFrame.setSize(550, 400);

		this.controlPanel = new JPanel();
		this.controlPanel.setLayout(null);
		this.controlPanel.setBackground(Color.GRAY);
		JLabel label = new JLabel("Active Segmentation");
		label.setFont(new Font("Arial", 1, 32));
		label.setBounds(100, 50, 450, 100);
		label.setForeground(Color.ORANGE);
		this.controlPanel.add(label);
		this.controlPanel.add(addButton("FILTERS", null, 25, 150, 200, 50, this.FILTER_BUTTON_PRESSED));
		this.controlPanel.add(addButton("FEATURE EXTRACTION", null, 275, 150, 200, 50, this.FEATURE_BUTTON_PRESSED));
		this.controlPanel.add(addButton("LEARNING", null, 25, 250, 200, 50, this.LEARNING_BUTTON_PRESSED));
		this.controlPanel.add(addButton("EVALUATION", null, 275, 250, 200, 50, this.EVALUATION_BUTTON_PRESSED));

		this.controlPanel.setLocation(0, 0);
		this.mainFrame.add(this.controlPanel);
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
		button.setForeground(Color.WHITE);
		button.setBounds(x, y, width, height);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Gui.this.doAction(action);
			}
		});
		return button;
	}
}