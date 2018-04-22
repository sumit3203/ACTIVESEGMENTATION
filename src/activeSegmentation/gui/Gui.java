package activeSegmentation.gui;



import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;

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

import activeSegmentation.IProjectManager;
import activeSegmentation.feature.FeatureManagerNew;


public class Gui {
	private JFrame mainFrame=null;
	private JPanel controlPanel=null;

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent FEATURE_BUTTON_PRESSED = new ActionEvent( this, 0, "Feature" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent FILTER_BUTTON_PRESSED = new ActionEvent( this, 1, "Filter" );
	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent LEARNING_BUTTON_PRESSED = new ActionEvent( this, 2, "Learning" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent EVALUATION_BUTTON_PRESSED = new ActionEvent( this, 3, "Evaluation" );
<<<<<<< HEAD
	private LearningPanel learningPanel;
	private FilterPanel filterPanel;
	private FeaturePanelNew featurePanel;

	public static final Font FONT = new Font( "Arial", Font.BOLD, 13 );
	private IProjectManager projectManager;
=======
	
	private LearningPanel learningPanel=null;
	private FilterPanel filterPanel=null;
	private FeaturePanel featurePanel=null;

	public static final Font FONT = new Font( "Arial", Font.BOLD, 13 );
	private IProjectManager projectManager=null;

>>>>>>> 700275c529f6d28413100ef34528e8cd290f9f71
	public Gui(IProjectManager projectManager){
		this.projectManager=projectManager;
		prepareGUI();
		
	}

	
	public void doAction( final ActionEvent event )
	{
		System.out.println("IN DO ACTION");
		System.out.println(event.toString());
		if(event ==FILTER_BUTTON_PRESSED ){
			if(filterPanel==null){
			   filterPanel=new FilterPanel(projectManager);
			SwingUtilities.invokeLater(filterPanel);
			}

		}
		if(event==FEATURE_BUTTON_PRESSED){
			if(featurePanel== null){
			/*		ImagePlus image= IJ.openImage(projectManager.getMetaInfo().getTrainingStack());
			    featurePanel= new FeaturePanel(new FeatureManager(image.getStackSize(),projectManager),image);
			    */
				new FeaturePanelNew(new FeatureManagerNew(projectManager));
				
			}
		}
		
		

		if(event==LEARNING_BUTTON_PRESSED){
			if(learningPanel==null)
			   learningPanel = new LearningPanel();
			SwingUtilities.invokeLater(learningPanel);
		}
		
		if(event==EVALUATION_BUTTON_PRESSED){
			
		//	EvaluationPanel evaluationPanel = new EvaluationPanel(dataManager, evaluation);
		//	SwingUtilities.invokeLater(evaluationPanel);
			
		}
	}
	


	private void prepareGUI(){
		
		//Make sure we have nice window decorations.
		//JFrame.setDefaultLookAndFeelDecorated(true);
		mainFrame = new JFrame("ACTIVE SEGMENTATION");
		mainFrame.getContentPane().setBackground( Color.GRAY );
		mainFrame.setLocationRelativeTo(null);
		//mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(500,400);

		controlPanel = new JPanel();
		controlPanel.setLayout(null);
		controlPanel.setBackground(Color.GRAY );
		JLabel label= new JLabel("Active Segmentation");
		label.setFont(new Font( "Arial", Font.BOLD, 32 ));
		label.setBounds( 100, 50, 450, 100 );
		label.setForeground(Color.ORANGE);
		controlPanel.add(label);
		controlPanel.add(addButton( "FILTERS", null, 25, 150, 200, 50,FILTER_BUTTON_PRESSED) );
		controlPanel.add(addButton( "FEATURE EXTRACTION", null, 275, 150, 200, 50,FEATURE_BUTTON_PRESSED));
		controlPanel.add(addButton("LEARNING", null, 25, 250, 200, 50, LEARNING_BUTTON_PRESSED ));
		controlPanel.add(addButton( "EVALUATION", null, 275, 250, 200, 50, EVALUATION_BUTTON_PRESSED ));
		
		// postioning

		controlPanel.setLocation(0, 0);
		mainFrame.add(controlPanel);
		mainFrame.setVisible(true);  

	}
	

	private ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = CreatProject.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {            
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	} 
	private JButton addButton( final String label, final ImageIcon icon, final int x,
			final int y, final int width, final int height,final ActionEvent action)
	{
		final JButton button =  new JButton(label, icon);
		button.setFont( FONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		button.setBounds( x, y, width, height );
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				//System.out.println("CLICKED");
				doAction(action);
			}
		} );

		return button;
	}






}