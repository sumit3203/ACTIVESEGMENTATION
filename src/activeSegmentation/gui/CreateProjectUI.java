package activeSegmentation.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import activeSegmentation.*;
import activeSegmentation.prj.ProjectManager;
import ij.WindowManager;


public class CreateProjectUI implements Runnable, ASCommon {

	public static final Font FONT = new Font( "Arial", Font.BOLD, 13 );
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent CREATE_BUTTON_PRESSED = new ActionEvent( this, 1, "create" );
	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent OPEN_BUTTON_PRESSED = new ActionEvent( this, 2, "open" );
	final ActionEvent BROWSE_BUTTON_PRESSED = new ActionEvent( this, 3, "browse" );
	final ActionEvent TRAININGF_BUTTON_PRESSED = new ActionEvent( this, 4, "browse" );
	final ActionEvent Tiff_BUTTON_PRESSED = new ActionEvent( this, 8, "tiff" );
	final ActionEvent TESTINGF_BUTTON_PRESSED = new ActionEvent( this, 5, "browse" );
	final ActionEvent FINISH_BUTTON_PRESSED = new ActionEvent( this, 6, "finish" );
	final ActionEvent CANCEL_BUTTON_PRESSED = new ActionEvent( this, 7, "cancel" );
	private JTextField projectFField= new JTextField();
	private JTextField projectNField = new JTextField();
	private JTextField projectDField= new JTextField();
	private JTextField trainingImageP = new JTextField();
	private JLabel errorText= new JLabel("");
	//private JTextField pluginsDir = new JTextField();
	
