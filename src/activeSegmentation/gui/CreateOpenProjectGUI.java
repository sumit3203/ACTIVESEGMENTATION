package activeSegmentation.gui;

import activeSegmentation.ASCommon;
import activeSegmentation.ProjectType;
import activeSegmentation.prj.ProjectManager;
import ij.IJ;
import ij.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

//  to rename to CreateOpenProjectUI
public class CreateOpenProjectGUI implements Runnable, ASCommon {

	//public static final Font FONT = new Font( "Arial", Font.BOLD, 13 );
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
	final ActionEvent BACK_BUTTON_PRESSED = new ActionEvent(this, 9, "back");
	final ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent(this, 10, "next");
	final ActionEvent EXIT_BUTTON_PRESSED = new ActionEvent(this, 11, "exit");


	//////////////////////////
	private JTextField projectFField= new JTextField();
	private JTextField projectNField = new JTextField();
	private JTextField projectDField= new JTextField();
	private JTextField trainingImageP = new JTextField();

	private JComboBox<ProjectType> projectList;
	private JFrame mainFrame;
	private JPanel cardPanel;
	private CardLayout cardLayout;
	private ProjectManager projectManager;
	private JButton nextButton;
	private JButton exitButton;

	int frameWidth = 600; // width
	int frameHeight = 450; // height

	/**
	 *
	 * @param projectManager
	 */
	public CreateOpenProjectGUI(ProjectManager projectManager) {
		this.projectManager = projectManager;
		projectList = new JComboBox<>(ProjectType.values());
	}

	@Override
	public void run() {
		mainFrame = new JFrame();
		mainFrame.getContentPane().setBackground(Color.GRAY);
		mainFrame.setSize(frameWidth, frameHeight);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent the default close operation

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

		cardPanel = new JPanel(new CardLayout());
		cardLayout = (CardLayout) cardPanel.getLayout();

		JPanel controlFrame = new JPanel();
		controlFrame.setLayout(null);
		controlFrame.setBackground(Color.GRAY);

		JLabel label = new JLabel("Active Segmentation");
		label.setFont(largeFONT);
		label.setBounds(135, 115, 450, 100);
		label.setForeground(Color.ORANGE);
		controlFrame.add(label);

		controlFrame.add(addButton("Create Project", createImageIcon("addProject.png", "add"), 77, 215, 210, 60, CREATE_BUTTON_PRESSED));
		controlFrame.add(addButton("Open Project", createImageIcon("openProject.png", "add"), 307, 215, 200, 60, OPEN_BUTTON_PRESSED));

		nextButton = addButton("Next", createImageIcon("next.png", "next"), 350, 360, 100, 30, NEXT_BUTTON_PRESSED);
		nextButton.setVisible(false); // Initially hide the nextButton
		controlFrame.add(nextButton);

		exitButton = addButton("Exit", null, 460, 360, 100, 30, EXIT_BUTTON_PRESSED); // Exit button
		exitButton.setVisible(false);
		controlFrame.add(exitButton);

		controlFrame.setLocation(0, 0);
		mainFrame.add(controlFrame);

		cardPanel.add(controlFrame, "mainPanel");
		cardPanel.add(createProjectPanel(), "createProjectPanel");
		mainFrame.add(cardPanel);
		mainFrame.setVisible(true);
	}

	private static File currentDir=null;

