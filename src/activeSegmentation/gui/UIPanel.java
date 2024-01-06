package activeSegmentation.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import activeSegmentation.ASCommon;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ProjectManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;

/**
 * 
 * @author prodanov
 *
 */
public class UIPanel extends JFrame  implements ASCommon  {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanel featurePanel;
	private ViewFilterOutputPanel filterOutputPanel;
	private ProjectManager projectManager;
	private EvaluationPanel evaluationPanel;
	private SessionGUI sessionPanel;
	
	private FeatureManager featureManager;
	private ClassifierManager learningManager;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIPanel frame = new UIPanel(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UIPanel(ProjectManager projMan) {
		setTitle("Active Segmentation v." + version);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
	
		System.out.println(".....");
		this.projectManager = projMan;
		System.out.println("ClassifierManager init");
		learningManager = new ClassifierManager(this.projectManager);
		System.out.println("FeatureManager init");
		featureManager=new FeatureManager(this.projectManager, this.learningManager);
		
		System.out.println("init Project GuiPanel ");
		
		
		JButton btnFilters = new JButton("Select Filters");
		btnFilters.addActionListener(e -> {		
				// for time being feature manager is passed , will think
				// of better design later
				filterPanel = new FilterPanel(projectManager,featureManager);
				filterPanel.setVisible(true);
			
		});
		btnFilters.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnFilters.setBounds(52, 119, 129, 35);
		contentPane.add(btnFilters);
		
		JLabel label = new JLabel("Active Segmentation");
		label.setForeground(Color.ORANGE);
		label.setFont(new Font("Arial", Font.BOLD, 32));
		label.setBounds(52, 11, 450, 100);
		contentPane.add(label);
		
		JButton btnFeatures = new JButton("Feature Extraction");
		btnFeatures.addActionListener(e -> {
		
				featurePanel=new FeaturePanel(featureManager);
				featurePanel.setVisible(true);
				
			
		});
		btnFeatures.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnFeatures.setBounds(52, 188, 129, 35);
		contentPane.add(btnFeatures);
		
		JButton btnEvaluation = new JButton("Evaluation");
		btnEvaluation.addActionListener(e -> {
	
				evaluationPanel = new EvaluationPanel(projectManager,null);
				evaluationPanel.setVisible(true);
	
		});
		btnEvaluation.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnEvaluation.setBounds(52, 252, 129, 35);
		contentPane.add(btnEvaluation);
		
		JButton btnFilterViz = new JButton("Filter Visualization");
		
		btnFilterViz.addActionListener(e -> {
		
		 		filterOutputPanel=new ViewFilterOutputPanel(projectManager,featureManager);
		 		filterOutputPanel.setVisible(true);
	
		});
		
		btnFilterViz.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnFilterViz.setBounds(244, 119, 129, 35);
		contentPane.add(btnFilterViz);
		
		JButton btnLearning = new JButton("Model Learning");
		btnLearning.addActionListener(e -> {
	
				learningPanel = new LearningPanel(projectManager, learningManager);
				learningPanel.setVisible(true);
	
		});
		btnLearning.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnLearning.setBounds(244, 188, 129, 35);
		contentPane.add(btnLearning);
		
		JButton btnSessions = new JButton("Sessions");
		btnSessions.addActionListener(e ->{  
				sessionPanel = new SessionGUI(projectManager);
			 
		});
		
		btnSessions.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSessions.setBounds(244, 252, 129, 35);
		contentPane.add(btnSessions);
	}
}