	JComboBox<ProjectType> projectList;
	private JFrame createProject;
	private ProjectManager projectManager;
	
	
	public CreateProjectUI(ProjectManager projectManager) {
		this.projectManager=projectManager;
		projectList= new JComboBox<>(ProjectType.values());
	}
	
	
	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
	Panel all = new Panel();
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		JFrame mainFrame = new JFrame();
		mainFrame.getContentPane().setBackground( Color.GRAY );
		//mainFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainFrame.setSize(frameWidth,frameHeight);
		mainFrame.setLocationRelativeTo(null);
		JPanel controlFrame= new JPanel();
		controlFrame.setLayout(null);
		controlFrame.setBackground(Color.GRAY );
		//setControls(controlFrame);
		//JLabel logo= new JLabel(createImageIcon("images/logo1.png","logo"));
		//logo.setBounds( 10, 10, 450, 200 );
		//controlFrame.add(logo);
		JLabel label= new JLabel("Active Segmentation");
		label.setFont(new Font( "Arial", Font.BOLD, 32 ));
		label.setBounds( 100, 150, 450, 100 );
		label.setForeground(Color.ORANGE);
		controlFrame.add(label);
		//controlFrame.add(new JLabel(createImageIcon("images/logo.png", "logo")));
		controlFrame.add(addButton("Create New Project",createImageIcon("addProject.png","add"), 30, 250, 220, 60, CREATE_BUTTON_PRESSED));
		controlFrame.add(addButton("Open Project",createImageIcon("openProject.png","add"),270, 250, 200, 60, OPEN_BUTTON_PRESSED));
		controlFrame.setLocation(0, 0);
		mainFrame.add(controlFrame);
		mainFrame.setVisible(true);  
	}
	
	private void doAction( final ActionEvent event )
	{ 
		if(event ==CREATE_BUTTON_PRESSED ){
			createProject=createProject();
		}

		if(event ==OPEN_BUTTON_PRESSED ){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
			 String file=fileChooser.getSelectedFile().toString();
			 projectManager.loadProject(file);
			 new Gui(projectManager);
			// new Gui2(projectManager);
			}
		}

		if(event== BROWSE_BUTTON_PRESSED){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				projectFField.setText(fileChooser.getSelectedFile().toString());
			}
		}
		
		if(event== TRAININGF_BUTTON_PRESSED){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				trainingImageP.setText(fileChooser.getSelectedFile().toString());
			}
		}
		
		if(event== Tiff_BUTTON_PRESSED){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				trainingImageP.setText(fileChooser.getSelectedFile().toString());
			}
		}
		
		if(event== TESTINGF_BUTTON_PRESSED){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				//pluginsDir.setText(fileChooser.getSelectedFile().toString());
			}
		}
		
		if(event== CANCEL_BUTTON_PRESSED){

			if(createProject!=null){
				System.out.println("Cancel");
				createProject.setVisible(false);
				createProject.dispose();
			}
		}
		
		if(event== FINISH_BUTTON_PRESSED){
			String projectName=projectNField.getText();
			String projectDirectory=projectFField.getText();
			String projectDescription=projectDField.getText();
			String trainingImage=trainingImageP.getText();
			//String pluginDir=pluginsDir.getText();
			String projectType=projectList.getSelectedItem().toString();
			//System.out.println(projectName+"--"+ projectType);
			String message=projectManager.createProject(projectName, projectType, projectDirectory, projectDescription, 
					trainingImage);	
			if("DONE".equalsIgnoreCase(message)) {
				createProject.setVisible(false);
				createProject.dispose();
				new Gui(projectManager);
			}
			else {
				errorText.setText(message);
			}
		}
	}
	

	private JFrame createProject(){
		
		JFrame mainFrame = new JFrame("Create Project");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setBackground( Color.GRAY );
		mainFrame.setSize(600,500);
		mainFrame.setLocationRelativeTo(null);
		JPanel controlFrame= new JPanel();
		controlFrame.setLayout(null);
		controlFrame.setBackground(Color.GRAY );
		JLabel label= new JLabel("Create Project");
		label.setFont(new Font( "Arial", Font.BOLD, 32 ));
		label.setBounds( 50, 0, 450, 100 );
		label.setForeground(Color.ORANGE);
		controlFrame.add(label);
		JLabel projectName= new JLabel("Project Name *:");
		projectName.setFont(new Font( "Arial", Font.PLAIN, 20 ));
		projectName.setBounds( 50, 100, 200, 30 );
		controlFrame.add(projectName);
		projectNField.setColumns(20);
		projectNField.setBounds( 200, 100, 250, 30 );
		controlFrame.add(projectNField);
		JLabel projectDesc= new JLabel("Project Desc :");
		projectDesc.setFont(new Font( "Arial", Font.PLAIN, 20 ));
		projectDesc.setBounds( 50, 140, 200, 30 );
		controlFrame.add(projectDesc);

		projectDField.setColumns(20);
		projectDField.setBounds( 200, 140, 250, 30 );
		controlFrame.add(projectDField);
		JLabel projectType= new JLabel("Project Type :");
		projectType.setFont(new Font( "Arial", Font.PLAIN, 20 ));
		projectType.setBounds( 50, 180, 250, 30 );
		controlFrame.add(projectType);


		projectList.setSelectedIndex(0);
		projectList.setBounds( 200, 180, 250, 30 );
		controlFrame.add(projectList);
		JLabel projectFolder= new JLabel("Project Folder *:");
		projectFolder.setFont(new Font( "Arial", Font.PLAIN, 20 ));
		projectFolder.setBounds( 50, 220, 200, 30 );
		controlFrame.add(projectFolder);		
		projectFField.setColumns(200);
		projectFField.setBounds( 200, 220, 250, 30 );
		controlFrame.add(projectFField);	
		controlFrame.add(addButton("Browse",null, 460, 220, 100, 30, BROWSE_BUTTON_PRESSED));
		
		if(null == WindowManager.getCurrentImage()) {
			JLabel trainingImage= new JLabel("Training Image *:");
			trainingImage.setFont(new Font( "Arial", Font.PLAIN, 20 ));
			trainingImage.setBounds( 50, 260, 200, 30 );
			controlFrame.add(trainingImage);		
			trainingImageP.setColumns(200);
			trainingImageP.setBounds( 200, 260, 250, 30 );
			controlFrame.add(trainingImageP);	
			controlFrame.add(addButton("Folder",null, 250, 300, 100, 30, TRAININGF_BUTTON_PRESSED));
			controlFrame.add(addButton("Image Stack",null, 360, 300, 100, 30, Tiff_BUTTON_PRESSED));
		}
		
		/*JLabel testImage= new JLabel("Plugin Dir :");
		testImage.setFont(new Font( "Arial", Font.PLAIN, 20 ));
		testImage.setBounds( 50, 300, 200, 30 );
		controlFrame.add(testImage);		*/
		//pluginsDir.setColumns(200);
		//pluginsDir.setBounds( 200, 300, 250, 30 );
		//controlFrame.add(pluginsDir);	
		errorText.setFont(new Font( "Arial", Font.BOLD, 12 ));
		errorText.setBounds(30, 300, 600, 30 );
		errorText.setForeground(Color.RED);
		controlFrame.add(errorText);

		//controlFrame.add(addButton("Browse",null, 460, 300, 100, 30, TESTINGF_BUTTON_PRESSED));
		controlFrame.add(addButton("Finish",null, 30, 350, 220, 60, FINISH_BUTTON_PRESSED));
		controlFrame.add(addButton("Cancel",null,270, 350, 200, 60, CANCEL_BUTTON_PRESSED));

		controlFrame.setLocation(0, 0);
		mainFrame.add(controlFrame);
		mainFrame.setVisible(true);
		return mainFrame;

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


	private ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = CreateProjectUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {            
			//System.err.println("Couldn't find file: " + path);
			return null;
		}
	}   


}