	private void doAction( final ActionEvent event ){
		if(event ==CREATE_BUTTON_PRESSED ){
			cardLayout.show(cardPanel, "createProjectPanel");
		}

		if(event ==OPEN_BUTTON_PRESSED ){
			JFileChooser fileChooser = new JFileChooser();

			// For Directory
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int rVal = fileChooser.showOpenDialog(null);
			if (currentDir!=null)
				fileChooser.setSelectedFile(currentDir);

			if (rVal == JFileChooser.APPROVE_OPTION) {
				currentDir = fileChooser.getSelectedFile();
				String file = currentDir.toString();
				if (projectManager.loadProject(file)) {
					System.out.println(" GuiPanel ");

					// Updates mainFrame by replacing its content with the main panel of a new GuiPanel instance
					activeSegmentation.gui.GuiPanel guiPanel = new activeSegmentation.gui.GuiPanel(projectManager);
					mainFrame.getContentPane().removeAll();
					mainFrame.getContentPane().add(guiPanel.getMainPanel());
					mainFrame.revalidate();
					mainFrame.repaint();

//					UIPanel frame=new UIPanel(projectManager);
//					frame.setVisible(true);
				}
				else
					IJ.error("Not a project file!");
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
			//TODO what do we do here?
			//int rVal = fileChooser.showOpenDialog(null);
			//if (rVal == JFileChooser.APPROVE_OPTION) {
			//pluginsDir.setText(fileChooser.getSelectedFile().toString());
			//}
		}

		if (event == CANCEL_BUTTON_PRESSED) {
			// Reset fields or perform any necessary cleanup
			projectNField.setText("");
			projectDField.setText("");
			projectFField.setText("");
			trainingImageP.setText("");
			projectList.setSelectedIndex(0); // Reset project type selection if needed

			cardLayout.show(cardPanel, "mainPanel"); // Go back to the main panel
			nextButton.setVisible(false);
			exitButton.setVisible(false);
		}

		// Creating project structure
		if(event== FINISH_BUTTON_PRESSED){
			String projectName=projectNField.getText();
			String projectDirectory=projectFField.getText();
			String projectDescription=projectDField.getText();
			String trainingImage=trainingImageP.getText();

			String projectType=projectList.getSelectedItem().toString();
			//System.out.println(projectName+"--"+ projectType);

			// Check if the project name field is empty
			if (projectName==null || projectName.isEmpty()) {
				// Display an error message to the user
				IJ.error("Project name cannot be empty.");
				return;
			}

			// Check if project directory is empty or same as training image directory
			if (projectDirectory == null || projectDirectory.isEmpty() || projectDirectory.equalsIgnoreCase(trainingImage)) {
				IJ.error("Project directory cannot be empty and should not be the same as the training image directory.");
				return;
			}

			// Check if training image directory is empty and no image is currently open
			if ((null == WindowManager.getCurrentImage() && (trainingImage == null || trainingImage.isEmpty()))) {
				IJ.error("Training folder cannot be empty and should contain either a tif file or folder with tiff images.");
				return;
			}

			// TODO change the signaling mechanism
			String message=projectManager.createProject(projectName, projectType, projectDirectory, projectDescription,
					trainingImage);
			if("DONE".equalsIgnoreCase(message)) {
				// Updates mainFrame by replacing its content with the main panel of a new GuiPanel instance
				activeSegmentation.gui.GuiPanel guiPanel = new activeSegmentation.gui.GuiPanel(projectManager);
				mainFrame.getContentPane().removeAll();
				mainFrame.getContentPane().add(guiPanel.getMainPanel());
				mainFrame.revalidate();
				mainFrame.repaint();
			}
		}

		if (event == BACK_BUTTON_PRESSED) {
			cardLayout.show(cardPanel, "mainPanel"); // Switch back to the main panel
			nextButton.setVisible(true);
			exitButton.setVisible(true);
		}

		if (event == NEXT_BUTTON_PRESSED) {
			cardLayout.show(cardPanel, "createProjectPanel"); // Switch to the create project panel
		}

		// Confirm Exit
		if (event == EXIT_BUTTON_PRESSED) {
			int response = javax.swing.JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to exit?", "Confirm Exit",
					javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
			if (response == javax.swing.JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
	}

	private JPanel createProjectPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(panelColor);

		JLabel label = new JLabel("Create Project");
		label.setFont(largeFONT);
		label.setBounds(50, 10, 450, 100);
		label.setForeground(Color.ORANGE);
		panel.add(label);

		JLabel projectName = new JLabel("Project Name *:");
		projectName.setFont(mediumFONT);
		projectName.setBounds(50, 110, 200, 30);
		panel.add(projectName);

		projectNField.setColumns(20);
		projectNField.setBounds(200, 110, 250, 30);
		panel.add(projectNField);

		JLabel projectDesc = new JLabel("Project Desc :");
		projectDesc.setFont(mediumFONT);
		projectDesc.setBounds(50, 150, 200, 30);
		panel.add(projectDesc);

		projectDField.setColumns(20);
		projectDField.setBounds(200, 150, 250, 30);
		panel.add(projectDField);

		JLabel projectType = new JLabel("Project Type :");
		projectType.setFont(mediumFONT);
		projectType.setBounds(50, 190, 250, 30);
		panel.add(projectType);

		projectList.setSelectedIndex(0);
		projectList.setBounds(200, 190, 250, 30);
		panel.add(projectList);

		JLabel projectFolder = new JLabel("Project Folder *:");
		projectFolder.setFont(mediumFONT);
		projectFolder.setBounds(50, 230, 200, 30);
		panel.add(projectFolder);

		projectFField.setColumns(200);
		projectFField.setBounds(200, 230, 250, 30);
		panel.add(projectFField);
		panel.add(addButton("Browse", null, 460, 230, 100, 30, BROWSE_BUTTON_PRESSED));

		if (null == WindowManager.getCurrentImage()) {
			JLabel trainingImage = new JLabel("Training Image *:");
			trainingImage.setFont(mediumFONT);
			trainingImage.setBounds(50, 270, 200, 30);
			panel.add(trainingImage);

			trainingImageP.setColumns(200);
			trainingImageP.setBounds(200, 270, 250, 30);
			panel.add(trainingImageP);
			panel.add(addButton("Folder", null, 240, 310, 100, 30, TRAININGF_BUTTON_PRESSED));
			panel.add(addButton("Image Stack", null, 350, 310, 100, 30, Tiff_BUTTON_PRESSED));
		}

		panel.add(addButton("Finish", null, 80, 360, 120, 30, FINISH_BUTTON_PRESSED));
		panel.add(addButton("Cancel", null, 240, 360, 100, 30, CANCEL_BUTTON_PRESSED));
		panel.add(addButton("Back", null, 350, 360, 100, 30, BACK_BUTTON_PRESSED));
		panel.add(addButton("Exit", null, 460, 360, 100, 30, EXIT_BUTTON_PRESSED));

		return panel;
	}

	private JButton addButton( final String label, final ImageIcon icon, final int x,
							   final int y, final int width, final int height,final ActionEvent action)
	{
		final JButton button =  new JButton(label, icon);
		button.setFont( labelFONT );
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBackground(buttonBGColor);
		button.setForeground(Color.WHITE);
		button.setBounds( x, y, width, height );
		button.addActionListener( new ActionListener()		{
			@Override
			public void actionPerformed( final ActionEvent e )	{

				doAction(action);
			}
		} );

		return button;
	}


	private ImageIcon createImageIcon(String path, String description) {
		URL imgURL = CreateOpenProjectGUI.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			//System.err.println("Couldn't find file: " + path);
			return null;
		}
	}


}
