package activeSegmentation.gui;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.TextRoi;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;






import activeSegmentation.Common;
import activeSegmentation.IFeatureManager;

/**
 * 
 *  
 * @author
 *
 */
public class FeaturePanel extends StackWindow 
{
	/** Generated serial version UID */
	private static final long serialVersionUID = -1037100741242680537L;
	private ImagePlus displayImage;
	/** Lookup table for the result overlay image */

	// GUI components
	final JPanel labelsJPanel=new JPanel(new GridBagLayout());
	final JPanel resetJPanel = new JPanel(new GridBagLayout());
	final JPanel configureJPanel = new JPanel(new GridBagLayout());
	final JPanel ClasslabelstrainingJPanel=new JPanel(new GridBagLayout());
	final JPanel ClasslabelstestingJPanel=new JPanel(new GridBagLayout());
	final JPanel ClasslabelsJPanel=new JPanel(new GridBagLayout());

	
	private List<JCheckBox> jCheckBoxList= new ArrayList<JCheckBox>();
	JPanel imagePanel;
	JPanel controlsBox;
	JPanel controlsBoxForClass;
	JPanel optionBox;
	JTextArea sliceStatus;
	private JFrame frame = new JFrame("CONFIGURE");
	private ImagePlus classifiedImage;
	private HashMap<Integer,Integer> indextolabel = null;
	Panel all;
	/** 50% alpha composite */
	final Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f );

	// Create overlay LUT
	byte[] red = new byte[ 256 ];
	byte[] green = new byte[ 256 ];
	byte[] blue = new byte[ 256 ];

	IFeatureManager featureManager;
	private List<JList<String>> exampleList;
	private List<JList> allexampleList;
	private List<JList> imageTypeList;
	private List<JList> allimageTypeList;
	private List<JList> imagetestingTypeList;
	private List<JList> allimagetestingTypeList;
	private List<Color> colors ;
	
	/** flag to display the overlay image */
	private boolean showColorOverlay=false;
	int originalJ=0;
	int originalJ1=0;
	JPanel classJPanel;
	int originajFrameJ=0, originalFrameK=0;
	LUT overlayLUT;
	ArrayList<Integer> arr = new ArrayList<Integer>();
	
	/** array of roi list overlays to paint the transparent rois of each class */
	private List<RoiListOverlay> roiOverlayList;

	private static final Icon uploadIcon = new ImageIcon(FeaturePanel.class.getResource("upload.png"));
	private static final Icon downloadIcon = new ImageIcon(FeaturePanel.class.getResource("download.png"));

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent( this, 21, "TRAIN" );

	/** This {@link ActionEvent} is fired when the 'pixel level' button is pressed. */
	final ActionEvent PIXEL_LEVEL_BUTTON_PRESSED = new ActionEvent( this, 21, "PIXEL LEVEL EXTRACTION" );
	/** This {@link ActionEvent} is fired when the 'class level' button is pressed. */
	final ActionEvent CLASS_LEVEL_BUTTON_PRESSED = new ActionEvent( this, 21, "CLASS LEVEL EXTRACTION" );


	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent( this, 23, "Save" );
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent CONFIGURE_BUTTON_PRESSED = new ActionEvent( this, 24, "CONFIGURE" );
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent TOGGLE_BUTTON_PRESSED = new ActionEvent( this, 26, "TRAIN" );
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent SAVE_CONFIGURE_BUTTON_PRESSED = new ActionEvent( this, 27, "SaveConfigure" );
	final ActionEvent ADD_BUTTON_PRESSED = new ActionEvent( this, 25, "ADDCLASS" );
	final ActionEvent DELETE_BUTTON_PRESSED = new ActionEvent( this, 2, "Delete" );
	/** opacity (in %) of the result overlay image */
	int overlayOpacity = 33;
	/** alpha composite for the result overlay image */
	Composite overlayAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity / 100f);
	Dimension dimension=new Dimension(100, 25);
	ImageOverlay resultOverlay;

	String feature_extraction_type = "Segmentation";
	
	public FeaturePanel(IFeatureManager featureManager,ImagePlus image)
	{
		super(image, new CustomCanvas(image));	
		final CustomCanvas canvas = (CustomCanvas) getCanvas();
		this.displayImage= image;
		this.setTitle("Active Segmentation (II)");
		this.exampleList = new ArrayList<JList<String>>();
		this.allexampleList = new ArrayList<JList>();
		this.imageTypeList = new ArrayList<JList>();
		this.allimageTypeList = new ArrayList<JList>();
		this.imagetestingTypeList = new ArrayList<JList>();
		this.allimagetestingTypeList = new ArrayList<JList>();
		this.featureManager= featureManager;	
		colors=Util.setDefaultColors();
		roiOverlayList = new ArrayList<RoiListOverlay>();
		setOverlay();
		// Remove the canvas from the window, to add it later
		removeAll();
		setLut(colors);
		imagePanel = new JPanel(new GridBagLayout());	
		imagePanel.setBackground(Color.GRAY);
		imagePanel.add(ic, Util.getGbc(0, 0, 1, false, false));
		imagePanel.add(zSelector,Util.getGbc(0, 0, 0, false, false));
		imagePanel.add(sliceSelector,Util.getGbc(0, 1, 1, false, true));

		
		if(null != sliceSelector){
			sliceSelector.setEnabled(true);
			// set slice selector to the correct number
			sliceSelector.setValue( displayImage.getSlice() );
			// add adjustment listener to the scroll bar
			sliceSelector.addAdjustmentListener(new AdjustmentListener() 
			{
				public void adjustmentValueChanged(final AdjustmentEvent e) {
              
					if(e.getSource() == sliceSelector)
					{
						displayImage.killRoi();
						updateGui();
						if(showColorOverlay)
						{
							updateResultOverlay();		
						}						
					}
				}
			});


			KeyListener keyListener = new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {}

				@Override
				public void keyReleased(final KeyEvent e) {

					if(e.getKeyCode() == KeyEvent.VK_LEFT ||
							e.getKeyCode() == KeyEvent.VK_RIGHT ||
							e.getKeyCode() == KeyEvent.VK_LESS ||
							e.getKeyCode() == KeyEvent.VK_GREATER ||
							e.getKeyCode() == KeyEvent.VK_COMMA ||
							e.getKeyCode() == KeyEvent.VK_PERIOD)
					{
						//IJ.log("moving scroll");
						displayImage.killRoi();
						updateGui();
						if(showColorOverlay)
						{
							updateResultOverlay();
						}
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {}
			};
			// add key listener to the window and the canvas
			addKeyListener(keyListener);
			canvas.addKeyListener(keyListener);

		}
		
		showOption();
		createPanelforPixelLevel();
		createPanelforClassLevel();
		updateGui();
		Panel all = new Panel();
		BoxLayout box = new BoxLayout(all, BoxLayout.X_AXIS);
		all.setLayout(box);
		all.add(imagePanel);
		JPanel subpanel=new JPanel(new GridBagLayout());
		subpanel.add(optionBox,Util.getGbc(0, 0, 0, false, true));
		subpanel.add(controlsBoxForClass,Util.getGbc(0, 20, 0, false, true));
		subpanel.add(controlsBox,Util.getGbc(0, 20, 0, false, true));
		all.add(subpanel);
		add(all);  	      	      	   
		this.pack();	 	    
		this.setVisible(true); 

	}

	private void setOverlay(){
		resultOverlay = new ImageOverlay();
		resultOverlay.setComposite( overlayAlpha );
		((OverlayedImageCanvas)ic).addOverlay(resultOverlay);
	}

	private void addSidePanel(int i){
		JList<String> current=Util.model();
		current.setForeground(colors.get(i));
		exampleList.add(current);
		JList<String> all=Util.model();
		all.setForeground(colors.get(i));
		allexampleList.add(all);	
		RoiListOverlay roiOverlay = new RoiListOverlay();
		roiOverlay.setComposite( transparency050 );
		((OverlayedImageCanvas)ic).addOverlay(roiOverlay);
		roiOverlayList.add(roiOverlay);
		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		ActionEvent addbuttonAction= new ActionEvent(this, i,"AddButton");
		addButton( featureManager.getClassLabel(i+1),null ,labelsJPanel,
				addbuttonAction,new Dimension(100, 21),Util.getGbc(0 ,originalJ , 1, false, false),null );

		ActionEvent uploadAction= new ActionEvent(this, i,"UploadButton");
		addButton( null,uploadIcon,buttonsPanel,uploadAction,
				new Dimension(20, 21),Util.getGbc(0, 0, 1, false, false),null);
		ActionEvent downloadAction= new ActionEvent(this, i,"DownloadButton");
		addButton( null,downloadIcon ,buttonsPanel,downloadAction,
				new Dimension(20, 21),Util.getGbc(1, 0, 1, false, false),null);
		labelsJPanel.add(buttonsPanel,Util.getGbc(1,originalJ , 1, false, false) );
		originalJ++;
		exampleList.get(i).addMouseListener(mouseListener);
		//allexampleList.get(i).addMouseListener(mouseListener);
		labelsJPanel.add( Util.addScrollPanel(exampleList.get(i),null), 
				Util.getGbc(0,originalJ, 1, false, false));
		labelsJPanel.add( Util.addScrollPanel( allexampleList.get(i),null ),
				Util.getGbc(1,originalJ, 1, false, false));
		originalJ++;
	}


	private void updateGui(){	
		try{
			drawExamples();
			updateExampleLists();
			updateallExampleLists();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			//updateImageTypeLists();
			//updateallImageTypeLists();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void showOption(){
		optionBox = new JPanel(new GridBagLayout());	

		addButton( "Pixel Level",null ,optionBox,
				PIXEL_LEVEL_BUTTON_PRESSED,dimension,Util.getGbc(0,0 , 1, false, false),null );
		/*addButton( "Class Level",null ,optionBox,
				CLASS_LEVEL_BUTTON_PRESSED,dimension,Util.getGbc(1,0 , 1, false, false),null );*/
		addButton( "CONFIGURE",null ,configureJPanel,
				CONFIGURE_BUTTON_PRESSED,dimension,Util.getGbc(0,0 , 1, false, false),null );
		optionBox.add(configureJPanel, Util.getGbc(2, 0, 1, false, true));
	}

	private void createPanelforPixelLevel(){
		controlsBox=new JPanel(new GridBagLayout());
		
		add(controlsBox, BorderLayout.EAST);
		configureFrame();
		controlsBox.setVisible(false);
		labelsJPanel.setBorder(BorderFactory.createTitledBorder("LABELS"));

		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			addSidePanel(i);
		}
		
		addButton( "COMPUTE",null ,resetJPanel,
				COMPUTE_BUTTON_PRESSED,dimension,Util.getGbc(0, 0, 1, false, false),null);
		addButton( "TOGGLE",null ,resetJPanel,
				TOGGLE_BUTTON_PRESSED,dimension,Util.getGbc(1, 0, 1, false, false),null );
		addButton( "SAVE",null ,resetJPanel,
				SAVE_BUTTON_PRESSED,dimension,Util.getGbc(2, 0, 1, false, false), null );
		controlsBox.add(Util.addScrollPanel(labelsJPanel, 
				labelsJPanel.getPreferredSize()), Util.getGbc(0, 1, 1, false, true));
		controlsBox.add(resetJPanel, Util.getGbc(0, 2, 1, false, true));
		
		
	}

	private void addsidepanelforClass(int i){
		JList current=Util.model();
		current.setForeground(colors.get(i));
		imageTypeList.add(current);
		
		JList current1=Util.model();
		current1.setForeground(colors.get(i));
		allimageTypeList.add(current1);
		
		JList current2=Util.model();
		current2.setForeground(colors.get(i));
		imagetestingTypeList.add(current2);
		
		JList current3=Util.model();
		current3.setForeground(colors.get(i));
		allimagetestingTypeList.add(current3);
		
		RoiListOverlay roiOverlay = new RoiListOverlay();
		roiOverlay.setComposite( transparency050 );
		((OverlayedImageCanvas)ic).addOverlay(roiOverlay);
		roiOverlayList.add(roiOverlay);
	}
	
	private void addContainerinPanelforClass(int i, int type){
		ActionEvent addbuttonAction= new ActionEvent(this, i,"AddImageType");
		addbuttonAction.setSource(type);
		addButton(featureManager.getClassLabel(i+1),null ,ClasslabelstrainingJPanel,
				addbuttonAction,new Dimension(100, 21),Util.getGbc(0 ,originalJ1 , 1, false, false),null );
		originalJ1++;
		if(type==1){
		
			allimageTypeList.get(i).addMouseListener(mouseListenerClassLevel);
			ClasslabelstrainingJPanel.add( Util.addScrollPanel(imageTypeList.get(i),null), 
					Util.getGbc(0,originalJ1, 1, false, false));
			ClasslabelstrainingJPanel.add( Util.addScrollPanel(allimageTypeList.get(i),null), 
			Util.getGbc(1,originalJ1, 1, false, false));
		}else{
			allimagetestingTypeList.get(i).addMouseListener(mouseListenerClassLevel);
			ClasslabelstrainingJPanel.add( Util.addScrollPanel(imagetestingTypeList.get(i),null), 
			Util.getGbc(0,originalJ1, 1, false, false));
			ClasslabelstrainingJPanel.add( Util.addScrollPanel(allimagetestingTypeList.get(i),null), 
					Util.getGbc(1,originalJ1, 1, false, false));
		}
		originalJ1++;
	}
	
	
	private void createPanelforClassLevel(){
		
		controlsBoxForClass=new JPanel(new GridBagLayout());
		String[] types = {"Training", "Testing"};
		JPanel dataJPanel = new JPanel();
		JComboBox datatype = new JComboBox(types);
		datatype.setVisible(true);
		datatype.setSelectedIndex(0);
		dataJPanel.add(datatype);
		controlsBoxForClass.add(dataJPanel, Util.getGbc(0, 2, 0, false, true));		
		
		final JPanel resetJPanel = new JPanel(new GridBagLayout());
		ClasslabelstrainingJPanel.setBorder(BorderFactory.createTitledBorder("LABELS"));
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			addsidepanelforClass(i);
		}
		
		ClasslabelsJPanel.add(Util.addScrollPanel(ClasslabelstrainingJPanel, 
				ClasslabelstrainingJPanel.getPreferredSize()), Util.getGbc(0, 1, 0, false, true));
		
		ClasslabelstrainingJPanel.removeAll();		
		originalJ1 = 0;
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			addContainerinPanelforClass(i,1);		
		}
		
		
		datatype.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JComboBox combo = (JComboBox)e.getSource();
				if(combo.getSelectedItem().equals("Training")){
					ClasslabelstrainingJPanel.removeAll();
					originalJ1 = 0;
					for(int i = 0; i < featureManager.getNumOfClasses(); i++){
						addContainerinPanelforClass(i,1);		
					}
					updateGui();
				}else{
					ClasslabelstrainingJPanel.removeAll();
					originalJ1 = 0;
					for(int i = 0; i < featureManager.getNumOfClasses(); i++){
						addContainerinPanelforClass(i,2);		
					}
					updateGui();
				}
				
			}});
		
		controlsBoxForClass.add(Util.addScrollPanel(ClasslabelsJPanel, 
				ClasslabelsJPanel.getPreferredSize()), Util.getGbc(0, 3, 0, false, true));
		addButton( "COMPUTE",null ,resetJPanel,
				COMPUTE_BUTTON_PRESSED,dimension,Util.getGbc(0, 0, 1, false, false),null);
		addButton( "SAVE",null ,resetJPanel,
				SAVE_BUTTON_PRESSED,dimension,Util.getGbc(1, 0, 1, false, false), null );
		controlsBoxForClass.add(resetJPanel, Util.getGbc(0, 4, 0, false, true));
		add(controlsBoxForClass, BorderLayout.EAST);
		controlsBoxForClass.setVisible(false);
		
	}
	

	private void configureFrame(){

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel all = new JPanel(new GridBagLayout());
		JPanel configureJPanel = new JPanel(new GridBagLayout());
		addButton( "ADDCLASS",null ,configureJPanel,
				ADD_BUTTON_PRESSED,dimension,Util.getGbc(0,0 , 1, false, false),null );
		classJPanel = new JPanel(new GridBagLayout());
		classJPanel.setBorder(BorderFactory.createTitledBorder("CLASSES"));	

		for(int i = 0; i < featureManager.getNumOfClasses(); i++){

			addclasses(i, originajFrameJ, originalFrameK);

		}

		JPanel resetJPanel = new JPanel(new GridBagLayout());
		addButton( "SAVE",null ,resetJPanel,
				SAVE_CONFIGURE_BUTTON_PRESSED,dimension,Util.getGbc(1, 0, 1, false, false),null );
		addButton( "DELETE",null ,resetJPanel,
				DELETE_BUTTON_PRESSED,dimension,Util.getGbc(2, 0, 1, false, false),null );

		all.add(configureJPanel,Util.getGbc(0, 0, 1, false, true));
		all.add(classJPanel,Util.getGbc(0, 1, 1, false, true));
		all.add(resetJPanel,Util.getGbc(0, 2, 1, false, true));
		frame.add(all);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);	

	}

	
	private JButton addButton( final String label, final Icon icon,JComponent panel, 
			final ActionEvent action, Dimension dimension,GridBagConstraints labelsConstraints,Color color ){
		final JButton button = new JButton();
		panel.add( button, labelsConstraints);
		button.setText( label );
		button.setIcon( icon );
		if(color!=null){
			button.setBackground(color);
		}
		button.setFont( Common.FONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		button.setPreferredSize(dimension);
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				doAction(action);
			}
		} );

		return button;
	}


	public void validateFrame(){

		frame.invalidate();
		frame.revalidate();
		frame.repaint();
	}

	public void doAction( final ActionEvent event )	{
		int currentSlice= displayImage.getCurrentSlice();
		if(event==PIXEL_LEVEL_BUTTON_PRESSED){
			controlsBox.setVisible(true);
			controlsBoxForClass.setVisible(false);
			feature_extraction_type = "Segmentation";
		}
		if(event==CLASS_LEVEL_BUTTON_PRESSED){
			controlsBox.setVisible(false);
			controlsBoxForClass.setVisible(true);
			feature_extraction_type = "classLevel";
		}
		
		if(event.getActionCommand()== "AddImageType"){	
			displayImage.killRoi();
			if(event.getSource().equals(1))
				featureManager.addImageType(event.getID(), currentSlice);			
			else{ 
				featureManager.addTestImageType(event.getID(), currentSlice);
			}
			updateGui();
		}
		
		if(event==COMPUTE_BUTTON_PRESSED){
			
			 
			classifiedImage=featureManager.compute(feature_extraction_type);
		/*	if(feature_extraction_type =="pixelLevel")
			{
			
				classifiedImage=featureManager.computeFeaturespixellevel(feature_extraction_type);
			}else{
				indextolabel = featureManager.computeFeatureclasslevel(feature_extraction_type);
				System.out.println(indextolabel.size()+"Size of indextolabel");
			}*/
			
			toggleOverlay();
		}
		
		if(event==SAVE_BUTTON_PRESSED){
			featureManager.saveFeatureMetadata();

		}
		if(event==SAVE_CONFIGURE_BUTTON_PRESSED){
			updateGui();
		}
		if(event==ADD_BUTTON_PRESSED ){
			addClass(event);
		}

		if(event==TOGGLE_BUTTON_PRESSED){

			toggleOverlay();
		}

		if(event.getActionCommand()== "ColorButton"){	
			int id=event.getID();
			Color c;
			c = JColorChooser.showDialog( new JFrame(),
					"CLASS COLOR", colors.get(id));
			colors.add(id, c);
			((Component)event.getSource()).setBackground(c);
		}	

		if(event==CONFIGURE_BUTTON_PRESSED){

			frame.setVisible(true);
		}


		if(event.getActionCommand()== "AddButton"){	
			
			final Roi r = displayImage.getRoi();
			if (null == r)
				return;
			displayImage.killRoi();
			featureManager.addExample(event.getID(),r, currentSlice);			
			updateGui();

		}

		if(event.getActionCommand()== "UploadButton"){	

			uploadExamples(event.getID(),currentSlice);
			updateGui();
		}

		if(event.getActionCommand()== "DownloadButton"){	

			saveRoi(event.getID(), currentSlice);
		}

	}	

	private void addClass(final ActionEvent  event) {
		featureManager.addClass();
		addclasses(featureManager.getNumOfClasses()-1, originajFrameJ, originalFrameK);
		addSidePanel(featureManager.getNumOfClasses()-1);
		addsidepanelforClass(featureManager.getNumOfClasses()-1);
		validateFrame();
	}

	/**
	 * Draw the painted traces on the display image
	 */
	protected void drawExamples(){
		final int currentSlice = displayImage.getCurrentSlice();
		for(int i = 0; i < getRois(currentSlice).size(); i++){
			roiOverlayList.get(i).setColor(colors.get(i));
			roiOverlayList.get(i).setRoi(getRois(currentSlice).get(i));
		}

		displayImage.updateAndDraw();
	}

	/**
	 * Update the example lists in the GUI
	 */
	private void updateExampleLists()	{
		final int currentSlice = displayImage.getCurrentSlice();
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			exampleList.get(i).removeAll();
			Vector listModel = new Vector();
			for(int j=0; j<featureManager.getSize(i, currentSlice); j++){	
				listModel.addElement(featureManager.getClassLabel(i+1)+ " "+ j + " " + currentSlice);
			}
			exampleList.get(i).setListData(listModel);
			exampleList.get(i).setForeground(colors.get(i));
		}
	}	
	
	/**
	 * Update the imagetype lists in the GUI
	 */
	private void updateImageTypeLists()	{
		final int currentSlice = displayImage.getCurrentSlice();
		displayImage.killRoi();
		int classid = featureManager.getClassIdofCurrentSlicetraining(currentSlice);
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			imageTypeList.get(i).removeAll();
			if(classid==-1||classid!=i){
				Vector<String> listModel = new Vector<String>();
				imageTypeList.get(i).setListData(listModel);
				imageTypeList.get(i).setForeground(colors.get(i));
			}
		}	
		if(classid!=-1){
				Roi roi = new Roi(0,0,displayImage.getWidth(),displayImage.getHeight());
				displayImage.setRoi(roi);
				displayImage.updateAndDraw();
				Vector<String> listModel = new Vector<String>();
				listModel.addElement(featureManager.getClassLabel(classid+1)+ " "+ currentSlice);
				imageTypeList.get(classid).setListData(listModel);
				imageTypeList.get(classid).setForeground(colors.get(classid));
		}
		

		classid = featureManager.getClassIdofCurrentSlicetesting(currentSlice);
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			imagetestingTypeList.get(i).removeAll();
			if(classid==-1||classid!=i){
				Vector<String> listModel = new Vector<String>();
				imagetestingTypeList.get(i).setListData(listModel);
				imagetestingTypeList.get(i).setForeground(colors.get(i));
			}
		}	
		if(classid!=-1){
				Roi roi = new Roi(0,0,displayImage.getWidth(),displayImage.getHeight());
				displayImage.setRoi(roi);
				displayImage.updateAndDraw();
				Vector<String> listModel = new Vector<String>();
				listModel.addElement(featureManager.getClassLabel(classid+1)+ " "+ currentSlice);
				imagetestingTypeList.get(classid).setListData(listModel);
				imagetestingTypeList.get(classid).setForeground(colors.get(classid));
		}
		
	}
	
	/**
	 * Update the allimagetype lists in the GUI
	 */
	private void updateallImageTypeLists()	{
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			allimageTypeList.get(i).removeAll();
			Vector<String> listModel = new Vector<String>();
			ArrayList<Integer> SliceNums = featureManager.getDataImageTypeId(i);
			if(SliceNums!=null){
				for(int j=0; j<SliceNums.size(); j++){	
					listModel.addElement(featureManager.getClassLabel(i+1)+ " "+ SliceNums.get(j));
				}
				allimageTypeList.get(i).setListData(listModel);
				allimageTypeList.get(i).setForeground(colors.get(i));
			}
		}

		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			allimagetestingTypeList.get(i).removeAll();
			Vector<String> listModel = new Vector<String>();
			ArrayList<Integer> SliceNums = featureManager.getDataImageTestTypeId(i);
			if(SliceNums!=null){
				for(int j=0; j<SliceNums.size(); j++){	
					listModel.addElement(featureManager.getClassLabel(i+1)+ " "+ SliceNums.get(j));
				}
				allimagetestingTypeList.get(i).setListData(listModel);
				allimagetestingTypeList.get(i).setForeground(colors.get(i));
			}
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
					int sliceNum=Integer.parseInt(arr[2].trim());
					
					displayImage.setSlice(sliceNum);
					sliceSelector.setValue(sliceNum);
					showSelected( arr[0].trim(),index ,sliceNum);
					
				}
			}

			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					System.out.println("ITEM : "+ item);
					String[] arr= item.split(" ");
					int classId= featureManager.getclassKey(arr[0].trim())-1;
					featureManager.deleteExample(classId, Integer.parseInt(arr[2].trim()), index);
					updateGui();
				}
			}
		}
	};


	private Roi getRoi(int classId, int slice, int index){
		return featureManager.getExamples(classId, slice)
				.get(index);
	}
	
	private List<ArrayList<Roi>> getRois(int currentSlice){
		List<ArrayList< Roi >> roiList= new ArrayList<ArrayList<Roi>>();

		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			ArrayList< Roi > rois = new ArrayList<Roi>();
			for (Roi r : featureManager.getExamples(i, currentSlice)){
				rois.add(r);
			}
			roiList.add(rois);
		}
		return roiList;
	}
	public void uploadExamples(int i, int currentSlice){
		//get selected pixel
		OpenDialog od = new OpenDialog("Choose data file", OpenDialog.getLastDirectory(), "data.arff");
		if (od.getFileName()==null)
			return;
		List<Roi> rois=featureManager.openZip(od.getDirectory() + od.getFileName());
		featureManager.addExampleList(i, rois,currentSlice );

	}
	private  MouseListener mouseListenerClassLevel = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					//System.out.println("ITEM : "+ item);
					String[] arr= item.split(" ");
					int classId= featureManager.getclassKey(arr[0].trim())-1;
					featureManager.deleteImageType(classId,Integer.parseInt(arr[1].trim()));
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
	private void showSelected(String className,int index, int sliceNum ){
		// find the right slice of the corresponding ROI
		updateGui();
		displayImage.setColor(Color.YELLOW);
		int classId= featureManager.getclassKey(className)-1;
	//	int index=exampleList.get(classId).getSelectedIndex();
		final Roi newRoi = getRoi(classId, sliceNum, index);			
		// Set selected trace as current ROI
		newRoi.setImage(displayImage);
		displayImage.setRoi(newRoi);
		displayImage.updateAndDraw();
	}  

	/**
	 * Update the example lists in the GUI
	 */
	private void updateallExampleLists(){
		for(int i = 0; i < featureManager.getNumOfClasses(); i++){
			allexampleList.get(i).removeAll();
			Vector listModel = new Vector();
			for(int currentSlice=1; currentSlice<=displayImage.getStackSize();currentSlice++){
				for(int j=0; j<featureManager.getSize(i, currentSlice); j++){	
					listModel.addElement(featureManager.getClassLabel(i+1)+ " "+ j + " " + currentSlice);
				} 
			}
			allexampleList.get(i).setListData(listModel);
			allexampleList.get(i).setForeground(colors.get(i));
		}
	}

	private void addclasses(int i , int j, int k){
		JCheckBox  checkBox = new JCheckBox("Class :"+ (i+1));
		jCheckBoxList.add(checkBox);
		JPanel classPanel= new JPanel();
		JTextArea textArea= new JTextArea();
		textArea.setText(featureManager.getClassLabel(i+1) );
		textArea.setSize(dimension);
		classPanel.add(checkBox);
		classPanel.add(textArea);
		JButton button= new JButton();
		button.setBackground(colors.get(i));
		ActionEvent colorAction= new ActionEvent(button, i,"ColorButton");
		addAction(button, colorAction);
		classPanel.add(button);
		classJPanel.add(classPanel,Util.getGbc(k, j, 1, false, false));
		originalFrameK++;
		if(i>0 && originalFrameK==2){
			originalFrameK=0;
			originajFrameJ++;
		}

	}

	/**
	 * Update the result image overlay with the corresponding slice
	 */
	public void updateResultOverlay()
	{
		if(feature_extraction_type == "Segmentation")
		{
			ImageProcessor overlay = classifiedImage.getImageStack().getProcessor(displayImage.getCurrentSlice()).duplicate();
			overlay = overlay.convertToByte(false);
			overlay.setColorModel(overlayLUT);
			resultOverlay.setImage(overlay);
			displayImage.updateAndDraw();
		}else{
			//System.out.println(indextolabel.get(displayImage.getCurrentSlice())+"AAA"+displayImage.getCurrentSlice()); 
			if(!indextolabel.containsKey(displayImage.getCurrentSlice()))
				 return;
		     
			 Font font = new Font("Arial", Font.PLAIN, 38);
			 TextRoi textRoi = new TextRoi(displayImage.getWidth()/2, displayImage.getHeight()/2, featureManager.getClassLabel(
					 indextolabel.get(displayImage.getCurrentSlice())), font);
			 textRoi.setStrokeColor(colors.get(indextolabel.get(displayImage.getCurrentSlice())-1));
			 textRoi.setNonScalable(true);                               
			 Overlay overlay = new Overlay(textRoi);
			 textRoi.setPosition(displayImage.getCurrentSlice());
			 displayImage.setOverlay(overlay);
			 displayImage.updateAndDraw();
		}
	}

	/**
	 * Toggle between overlay and original image with markings
	 */
	void toggleOverlay()
	{
		showColorOverlay = !showColorOverlay;
		if (showColorOverlay && (null != classifiedImage || null != indextolabel))
		{
			updateResultOverlay();
		}
		else{
			resultOverlay.setImage(null);
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
	public boolean saveRoi(int  i, int n) {

		String path;
		SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
		String name = sd.getFileName();
		if (name == null)
			return false;
		if (!(name.endsWith(".zip") || name.endsWith(".ZIP")))
			name = name + ".zip";
		String dir = sd.getDirectory();
		path = dir+name;

		return featureManager.saveExamples(path, featureManager.getExamples(i, n));

	}
}
