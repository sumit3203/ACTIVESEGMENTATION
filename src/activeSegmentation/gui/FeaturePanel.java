package activeSegmentation.gui;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;


import activeSegmentation.ASCommon;
import activeSegmentation.IUtil;
import activeSegmentation.LearningType;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.util.GuiUtil;

import static  activeSegmentation.ProjectType.*;
/**
 *  Main interaction
 * @author Sumit Vohra, Dimiter Prodanov
 *
 */
public class FeaturePanel extends ImageWindow implements Runnable, ASCommon, IUtil {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/** opacity   of the result overlay image */
	public static final float resultOpacity = .33f;
	/** alpha composite for the result overlay image */
	private final Composite overlayAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, resultOpacity );
	
	public static final float roiOpacity = .5f;
	/** alpha composite for the roi overlay image */
	private final Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, roiOpacity );
	
	private FeatureManager featureManager;
	

	private ImageOverlay resultOverlay;
	private LUT overlayLUT;
	/** flag to display the overlay image */
	private boolean showColorOverlay=false;
	private ImagePlus classifiedImage=null;
	// Create overlay LUT
	private byte[] red = new byte[ 256 ];
	private byte[] green = new byte[ 256 ];
	private byte[] blue = new byte[ 256 ];

	private Map<String, JList<String>> exampleList;
	//private Map<String, JList<String>> allexampleList;

	/** array of ROI list overlays to paint the transparent ROIs of each class */
	private Map<String,RoiListOverlay> roiOverlayList;

	/** Used only during classification setting*/
	private Map<String,Integer> predictionResultClassification;

	
	/*
	 *  the files must be in the resources/feature folder
	 */
	private static final Icon uploadIcon = new ImageIcon(FeaturePanel.class.getResource("upload.png"));
	private static final Icon downloadIcon = new ImageIcon(FeaturePanel.class.getResource("download.png"));
 

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	private ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	private ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent( this, 1, "Previous" );
	private ActionEvent ADDCLASS_BUTTON_PRESSED = new ActionEvent( this, 2, "AddClass" );
	private ActionEvent UPDATECLASS_BUTTON_PRESSED= new ActionEvent( this, 3, "SaveLabel" );
	private ActionEvent DELETE_BUTTON_PRESSED = new ActionEvent( this, 4, "DeleteClass" );
	private ActionEvent TRAIN_BUTTON_PRESSED  = new ActionEvent( this, 5, "TRAIN" );
	private ActionEvent SAVE_BUTTON_PRESSED  = new ActionEvent( this, 6, "SAVEDATA" );
	private ActionEvent TOGGLE_BUTTON_PRESSED = new ActionEvent( this, 7, "TOGGLE" );
	private ActionEvent DOWNLOAD_BUTTON_PRESSED = new ActionEvent( this, 8, "DOWNLOAD" );
	private ActionEvent MASKS_BUTTON_PRESSED = new ActionEvent( this, 9, "MASKS" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	private ActionEvent SNAP_BUTTON_PRESSED = new ActionEvent( this, 10, "Snap" );
	
	private ActionEvent SAVE_SESSION_BUTTON_PRESSED = new ActionEvent(this, 11, "Save Session Data");


	private ImagePlus displayImage;
	/** Used only in classification setting, in segmentation we get from feature manager*/
 
	private JPanel imagePanel,classPanel,roiPanel;
	private JTextField imageNum;
	private JLabel total;
	private List<JCheckBox> jCheckBoxList;
	private Map<String,JTextArea> jTextList;
	private JComboBox<LearningType> learningType;
	private JFrame frame;
	private JProgressBar progressBar;
	private JLabel trainingStatus;


	private String ltype="TRAINING";


	/*
	 * constructor 
	 */
	public FeaturePanel(FeatureManager featureManager) {		
		super(featureManager.getCurrentImage());
		this.featureManager = featureManager;
		this.displayImage= featureManager.getCurrentImage();
		this.jCheckBoxList= new ArrayList<>();
		this.jTextList= new HashMap<>();
		this.exampleList = new HashMap<>();
		//this.allexampleList = new HashMap<>();
		roiOverlayList = new HashMap<>();		
		super.setVisible(false);
		showPanel();
	}

	@Override
	public void run() {
		if (!isRunning)
			showPanel();
		
	}
	
	boolean isRunning=false;
	
	
	@Override
	public void setVisible(boolean vis) {
		frame.setVisible(vis);
	}

	public void showPanel() {
		frame = new JFrame("Active Segmentation - Feature Marking");
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(FeaturePanel.class.getResource("logo.png")));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		imagePanel = new JPanel();
		roiPanel = new JPanel();
		classPanel = new JPanel();

		/*
		 * Image panel (left side)
		 */
		imagePanel.setLayout(new BorderLayout());
		if (displayImage != null) {
			ic = new SimpleCanvas(displayImage);
			ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
			setOverlay();
			imagePanel.add(ic, BorderLayout.CENTER);
		}
		imagePanel.setBackground(Color.GRAY);
		imagePanel.setPreferredSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));

		/*
		 * Right-side controls panel (stacked vertically)
		 */
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		/*
		 * Class panel
		 */
		classPanel.setPreferredSize(new Dimension(350, 100));
		classPanel.setBorder(BorderFactory.createTitledBorder("Classes"));
		JScrollPane classScrolPanel = new JScrollPane(classPanel);
		classScrolPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		classScrolPanel.setPreferredSize(new Dimension(360, 100));
		classScrolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
		if (featureManager != null) {
			addClassPanel();
		} else {
			// Mock data for WindowBuilder
			classPanel.setPreferredSize(new Dimension(340, 110));
			roiPanel.setPreferredSize(new Dimension(350, 175 * 2));
			addButton(new JButton(), "Add Class", null, 0, 0, 0, 0, classPanel, ADDCLASS_BUTTON_PRESSED, null);
			addButton(new JButton(), "Update Class", null, 0, 0, 0, 0, classPanel, UPDATECLASS_BUTTON_PRESSED, null);
			addButton(new JButton(), "Delete Class", null, 0, 0, 0, 0, classPanel, DELETE_BUTTON_PRESSED, null);
			addClasses("class_1", "Background", Color.RED);
			addClasses("class_2", "Foreground", Color.GREEN);
		}
		rightPanel.add(classScrolPanel);
		rightPanel.add(Box.createVerticalStrut(5));

		/*
		 * Learning panel (navigation + compute + progress)
		 */
		JPanel features = new JPanel();
		features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
		features.setBorder(BorderFactory.createTitledBorder("Learning"));
		features.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

		// Navigation row: << imageNum / total >>
		JPanel navPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));
		addButton(new JButton(), "<<", null, 0, 0, 0, 0, navPanel, PREVIOUS_BUTTON_PRESSED, null)
				.setToolTipText("Previous image (\u2190 key)");
		imageNum = new JTextField();
		imageNum.setColumns(5);
		JLabel dasedLine = new JLabel("/");
		dasedLine.setFont(new Font("Arial", Font.PLAIN, 15));
		dasedLine.setForeground(Color.BLACK);
		total = new JLabel("Total");
		total.setFont(new Font("Arial", Font.PLAIN, 15));
		total.setForeground(Color.BLACK);
		imageNum.setText(featureManager != null ? Integer.toString(featureManager.getCurrentSlice()) : "1");
		total.setText(featureManager != null ? Integer.toString(featureManager.getTotalSlice()) : "1");
		navPanel.add(imageNum);
		navPanel.add(dasedLine);
		navPanel.add(total);
		addButton(new JButton(), ">>", null, 0, 0, 0, 0, navPanel, NEXT_BUTTON_PRESSED, null)
				.setToolTipText("Next image (\u2192 key)");
		features.add(navPanel);

		// Compute row: Train, Save, Overlay, Masks, Snap
		JPanel computePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));
		addButton(new JButton(), "Train", null, 0, 0, 0, 0, computePanel, TRAIN_BUTTON_PRESSED, null)
				.setToolTipText("Train the classifier (Ctrl+T)");
		addButton(new JButton(), "Save", null, 0, 0, 0, 0, computePanel, SAVE_BUTTON_PRESSED, null)
				.setToolTipText("Save regions of interest (Ctrl+S)");
		addButton(new JButton(), "Overlay", null, 0, 0, 0, 0, computePanel, TOGGLE_BUTTON_PRESSED, null)
				.setToolTipText("Toggle result overlay (Ctrl+O)");
		addButton(new JButton(), "Masks", null, 0, 0, 0, 0, computePanel, MASKS_BUTTON_PRESSED, null)
				.setToolTipText("Show classification masks");
		addButton(new JButton(), "Snap", null, 0, 0, 0, 0, computePanel, SNAP_BUTTON_PRESSED, null)
				.setToolTipText("Take a screenshot of the canvas");
		features.add(computePanel);

		// Progress bar for training feedback
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Training in progress...");
		progressBar.setVisible(false);
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		features.add(progressBar);

		// Training status label
		trainingStatus = new JLabel(" ");
		trainingStatus.setForeground(new Color(0, 100, 200));
		trainingStatus.setFont(new Font("Arial", Font.BOLD, 12));
		trainingStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
		features.add(trainingStatus);

		// Session save
		JPanel sessionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));
		addButton(new JButton(), "Save Session Data", null, 0, 0, 0, 0, sessionPanel,
				SAVE_SESSION_BUTTON_PRESSED, null).setToolTipText("Save current session data");
		features.add(sessionPanel);

		rightPanel.add(features);
		rightPanel.add(Box.createVerticalStrut(5));

		/*
		 * Training/testing panel (classification projects only)
		 */
		if (featureManager == null || featureManager.getProjectType() == ProjectType.CLASSIF) {
			JPanel dataJPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
			learningType = new JComboBox<>(LearningType.values());
			learningType.setVisible(true);
			learningType.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (featureManager.getProjectType() == ProjectType.CLASSIF) {
						if (showColorOverlay) {
							updateGui();
							updateResultOverlay(null);
						} else
							updateGui();
					} else
						updateGui();

					ltype = learningType.getSelectedItem().toString();
					System.out.println("ltype: " + ltype);
				}
			});

			learningType.setSelectedIndex(0);
			learningType.setFont(panelFONT);
			learningType.setBackground(Color.GRAY);
			learningType.setForeground(Color.BLUE);
			learningType.setToolTipText("Select training or testing mode");
			dataJPanel.add(learningType);
			dataJPanel.setBackground(Color.GRAY);
			dataJPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
			rightPanel.add(dataJPanel);
			rightPanel.add(Box.createVerticalStrut(5));
		}

		/*
		 * ROI panel
		 */
		roiPanel.setBorder(BorderFactory.createTitledBorder("Regions Of Interest"));
		JScrollPane scrollPane = new JScrollPane(roiPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(360, 250));
		rightPanel.add(scrollPane);

		/*
		 * Main layout: image on left, controls on right via JSplitPane
		 */
		JScrollPane rightScrollPane = new JScrollPane(rightPanel);
		rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, rightScrollPane);
		splitPane.setResizeWeight(0.6);
		splitPane.setDividerLocation(IMAGE_CANVAS_DIMENSION + 20);
		splitPane.setOneTouchExpandable(true);
		frame.add(splitPane);

		/*
		 * Keyboard shortcuts
		 */
		frame.setFocusable(true);
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						doAction(PREVIOUS_BUTTON_PRESSED);
						break;
					case KeyEvent.VK_RIGHT:
						doAction(NEXT_BUTTON_PRESSED);
						break;
					case KeyEvent.VK_T:
						if (e.isControlDown()) doAction(TRAIN_BUTTON_PRESSED);
						break;
					case KeyEvent.VK_S:
						if (e.isControlDown()) doAction(SAVE_BUTTON_PRESSED);
						break;
					case KeyEvent.VK_O:
						if (e.isControlDown()) doAction(TOGGLE_BUTTON_PRESSED);
						break;
				}
			}
		});

		/*
		 * Frame setup
		 */
		frame.pack();
		frame.setSize(largeframeWidth, largeframeHight);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		WindowManager.addWindow(this);
		updateGui();
		isRunning = true;
	}


	private void addClassPanel(){
		classPanel.removeAll();
		roiPanel.removeAll();
		jCheckBoxList.clear();
		jTextList.clear();
		int classes=featureManager.getNumOfClasses();
		IJ.log(Integer.toString(classes));
		if(classes%3==0){
			int tempSize=classes/3;
			classPanel.setPreferredSize(new Dimension(340, 80+30*tempSize));	
		}
		roiPanel.setPreferredSize(new Dimension(350, 175*classes));
		addButton(new JButton(), "Add Class",null , 630, 20, 130, 20,classPanel,ADDCLASS_BUTTON_PRESSED,null );
		addButton(new JButton(), "Update Class",null , 630, 20, 130, 20,classPanel,UPDATECLASS_BUTTON_PRESSED,null );
		addButton(new JButton(), "Delete Class",null , 630, 20, 130, 20,classPanel,DELETE_BUTTON_PRESSED,null );
		for(String key: featureManager.getClassKeys()){
			String label=featureManager.getClassLabel(key);
			Color color= featureManager.getClassColor(key);
			addClasses(key,label,color);
			addSidePanel(color,key,label);
		}		
	}

	/**
	 * Draws the painted traces on the display image
	 */
	private void drawExamples(){
	//	imp.setHideOverlay(true);
		final String type=ltype ;//learningType.getSelectedItem().toString();
		for(String key: featureManager.getClassKeys()){
			ArrayList<Roi> rois=(ArrayList<Roi>) featureManager.
					getRoiList(key, type, featureManager.getCurrentSlice());
			
			RoiListOverlay lsto=roiOverlayList.get(key);
			
			String[] parts=key.split("_|-");
			if (parts.length>1) {
				System.out.println("lab "+ parts[1]);
				String label=parts[1];
				lsto.setLabel(label);
			}
			
			lsto.setColor(featureManager.getClassColor(key));
			lsto.setRoi(rois);
			//System.out.println("roi draw"+ key);
			
		}
		//imp.setHideOverlay(false);
	
		displayImage.updateAndDraw();
	}
	
	
	
	private void addSidePanel(Color color,String key,String label){
		JPanel panel= new JPanel();
		JList<String> current=GuiUtil.getFilterJList();

		current.setForeground(color);
		exampleList.put(key,current);
		JList<String> all=GuiUtil.getFilterJList();
		all.setForeground(color);
		//allexampleList.put(key,all);	
		RoiListOverlay roiOverlay = new RoiListOverlay();
		roiOverlay.setComposite( transparency050 );
		((OverlayedImageCanvas)ic).addOverlay(roiOverlay);
		
		
		roiOverlayList.put(key,roiOverlay);
		JPanel buttonPanel= new JPanel();
		buttonPanel.setName(key);
		ActionEvent addbuttonAction= new ActionEvent(buttonPanel, 1,"AddButton");
		ActionEvent uploadAction= new ActionEvent(buttonPanel, 2,"UploadButton");
		ActionEvent downloadAction= new ActionEvent(buttonPanel, 3,"DownloadButton");
		JButton addButton= new JButton();
		addButton.setName(key);
		JButton upload= new JButton();
		upload.setName(key);
		JButton download= new JButton();
		download.setName(key);
		addButton(addButton, label, null, 605,280,350,250, buttonPanel, addbuttonAction, null);
		addButton(upload, null, uploadIcon, 605,280,350,250, buttonPanel, uploadAction, null);
		addButton(download, null, downloadIcon, 605,280,350,250, buttonPanel, downloadAction, null);
		roiPanel.add(buttonPanel);
		panel.add(GuiUtil.addScrollPanel(exampleList.get(key),new Dimension(300, 100)));
		//panel.add(GuiUtil.addScrollPanel(allexampleList.get(key),null));
		roiPanel.add(panel );
		exampleList.get(key).addMouseListener(mouseListener);
		//allexampleList.get(key).addMouseListener(mouseListener);
	}

	private void addClasses(String key , String label, Color color){
		JCheckBox  checkBox = new JCheckBox();
		checkBox.setName(key);
		jCheckBoxList.add(checkBox);
		JTextArea textArea= new JTextArea();
		textArea.setName(key);
		textArea.setText(label );
		jTextList.put(key, textArea);
		classPanel.add(checkBox);
		classPanel.add(textArea);
		JButton button= new JButton();
		button.setBackground(color);
		button.setName(key);
		ActionEvent colorAction= new ActionEvent(button, color.getRGB(),"ColorButton");
		addAction(button, colorAction);		
		classPanel.add(button);
	}

	private void addAction(JButton button ,final  ActionEvent action){
		 button.addActionListener( new ActionListener()	{
			@Override
			public void actionPerformed( final ActionEvent e )	{
				doAction(action);
			}
		} );
	 
	}
	
	private void loadImage(ImagePlus image){
		this.displayImage=image;
		setImage(this.displayImage);
		updateImage(this.displayImage);
	}

	public void validateFrame(){
		frame.invalidate();
		frame.revalidate();
		frame.repaint();
	}
	
	private int classCnt=0;

	public void doAction( final ActionEvent event ) {
		if(event== ADDCLASS_BUTTON_PRESSED){
			featureManager.addClass("class_"+classCnt);
			classCnt++;
			addClassPanel();
			validateFrame();
			updateGui();
		} // end if
		if(event==DELETE_BUTTON_PRESSED){          

			System.out.println(featureManager.getNumOfClasses());
			System.out.println(jCheckBoxList.size());
			int totalDel=0;
			
			for (JCheckBox checkBox : jCheckBoxList) 
				if (checkBox.isSelected()) 
					totalDel++;
		
			if(featureManager.getNumOfClasses()-totalDel<2) 
             JOptionPane.showMessageDialog(null, "There should be minimum two classes");
			else {
				for (JCheckBox checkBox : jCheckBoxList) {
					if (checkBox.isSelected()) 
						featureManager.deleteClass(checkBox.getName());
					classCnt--;
				}
				addClassPanel();
				validateFrame();
				updateGui();
			}	

		} // end if

		if(event==SAVE_BUTTON_PRESSED){
			featureManager.saveFeatureMetadata();
			IJ.log("Successfully saved regions of interest");
			//JOptionPane.showMessageDialog(null, "Successfully saved regions of interest");
		} //end if

		/* execute only if a project is of CLSSIF type
		if (event == SAVE_SESSION_BUTTON_PRESSED) {
			// Handle the save session data action
			Connection conn = createConnection();
			System.out.println(conn);
			System.out.println("success connection");
			try {
				featureManager.saveSessionDetails(conn);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		// updaing
		if(event==UPDATECLASS_BUTTON_PRESSED){
			for (JCheckBox checkBox : jCheckBoxList) {				
				//System.out.println(checkBox.getText());
				String key=checkBox.getName();
				featureManager.setClassLabel(key,jTextList.get(key).getText() );
				
			}
			addClassPanel();
			validateFrame();
			updateGui();
		} // end if
		
		if(event == PREVIOUS_BUTTON_PRESSED){			
			ImagePlus image=featureManager.getPreviousImage();
			imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
			loadImage(image);
			
			if (showColorOverlay){
				if(featureManager.getProjectType()==ProjectType.CLASSIF) 
					classifiedImage = null;
				else 
					classifiedImage=featureManager.getClassifiedImage();		
				updateResultOverlay(classifiedImage);
			}

			// force limit size of image window
			if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}			
			updateGui();
		} // end if
		
		if(event==NEXT_BUTTON_PRESSED  ){			
			ImagePlus image=featureManager.getNextImage();
			imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
			loadImage(image);
			if (showColorOverlay){
				if(featureManager.getProjectType()==ProjectType.CLASSIF)
					classifiedImage = null;
				else
					classifiedImage=featureManager.getClassifiedImage();
				updateResultOverlay(classifiedImage);
			}

			// force limit size of image window
			if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}
			//imagePanel.add(ic);
			updateGui();
		} // end if
		
		if(event==TRAIN_BUTTON_PRESSED){
			
			//toggleOverlay();
			if(featureManager.getProjectType()==ProjectType.CLASSIF) {
				// it means new round of training, so set result setting to false
				showColorOverlay = false;
				// removing previous markings and reset things
				predictionResultClassification = null;
				displayImage.setOverlay(null);
				progressBar.setVisible(true);

				new javax.swing.SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						featureManager.compute();
						return null;
					}
					@Override
					protected void done() {
						progressBar.setIndeterminate(false);
						progressBar.setString("Training complete!");
						predictionResultClassification = featureManager.getClassificationResultMap();
						classifiedImage = null;
						updateGui();
						new javax.swing.Timer(2000, e -> {
							progressBar.setVisible(false);
							progressBar.setIndeterminate(true);
							progressBar.setString("Training in progress...");
							((javax.swing.Timer) e.getSource()).stop();
						}).start();
					}

				}.execute();

			}

			//segmentation setting
			else {
				// remove result overlay
				displayImage.setOverlay(null);
				displayImage.updateAndDraw();
				progressBar.setVisible(true);

				new javax.swing.SwingWorker<ImagePlus, Void>() {
					@Override
					protected ImagePlus doInBackground() {
						return featureManager.compute();
					}
					@Override
					protected void done() {
						try {
							classifiedImage = get();
							progressBar.setIndeterminate(false);
							progressBar.setString("Training complete!");
							toggleOverlay();
							new javax.swing.Timer(2000, e -> {
								progressBar.setVisible(false);
								progressBar.setIndeterminate(true);
								progressBar.setString("Training in progress...");
								((javax.swing.Timer) e.getSource()).stop();
							}).start();
						} catch (Exception ex) {
							progressBar.setVisible(false);
							progressBar.setString("Training in progress...");
							progressBar.setIndeterminate(true);
							javax.swing.JOptionPane.showMessageDialog(null, 
								"Training failed. Please ensure filters are computed and ROIs are drawn.",
								"Training Error", javax.swing.JOptionPane.ERROR_MESSAGE);
						}

					}

				}.execute();

			}
			IJ.log("computing");

			
		} //end if
		
		if(event==TOGGLE_BUTTON_PRESSED){
			toggleOverlay();
		} // end if
		
		if(event==DOWNLOAD_BUTTON_PRESSED){

			ImagePlus image=featureManager.stackedClassifiedImage();
			image.show();
			
		} //end if
		
		if(event==MASKS_BUTTON_PRESSED){
			System.out.println("masks ");
			if (classifiedImage==null) {
				classifiedImage=featureManager.getClassifiedImage();//compute();
			}
			getMask();
			 
		} //end if
		
		if (event==SNAP_BUTTON_PRESSED) {			 
			GuiUtil.grabWindow(ic);

		}
		
		if("ColorButton".equals(event.getActionCommand())){	
			String key=((Component)event.getSource()).getName();
			Color c;
			c = JColorChooser.showDialog( new JFrame(),
					"CLASS COLOR", featureManager.getClassColor(key));

			((Component)event.getSource()).setBackground(c);
			featureManager.updateColor(key, c);
			updateGui();
		}// end if
		
		if("AddButton".equals(event.getActionCommand())){	
			String key=((Component)event.getSource()).getName();
			final Roi r = displayImage.getRoi();
			// key = "roi";
			if (null == r)
				return;
			displayImage.killRoi();
			System.out.println("adding roi "+key);
			
			if(featureManager.addExample(key,r,ltype,featureManager.getCurrentSlice()))
				updateGui();
			else 
			    JOptionPane.showMessageDialog(null, "Another class already contains this roi");	
	
			
		} //end if
		
		if("UploadButton".equals(event.getActionCommand())){	
			String key=((Component)event.getSource()).getName();
			uploadExamples(key);
			updateGui();
		}//end if
		
		if("DownloadButton".equals(event.getActionCommand())){	
			String key=((Component)event.getSource()).getName();
			downloadRois(key);
		}


	}


	/**
	 * 
	 */
	private void getMask() {
		ImagePlus mask=classifiedImage.duplicate();
		NamedLUT nlut=new NamedLUT( featureManager.getColors());
		mask.setLut(nlut.getLUT());
		mask.show();
	}


	/**
	 * Toggle between overlay and original image with markings
	 */
	private void toggleOverlay()
	{
		if(featureManager.getProjectType()== ProjectType.SEGM) {
			showColorOverlay = !showColorOverlay;			
			if (showColorOverlay && ( classifiedImage!=null)){
				updateResultOverlay(classifiedImage);
			}
			else{
				resultOverlay.setImage(null);
				displayImage.updateAndDraw();
			}
		}

		// classification setting, no classified image
		else {			
			showColorOverlay = !showColorOverlay;
			// user wants to see results
			if(showColorOverlay) {
				updateResultOverlay(classifiedImage);
			}

			// user wants to see original rois, not the results
			else {

				// remove result overlay
				displayImage.setOverlay(null);
				displayImage.updateAndDraw();

				//just show examples drawn by user
				updateGui();
			}
		}		
	}

	public void updateResultOverlay(ImagePlus classifiedImage)	{
		if(featureManager.getProjectType()==ProjectType.SEGM) {
			ImageProcessor overlay = classifiedImage.getProcessor().duplicate();
			overlay = overlay.convertToByte(false);
			setLut(featureManager.getColors());
			overlay.setColorModel(overlayLUT);
			resultOverlay.setImage(overlay);
			displayImage.updateAndDraw();
		}

		if(featureManager.getProjectType()== ProjectType.CLASSIF) {
			// remove previous overlay
			displayImage.setOverlay(null);
			displayImage.updateAndDraw();

			//get current slice
			int currentSlice = featureManager.getCurrentSlice();			
			Font font = new Font("Arial", Font.PLAIN, 38);           
			Overlay overlay = new Overlay();		 		 			 			
			ArrayList<Roi> rois;
			for(String classKey:featureManager.getClassKeys()) {
				//returns rois of current image slice of given class, current slice is updated internally
				rois = (ArrayList<Roi>) featureManager.getRoiList(classKey,learningType.getSelectedItem().toString(), featureManager.getCurrentSlice());
				if(rois!=null) {					
					for (Roi roi:rois) {
						int pred = predictionResultClassification.get(roi.getName());
						TextRoi textroi = new TextRoi(roi.getBounds().x,roi.getBounds().y,
								roi.getFloatWidth(),roi.getFloatHeight(),Integer.toString(pred),font);
						textroi.setFillColor(roi.getFillColor());
						//textroi.setNonScalable(true);
						textroi.setPosition(currentSlice);
						overlay.add(textroi);
					}
				}
			}
			// add result overlay
			displayImage.setOverlay(overlay);			
			displayImage.updateAndDraw();				
		}
	}

	public void setLut(List<Color> colors ){
		int i=0;
		for(Color color: colors){
			red[i] = (byte) color.getRed();
			green[i] = (byte) color.getGreen();
			blue[i] = (byte) color.getBlue();
			i++;
		}
		overlayLUT = new LUT(red, green, blue);
	}

	// Create database connection
    private Connection createConnection() {
        return connStart("C:\\Users\\aarya\\Desktop\\gsoc23\\ACTIVESEGMENTATION\\sqliteTest.db");
    }

    private Connection connStart(String dbName) {
		//connecting to database
    	Connection conn = null;
		String driver="org.sqlite.JDBC";
    	String dbUrl="jdbc:sqlite:"+ dbName;
        try {
            //Class.forName("org.gjt.mm.mysql.Driver");
            Class.forName(driver);
        } catch(Exception ex) {
            IJ.log("Can't find Database driver class: " + ex);
            return null;
        }
        try {
            conn = DriverManager.getConnection(dbUrl);
            IJ.log("Connected to " + dbUrl);
            return conn;
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
            return null;
        }
    }
	
	private void updateGui(){
		try{
			//displayImage.killRoi();
			updateExampleLists();
			drawExamples();
			//updateallExampleLists();

			ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
			ic.repaint();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private LearningType defualtLT = LearningType.TRAINING;
	

	/*
	  private void updateExampleLists() { 
		  LearningType type=(LearningType)   learningType.getSelectedItem(); 
		  updateExampleLists(featureManager, type,  exampleList); 
	  }
	 */
	
 
	
	  private void updateExampleLists() { 
		 if  (featureManager.getProjectType()==ProjectType.CLASSIF) { 
			  LearningType   ltype=(LearningType) learningType.getSelectedItem();
			  updateExampleLists(featureManager, ltype, exampleList); } 
		 else {
			 updateExampleLists(featureManager, defualtLT, exampleList); 
		 } 
	  }
	 
	
	private  MouseListener mouseListener = new MouseAdapter() {
		
		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			JList<?>  theList = ( JList<?>) mouseEvent.getSource();
			// what is this?
			if (mouseEvent.getClickCount() == 1) {
				int index = theList.getSelectedIndex();

				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					if (item.equalsIgnoreCase("")|| item.equalsIgnoreCase(" ") ) return;
					String[] arr= item.split(" ");
					//System.out.println("roi Id "+ arr[0].trim()+" "+ index);
					//int sliceNum=Integer.parseInt(arr[2].trim());
					//try {
						showSelected( arr[0].trim(),index);
					//} catch (ArrayIndexOutOfBoundsException ex ) {
					//	ex.printStackTrace();
					//}
				}
			}

			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				String type=ltype;// learningType.getSelectedItem().toString();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					if (item.equalsIgnoreCase("")|| item.equalsIgnoreCase(" ") ) return;
					//System.out.println("ITEM : "+ item);
					String[] arr= item.split(" ");
					//int classId= featureManager.getclassKey(arr[0].trim())-1;
					//try {
						featureManager.deleteExample(arr[0], Integer.parseInt(arr[1].trim()), type);
						updateGui();
					//} catch (ArrayIndexOutOfBoundsException ex ) {
					//	ex.printStackTrace();
					//}
				}
			}
		}
	};


	/**
	 * Select a list and deselect the others
	 * @param e item event (originated by a list)
	 * @param i list index
	 */
	private void showSelected(String classKey,int index ){
		displayImage.killRoi();
		//displayImage.setColor(Color.YELLOW);
		String type=ltype;// learningType.getSelectedItem().toString();
		System.out.println(classKey+"--"+index+"---"+type);
		final Roi newRoi = featureManager.getRoi(classKey, index,type);	
		System.out.println(newRoi);
		if (newRoi!=null) {
			displayImage.setRoi(newRoi);
			//activeRoi=newRoi;
		}
		updateGui();
	}  
	

	
	private JButton addButton(final JButton button ,final String label, final Icon icon, final int x,
			final int y, final int width, final int height,
			JComponent panel, final ActionEvent action,final Color color )	{
		panel.add(button);
		button.setText( label );
		button.setIcon( icon );
		button.setFont( panelFONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(buttonBGColor);
		button.setForeground(buttonColor);
		if(color!=null){
			button.setBackground(color);
		}
		button.setBounds( x, y, width, height );
		button.addActionListener( new ActionListener()	{
			@Override
			public void actionPerformed( final ActionEvent e )	{
				//System.out.println(e.toString());
				doAction(action);
			}
		});

		return button;
	}

	private void setOverlay(){
		resultOverlay = new ImageOverlay();
		resultOverlay.setComposite( overlayAlpha );
		((OverlayedImageCanvas)ic).addOverlay(resultOverlay);
	}

	private void downloadRois(String key) {
		String type=ltype;// learningType.getSelectedItem().toString();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			String name=fileChooser.getSelectedFile().toString();
			if(!name.endsWith(".zip")){
				name = name + ".zip";
			}

			featureManager.saveExamples(name, key,type, featureManager.getCurrentSlice());
		}
	}

	private void uploadExamples(String key) {
		String type=ltype; //learningType.getSelectedItem().toString();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			featureManager.uploadExamples(fileChooser.getSelectedFile().toString(),key,type, featureManager.getCurrentSlice());
		}
	}


}