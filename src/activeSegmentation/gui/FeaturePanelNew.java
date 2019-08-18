package activeSegmentation.gui;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
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

import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.LearningType;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureManagerNew;
import activeSegmentation.io.ProjectManagerImp;

public class FeaturePanelNew extends ImageWindow  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int IMAGE_CANVAS_DIMENSION = 560; //same width and height	
	private IFeatureManagerNew featureManager;
	/** opacity (in %) of the result overlay image */
	int overlayOpacity = 33;
	/** alpha composite for the result overlay image */
	Composite overlayAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity / 100f);
	private ImageOverlay resultOverlay;
	LUT overlayLUT;
	/** flag to display the overlay image */
	private boolean showColorOverlay=false;
	ImagePlus classifiedImage;
	// Create overlay LUT
	byte[] red = new byte[ 256 ];
	byte[] green = new byte[ 256 ];
	byte[] blue = new byte[ 256 ];

	private Map<String, JList> exampleList;
	private Map<String, JList> allexampleList;

	/** array of roi list overlays to paint the transparent rois of each class */
	private Map<String,RoiListOverlay> roiOverlayList;

	/** Used only during classification setting*/
	private Map<String,Integer> predictionResultClassification;

	final Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f );
	private static final Icon uploadIcon = new ImageIcon(FeaturePanelNew.class.getResource("upload.png"));
	private static final Icon downloadIcon = new ImageIcon(FeaturePanelNew.class.getResource("download.png"));
	public static final Font FONT = new Font( "Arial", Font.BOLD, 10 );

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent NEXT_BUTTON_PRESSED;
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	ActionEvent PREVIOUS_BUTTON_PRESSED ;
	ActionEvent ADDCLASS_BUTTON_PRESSED ;
	ActionEvent SAVECLASS_BUTTON_PRESSED ;
	ActionEvent DELETE_BUTTON_PRESSED;
	ActionEvent COMPUTE_BUTTON_PRESSED;
	ActionEvent SAVE_BUTTON_PRESSED;
	ActionEvent TOGGLE_BUTTON_PRESSED;
	ActionEvent DOWNLOAD_BUTTON_PRESSED;
	ItemEvent LEARNINGTYPE_BUTTON_PRESSED;

	private ImagePlus displayImage;
	/** Used only in classification setting, in segmentation we get from feature manager*/
	private ImagePlus tempClassifiedImage;
	private JPanel imagePanel,classPanel,roiPanel;
	private JTextField imageNum;
	private JLabel total;
	private List<JCheckBox> jCheckBoxList;
	private Map<String,JTextArea> jTextList;
	private JComboBox<LearningType> learningType;
	private JFrame frame;

	public FeaturePanelNew(IFeatureManagerNew featureManager) {		
		super(featureManager.getCurrentImage());
		this.featureManager = featureManager;
		this.displayImage= featureManager.getCurrentImage();
		this.jCheckBoxList= new ArrayList<JCheckBox>();
		this.jTextList= new HashMap<String,JTextArea>();
		this.exampleList = new HashMap<String, JList>();
		this.allexampleList = new HashMap<String, JList>();
		roiOverlayList = new HashMap<String, RoiListOverlay>();
		tempClassifiedImage = new ImagePlus();		


		this.hide();
		showPanel();
	}






	public void showPanel() {
		frame = new JFrame("FEATURE PANEL");	        
		frame.setResizable(false);
		NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );
		PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 1, "Previous" );
		ADDCLASS_BUTTON_PRESSED= new ActionEvent( this, 2, "AddClass" );
		SAVECLASS_BUTTON_PRESSED= new ActionEvent( this, 3, "SaveLabel" );
		DELETE_BUTTON_PRESSED = new ActionEvent( this, 4, "DeleteClass" );
		COMPUTE_BUTTON_PRESSED = new ActionEvent( this, 5, "TRAIN" );
		SAVE_BUTTON_PRESSED = new ActionEvent( this, 6, "SAVEDATA" );
		TOGGLE_BUTTON_PRESSED = new ActionEvent( this, 7, "TOGGLE" );
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JList frameList= Util.model();
		frameList.setForeground(Color.BLACK);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(FONT);
		panel.setBackground(Color.GRAY);
		imagePanel = new JPanel();	
		imagePanel.setLayout(new BorderLayout());
		classPanel= new JPanel();
		roiPanel= new JPanel();
		ic=new CustomCanvas(featureManager.getCurrentImage());
		ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
		loadImage(displayImage);
		setOverlay();
		imagePanel.setBackground(Color.GRAY);		
		imagePanel.add(ic,BorderLayout.CENTER);
		imagePanel.setBounds( 10, 10, IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION );		
		panel.add(imagePanel);
		classPanel.setBounds(605,20,350,100);
		classPanel.setPreferredSize(new Dimension(350, 100));
		classPanel.setBorder(BorderFactory.createTitledBorder("CLASSES"));
		JScrollPane classScrolPanel = new JScrollPane(classPanel);
		classScrolPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		classScrolPanel.setBounds(605,20,350,80);
		addClassPanel();
		panel.add(classScrolPanel);
		JPanel features= new JPanel();
		features.setBounds(605,120,350,100);
		features.setBorder(BorderFactory.createTitledBorder("LEARNING"));
		addButton(new JButton(), "PREVIOUS",null , 610, 130, 120, 20,features,PREVIOUS_BUTTON_PRESSED,null );
		imageNum= new JTextField();
		imageNum.setColumns(5);
		imageNum.setBounds( 630, 130, 10, 20 );
		JLabel dasedLine= new JLabel("/");
		dasedLine.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		dasedLine.setForeground(Color.BLACK);
		dasedLine.setBounds(  670, 130, 10, 20 );
		total= new JLabel("Total");
		total.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		total.setForeground(Color.BLACK);
		total.setBounds( 500, 600, 80, 30);		
		imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
		total.setText(Integer.toString(featureManager.getTotalSlice()));
		features.add(imageNum);
		features.add(dasedLine);
		features.add(total);
		JPanel computePanel = new JPanel();
		addButton(new JButton(), "NEXT",null , 800, 130, 80, 20,features,NEXT_BUTTON_PRESSED,null );
		addButton(new JButton(), "COMPUTE",null, 550,550,350,100,computePanel, COMPUTE_BUTTON_PRESSED,null);
		addButton(new JButton(), "SAVE",null, 550,550,350,100,computePanel, SAVE_BUTTON_PRESSED,null);
		addButton(new JButton(), "TOGGLE",null, 550,550,350,100,computePanel, TOGGLE_BUTTON_PRESSED,null);
		addButton(new JButton(), "DOWNLOAD",null, 550,550,350,100,computePanel, DOWNLOAD_BUTTON_PRESSED,null);
		features.add(computePanel);
		frame.add(features);
		JPanel dataJPanel = new JPanel();
		learningType = new JComboBox<LearningType>(LearningType.values());
		learningType.setVisible(true);
		learningType.addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
					if(showColorOverlay) {
						updateGui();
						updateResultOverlay(null);
					}
					else {
						updateGui();
					}					
				}
				else {
					updateGui();
				}


				// here we need to add for classification
			}
		});
		dataJPanel.setBounds(720,240,100,40);
		learningType.setSelectedIndex(0);
		learningType.setFont( FONT );
		learningType.setBackground(new Color(192, 192, 192));
		learningType.setForeground(Color.WHITE);
		dataJPanel.add(learningType);
		dataJPanel.setBackground(Color.GRAY);
		panel.add(dataJPanel);
		roiPanel.setBorder(BorderFactory.createTitledBorder("Region Of Interests"));
		//roiPanel.setPreferredSize(new Dimension(350, 400));
		JScrollPane scrollPane = new JScrollPane(roiPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
		scrollPane.setBounds(605,300,350,250);
		panel.add(scrollPane);
		frame.add(panel);
		frame.pack();
		frame.setSize(1000,600);
		//frame.setSize(getMaximumSize());		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		updateGui();

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
		addButton(new JButton(), "ADD CLASS",null , 630, 20, 130, 20,classPanel,ADDCLASS_BUTTON_PRESSED,null );
		addButton(new JButton(), "SAVE CLASS",null , 630, 20, 130, 20,classPanel,SAVECLASS_BUTTON_PRESSED,null );
		addButton(new JButton(), "DELETE CLASS",null , 630, 20, 130, 20,classPanel,DELETE_BUTTON_PRESSED,null );
		for(String key: featureManager.getClassKeys()){
			String label=featureManager.getClassLabel(key);
			Color color= featureManager.getClassColor(key);
			addClasses(key,label,color);
			addSidePanel(color,key,label);
		}		
	}

	/**
	 * Draw the painted traces on the display image
	 */
	private void drawExamples(){
		for(String key: featureManager.getClassKeys()){
			ArrayList<Roi> rois=(ArrayList<Roi>) featureManager.
					getExamples(key,learningType.getSelectedItem().toString());
			roiOverlayList.get(key).setColor(featureManager.getClassColor(key));
			roiOverlayList.get(key).setRoi(rois);
			//System.out.println("roi draw"+ key);
		}

		getImagePlus().updateAndDraw();
	}
	private void addSidePanel(Color color,String key,String label){
		JPanel panel= new JPanel();
		JList current=Util.model();

		current.setForeground(color);
		exampleList.put(key,current);
		JList all=Util.model();
		all.setForeground(color);
		allexampleList.put(key,all);	
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
		panel.add(Util.addScrollPanel(exampleList.get(key),null));
		panel.add(Util.addScrollPanel(allexampleList.get(key),null));
		roiPanel.add(panel );
		exampleList.get(key).addMouseListener(mouseListener);
		allexampleList.get(key).addMouseListener(mouseListener);
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
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
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

	public void doAction( final ActionEvent event )
	{
		if(event== ADDCLASS_BUTTON_PRESSED){
			featureManager.addClass();
			addClassPanel();
			validateFrame();
			updateGui();
		}
		if(event==DELETE_BUTTON_PRESSED){          

			if(featureManager.getNumOfClasses()-jCheckBoxList.size()<2) {
             JOptionPane.showMessageDialog(null, "There should be minimum two classes");
			}
			else {

				for (JCheckBox checkBox : jCheckBoxList) {

					if (checkBox.isSelected()) {
						featureManager.deleteClass(checkBox.getName());
					}
				}
				addClassPanel();
				validateFrame();
				updateGui();
			}	

		}

		if(event==SAVE_BUTTON_PRESSED){
			featureManager.saveFeatureMetadata();
		}
		if(event==SAVECLASS_BUTTON_PRESSED){
			for (JCheckBox checkBox : jCheckBoxList) {
				if (checkBox.isSelected()) {
					//System.out.println(checkBox.getText());
					String key=checkBox.getName();
					featureManager.setClassLabel(key,jTextList.get(key).getText() );
				}
			}
		}
		if(event == PREVIOUS_BUTTON_PRESSED){			
			ImagePlus image=featureManager.getPreviousImage();
			imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
			loadImage(image);
			if (showColorOverlay){
				if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
					classifiedImage = null;
				}
				else {
					classifiedImage=featureManager.getClassifiedImage();
				}				
				updateResultOverlay(classifiedImage);
			}

			// force limit size of image window
			if(ic.getWidth()>IMAGE_CANVAS_DIMENSION) {
				int x_centre = ic.getWidth()/2+ic.getX();
				int y_centre = ic.getHeight()/2+ic.getY();
				ic.zoomIn(x_centre,y_centre);
			}			
			updateGui();
		}
		if(event==NEXT_BUTTON_PRESSED  ){			
			ImagePlus image=featureManager.getNextImage();
			imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
			loadImage(image);
			if (showColorOverlay){
				if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
					classifiedImage = null;
				}
				else {
					classifiedImage=featureManager.getClassifiedImage();
				}
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
		}
		if(event==COMPUTE_BUTTON_PRESSED){
			if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
				// it means new round of training, so set result setting to false
				showColorOverlay = false;
				// removing previous markings and reset things
				predictionResultClassification = null;
				displayImage.setOverlay(null);

				// compute new predictions
				featureManager.compute();				
				predictionResultClassification = featureManager.getClassificationResultMap();

				// we do not need to get any image in classification setting, only predictions are needed
				classifiedImage = null;
			}

			//segmentation setting
			else {
				classifiedImage=featureManager.compute();
			}
			IJ.log("compute");

			toggleOverlay();
		}
		if(event==TOGGLE_BUTTON_PRESSED){

			toggleOverlay();
		}

		if(event==DOWNLOAD_BUTTON_PRESSED){

			ImagePlus image=featureManager.stackedClassifiedImage();
			FileSaver saver= new FileSaver(image);
			saver.saveAsTiff();
		}
		if(event.getActionCommand()== "ColorButton"){	
			String key=((Component)event.getSource()).getName();
			Color c;
			c = JColorChooser.showDialog( new JFrame(),
					"CLASS COLOR", featureManager.getClassColor(key));

			((Component)event.getSource()).setBackground(c);
			featureManager.updateColor(key, c);
		}
		if(event.getActionCommand()== "AddButton"){	
			String key=((Component)event.getSource()).getName();
			final Roi r = displayImage.getRoi();
			if (null == r)
				return;
			displayImage.killRoi();
			featureManager.addExample(key,r,learningType.getSelectedItem().toString());			
			updateGui();

		}
		if(event.getActionCommand()== "UploadButton"){	
			String key=((Component)event.getSource()).getName();
			uploadExamples(key);
			updateGui();
		}
		if(event.getActionCommand()== "DownloadButton"){	
			String key=((Component)event.getSource()).getName();
			downloadRois(key);
		}


	}


	/**
	 * Toggle between overlay and original image with markings
	 */
	void toggleOverlay()
	{
		if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.SEGMENTATION)) {
			showColorOverlay = !showColorOverlay;			
			if (showColorOverlay && (null != classifiedImage)){
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

			// user wants to see original rois, no results
			else {

				// remove result overlay
				displayImage.setOverlay(null);
				displayImage.updateAndDraw();

				//just show examples drawn by user
				updateGui();
			}
		}		
	}

	public void updateResultOverlay(ImagePlus classifiedImage)
	{
		if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.SEGMENTATION)) {
			ImageProcessor overlay = classifiedImage.getProcessor().duplicate();
			overlay = overlay.convertToByte(false);
			setLut(featureManager.getColors());
			overlay.setColorModel(overlayLUT);
			resultOverlay.setImage(overlay);
			displayImage.updateAndDraw();
		}

		if(ProjectType.valueOf(featureManager.getProjectType()).equals(ProjectType.CLASSIFICATION)) {
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
				rois = (ArrayList<Roi>) featureManager.getExamples(classKey,learningType.getSelectedItem().toString());
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
	private void updateGui(){
		try{
			drawExamples();
			updateExampleLists();
			//updateallExampleLists();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void updateExampleLists()	{
		LearningType type=(LearningType) learningType.getSelectedItem();
		for(String key:featureManager.getClassKeys()){
			exampleList.get(key).removeAll();
			Vector<String> listModel = new Vector<String>();

			for(int j=0; j<featureManager.getRoiListSize(key, learningType.getSelectedItem().toString()); j++){	
				listModel.addElement(key+ " "+ j + " " +
						featureManager.getCurrentSlice()+" "+type.getLearningType());
			}
			exampleList.get(key).setListData(listModel);
			exampleList.get(key).setForeground(featureManager.getClassColor(key));
		}
	}	

	private  MouseListener mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 1) {
				int index = theList.getSelectedIndex();

				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					String[] arr= item.split(" ");
					//System.out.println("Class Id"+ arr[0].trim());
					//int sliceNum=Integer.parseInt(arr[2].trim());
					showSelected( arr[0].trim(),index);

				}
			}

			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				String type= learningType.getSelectedItem().toString();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					//System.out.println("ITEM : "+ item);
					String[] arr= item.split(" ");
					//int classId= featureManager.getclassKey(arr[0].trim())-1;
					featureManager.deleteExample(arr[0], Integer.parseInt(arr[1].trim()), type);
					updateGui();
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
		updateGui();


		displayImage.setColor(Color.YELLOW);
		String type= learningType.getSelectedItem().toString();
		//System.out.println(classKey+"--"+index+"---"+type);
		final Roi newRoi = featureManager.getRoi(classKey, index,type);	
		//System.out.println(newRoi);
		newRoi.setImage(displayImage);
		displayImage.setRoi(newRoi);
		displayImage.updateAndDraw();
	}  
	private JButton addButton(final JButton button ,final String label, final Icon icon, final int x,
			final int y, final int width, final int height,
			JComponent panel, final ActionEvent action,final Color color )
	{
		panel.add(button);
		button.setText( label );
		button.setIcon( icon );
		button.setFont( FONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		if(color!=null){
			button.setBackground(color);
		}
		button.setBounds( x, y, width, height );
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
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
		String type=learningType.getSelectedItem().toString();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			String name=fileChooser.getSelectedFile().toString();
			if(!name.endsWith(".zip")){
				name = name + ".zip";
			}

			featureManager.saveExamples(name, key,type);
		}
	}

	private void uploadExamples(String key) {
		String type=learningType.getSelectedItem().toString();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			featureManager.uploadExamples(fileChooser.getSelectedFile().toString(),key,type);
		}
	}

	/*public static void main(String[] args) {
		new ImageJ();
		IProjectManager projectManager= new ProjectManagerImp();
		projectManager.loadProject("C:\\Users\\sanje\\Documents\\hello\\hello.json");
		new FeaturePanelNew(new FeatureManagerNew(projectManager));
	}*/

}