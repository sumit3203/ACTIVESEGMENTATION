package activeSegmentation.gui;

import activeSegmentation.ASCommon;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ProjectManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Selector GUI class
 * @author Sumit Vohra, Dimiter Prodanov
 *
 */
public class GuiPanel extends JFrame implements ASCommon {

	private JFrame mainFrame;
	private JPanel controlPanel;
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanel featurePanel;
	private ViewFilterOutputPanel filterOutputPanel;


	final ActionEvent FEATURE_BUTTON_PRESSED    = new ActionEvent(this, 0, "Feature"   );
	final ActionEvent FILTER_BUTTON_PRESSED     = new ActionEvent(this, 1, "Filter"    );
	final ActionEvent LEARNING_BUTTON_PRESSED   = new ActionEvent(this, 2, "Learning"  );
	final ActionEvent EVALUATION_BUTTON_PRESSED = new ActionEvent(this, 3, "Evaluation");
	final ActionEvent FILTERVIS_BUTTON_PRESSED  = new ActionEvent(this, 4, "FilterVis" );
	final ActionEvent SESSIONGUI_BUTTON_PRESSED = new ActionEvent(this, 5, "SessionGUI");
	final ActionEvent VISUALIZATION_BUTTON_PRESSED = new ActionEvent(this, 8, "Visualization");
	final ActionEvent BACK_BUTTON_PRESSED       = new ActionEvent(this, 6, "Back");
	final ActionEvent EXIT_BUTTON_PRESSED       = new ActionEvent(this, 7, "Exit");


	private FeatureManager featureManager;
	private ClassifierManager learningManager;
	private ProjectManager projectManager;
	private EvaluationPanel evaluationPanel;
	private VisualizationPanel visualizationPanel;

	/**
	 *
	 * @param projectManager
	 */
	public GuiPanel(ProjectManager projectManager)	{
		System.out.println(".....");
		this.projectManager = projectManager;
		System.out.println("ClassifierManager init");
		learningManager = new ClassifierManager(this.projectManager);
		System.out.println("FeatureManager init");
		featureManager=new FeatureManager(this.projectManager, this.learningManager);

		System.out.println("init Project GuiPanel ");
		initGUI();
	}

	public JPanel getMainPanel() {
		return controlPanel;
	}

	public void doAction(ActionEvent event) 	{
		if ((event == this.FILTER_BUTTON_PRESSED)) {
			//if(this.filterPanel == null) {
			// for time being feature manager is passed , will think
			// of better design later
			filterPanel = new FilterPanel(projectManager,featureManager);
			//}
			SwingUtilities.invokeLater(filterPanel);
		}

		if(event==this.FILTERVIS_BUTTON_PRESSED){
			//if (this.filterOutputPanel==null) {
			filterOutputPanel=new ViewFilterOutputPanel(projectManager,featureManager);
			//}
			SwingUtilities.invokeLater(this.filterOutputPanel);
		}

		if ((event == this.FEATURE_BUTTON_PRESSED)) {
			//if (this.featurePanel == null) {
			featurePanel=new FeaturePanel(featureManager);
			//}
			SwingUtilities.invokeLater(this.featurePanel);
		}

		if (event == this.LEARNING_BUTTON_PRESSED)	{
			//if (this.learningPanel == null) {
			learningPanel = new LearningPanel(projectManager, learningManager);
			//}
			SwingUtilities.invokeLater(learningPanel);
		}

		if (event == this.EVALUATION_BUTTON_PRESSED) {
			//if (evaluationPanel==null) {
			evaluationPanel = new EvaluationPanel(projectManager, null);
			//}
			SwingUtilities.invokeLater(evaluationPanel);

		}

		if (event == this.SESSIONGUI_BUTTON_PRESSED) {
			new SessionGUI(projectManager); // Create and display the SessionGUI instance
		}

		if (event == this.VISUALIZATION_BUTTON_PRESSED) {
			SwingUtilities.invokeLater(() -> {
				JFrame frame = new JFrame("Visualization Panel");
				frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				frame.setSize(800, 600);
				frame.setLocationRelativeTo(null); // Center the frame
				frame.add(new VisualizationPanel());
				frame.setVisible(true);
			});
		}

		if (event == this.BACK_BUTTON_PRESSED) {
			mainFrame.dispose(); // Close the current window
			new CreateOpenProjectGUI(projectManager).run(); // Reopen the main window
		}

		// Confirm Exit
		if (event == this.EXIT_BUTTON_PRESSED) {
			int response = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit?", "Confirm Exit",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}

	}


	/**
	 *
	 */
	private void initGUI()	{
		this.mainFrame = new JFrame("Active Segmentation v." + version);
		this.mainFrame.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.mainFrame.setLocationRelativeTo(null);
		this.mainFrame.setSize(frameWidth, frameHeight);
		this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent the default close operation

		// Confirm Exit to intercept the window close event
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(mainFrame,
						"Are you sure you want to exit?", "Confirm Exit",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});

		this.controlPanel = new JPanel();
		this.controlPanel.setLayout(null);
		this.controlPanel.setBackground(Color.GRAY);

		JLabel label = new JLabel("Active Segmentation");
		label.setFont(largeFONT);
		label.setBounds(130, 5, 450, 100);
		label.setForeground(Color.ORANGE);
		this.controlPanel.add(label);
		this.controlPanel.add(addButton("Select Filters",       null,  70, 100, 200, 50, this.FILTER_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Filter Visualization", null, 310, 100, 200, 50, this.FILTERVIS_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Feature Extraction",   null,  70, 170, 200, 50, this.FEATURE_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Model Learning",       null, 310, 170, 200, 50, this.LEARNING_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Evaluation",           null,  70, 240, 200, 50, this.EVALUATION_BUTTON_PRESSED));
		this.controlPanel.add(addButton("View Sessions",        null,  310, 240, 200, 50, this.SESSIONGUI_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Visualization",        null,  70, 310, 200, 50, this.VISUALIZATION_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Back", 				 null, 310, 310, 98, 50, this.BACK_BUTTON_PRESSED));
		this.controlPanel.add(addButton("Exit",                 null,  413, 310, 98, 50, this.EXIT_BUTTON_PRESSED));

	}


	private JButton addButton(String label, ImageIcon icon, int x, int y, int width, int height, final ActionEvent action)	{
		JButton button = new JButton(label, icon);
		button.setFont(labelFONT);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBackground(buttonBGColor);
		button.setForeground(Color.WHITE);
		button.setBounds(x, y, width, height);
		button.addActionListener(new ActionListener()		{
			@Override
			public void actionPerformed(ActionEvent e)			{
				doAction(action);
			}
		});
		return button;
	}
}
