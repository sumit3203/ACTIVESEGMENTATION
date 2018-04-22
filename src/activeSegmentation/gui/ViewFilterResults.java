package activeSegmentation.gui;


import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;

import java.awt.AlphaComposite;
import java.awt.Color;
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import activeSegmentation.Common;
import activeSegmentation.IFeatureManager;
import activeSegmentation.IFeatureManagerNew;
import activeSegmentation.IProjectManager;
import activeSegmentation.LearningType;
import activeSegmentation.feature.FeatureManagerNew;
import activeSegmentation.io.ProjectInfo;
import activeSegmentation.io.ProjectManagerImp;

public class ViewFilterResults extends ImageWindow  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IProjectManager projectManager;
	private ProjectInfo projectInfo;
	private String filterString;
	private Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f );
	/** array of roi list overlays to paint the transparent rois of each class */
	private Map<String,RoiListOverlay> roiOverlayList;

	public static final Font FONT = new Font( "Arial", Font.BOLD, 10 );

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent NEXT_BUTTON_PRESSED;

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent SLICE_NEXT_BUTTON_PRESSED;
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	ActionEvent PREVIOUS_BUTTON_PRESSED ;

	ActionEvent SLICE_PREVIOUS_BUTTON_PRESSED;
	private Map<String, JList> exampleList;
	JPanel imagePanel,roiPanel;
	JTextField imageNum;
	JTextField sliceField;
	JLabel totalSlice;
	JLabel total;
	List<String> featuresList;
	List<String> images;
	int sliceNum,featureNum,totalSlices,totalFeatures;
	JFrame frame;
	private JComboBox<LearningType> learningType;

	public ViewFilterResults(IProjectManager projectManager,ImagePlus image) {
		super(image);
		this.projectManager = projectManager;
		this.featuresList=new ArrayList<String>();
		this.images=new ArrayList<>();
		this.exampleList = new HashMap<String, JList>();
		roiOverlayList = new HashMap<String, RoiListOverlay>();
		this.projectInfo=this.projectManager.getMetaInfo();
		//this.images=loadImages(projectString);
		this.filterString=this.projectInfo.getProjectDirectory().get(Common.FILTERSDIR);
		this.hide();
		showPanel();
	}



	private static ImagePlus  createImageIcon(String path) {
		java.net.URL imgURL = ViewFilterResults.class.getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {            
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}   

	private int loadImages(String directory){
		featuresList.clear();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				featuresList.add(file.getName());
			}
		}
		return featuresList.size();
	}

	private int loadSlices(String directory){
		int count=0;
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isDirectory()) {
				count++;
				this.images.add(file.getName());
			}
		}
		return count;
	}


	public void showPanel() {

		frame = new JFrame("VISUALIZATION");
		this.roiPanel=new JPanel();
		NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );
		PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 1, "Previous" );
		SLICE_NEXT_BUTTON_PRESSED = new ActionEvent( this, 3, "Next" );
		SLICE_PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 4, "Previous" );
		this.totalSlices=loadSlices(filterString);
		if(totalSlices>0){
			this.sliceNum=1;
			this.totalFeatures=loadImages(filterString+images.get(0));
		}

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JList frameList= Util.model();
		frameList.setForeground(Color.BLACK);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(FONT);
		panel.setBackground(Color.GRAY);
		imagePanel = new JPanel();	
		imagePanel.setBackground(Color.GRAY);
		if(totalFeatures>0){
			featureNum=1;
			loadImage(sliceNum, featureNum);
		}
		imagePanel.add(ic);
		imagePanel.setBounds( 10, 10, 560, 560 );
		panel.add(imagePanel);
		roiPanel.setBorder(BorderFactory.createTitledBorder("Region Of Interests"));
		roiPanel.setPreferredSize(new Dimension(200, 400));
		JScrollPane scrollPane = new JScrollPane(roiPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);	
		scrollPane.setBounds(605,300,200,250);
		panel.add(scrollPane);

		JPanel slicePanel= new JPanel();
		slicePanel.setBounds(605,20,350,80);
		slicePanel.setBorder(BorderFactory.createTitledBorder("SLICES"));
		addButton(new JButton(), "PREVIOUS",null , 610, 70, 120, 20,slicePanel,SLICE_PREVIOUS_BUTTON_PRESSED,null );
		sliceField= new JTextField();
		sliceField.setColumns(5);
		sliceField.setBounds( 630, 70, 10, 20 );
		JLabel sliceLine= new JLabel("/");
		sliceLine.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		sliceLine.setForeground(Color.BLACK);
		sliceLine.setBounds( 670, 70, 10, 20 );
		totalSlice= new JLabel("--");
		totalSlice.setFont(new Font( "Arial", Font.PLAIN, 15 ));
		totalSlice.setForeground(Color.BLACK);
		totalSlice.setBounds( 730, 600, 80, 30);
		if(sliceNum>0){
			sliceField.setText("1");
			totalSlice.setText(Integer.toString(totalSlices));
		}
		slicePanel.add(sliceField);
		slicePanel.add(sliceLine);
		slicePanel.add(totalSlice);
		addButton(new JButton(), "NEXT",null , 800, 50, 80, 20,slicePanel,SLICE_NEXT_BUTTON_PRESSED,null );
		panel.add(slicePanel);
		JPanel features= new JPanel();
		features.setBounds(605,120,350,80);
		features.setBorder(BorderFactory.createTitledBorder("FEATURES"));
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
		if(this.totalFeatures>0){
			imageNum.setText("1");
			total.setText(Integer.toString(totalFeatures));

		}
		features.add(imageNum);
		features.add(dasedLine);
		features.add(total);
		addButton(new JButton(), "NEXT",null , 800, 130, 80, 20,features,NEXT_BUTTON_PRESSED,null );
		frame.add(features);
		JPanel dataJPanel = new JPanel();
		learningType = new JComboBox<LearningType>(LearningType.values());
		learningType.setVisible(true);
		learningType.addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {			 
				//updateGui();	
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
		frame.add(panel);
		frame.pack();
		frame.setSize(1000,600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
       // refreshPanel();
	}

	/*private void refreshPanel() {
		roiPanel.removeAll();
		for(String key: featureManager.getClassKeys()){
			String label=featureManager.getClassLabel(key);
			Color color= featureManager.getClassColor(key);
			addSidePanel(color,key,label);
		}		
	}*/

	private void addSidePanel(Color color,String key,String label){
		JPanel panel= new JPanel();
		JList current=Util.model();
		current.setForeground(color);
		exampleList.put(key,current);

		RoiListOverlay roiOverlay = new RoiListOverlay();
		roiOverlay.setComposite( transparency050 );
		((OverlayedImageCanvas)ic).addOverlay(roiOverlay);
		roiOverlayList.put(key,roiOverlay);
		JPanel buttonPanel= new JPanel();
		buttonPanel.setName(key);
		ActionEvent addbuttonAction= new ActionEvent(buttonPanel, 1,"AddButton");

		JButton addButton= new JButton();
		addButton.setName(key);
		addButton(addButton, label, null, 605,280,350,250, buttonPanel, addbuttonAction, null);
		roiPanel.add(buttonPanel);
		panel.add(Util.addScrollPanel(exampleList.get(key),null));
		roiPanel.add(panel );
		exampleList.get(key).addMouseListener(mouseListener);
	}

	private void loadImage(int sliceNum, int featureNum){

		ImagePlus image= new ImagePlus(filterString+images.get(sliceNum-1)+"/"+featuresList.get(featureNum-1));
		setImage(image);
		updateImage(image);
	}

	public void doAction( final ActionEvent event )
	{

		if(event == PREVIOUS_BUTTON_PRESSED && featureNum >1){

			//System.out.println("BUTTON PRESSED");
			featureNum=featureNum-1;
			imageNum.setText(Integer.toString(featureNum));
			loadImage(sliceNum, featureNum);


		}
		if(event==NEXT_BUTTON_PRESSED && featureNum<totalFeatures ){
			//	System.out.println("IN NEXT BUTTOn");
			featureNum=featureNum+1;
			imageNum.setText(Integer.toString(featureNum));
			loadImage(sliceNum, featureNum);
		}

		if(event==SLICE_PREVIOUS_BUTTON_PRESSED && sliceNum>1){
			featureNum=1;
			sliceNum= sliceNum-1;
			this.totalFeatures=loadImages(filterString+images.get(sliceNum-1)+"/");
			imageNum.setText(Integer.toString(featureNum));
			sliceField.setText(Integer.toString(sliceNum));
			total.setText(Integer.toString(totalFeatures));
			loadImage(sliceNum, featureNum);
		}
		if(event==SLICE_NEXT_BUTTON_PRESSED && sliceNum< totalSlices){
			featureNum=1;
			sliceNum= sliceNum+1;
			this.totalFeatures=loadImages(filterString+images.get(sliceNum-1)+"/");
			imageNum.setText(Integer.toString(featureNum));
			sliceField.setText(Integer.toString(sliceNum));
			total.setText(Integer.toString(totalFeatures));
			loadImage(sliceNum, featureNum);

		}


	}




	private  MouseListener mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					//String filter=(String)frameList.getSelectedValue();
					//filterManager.enableFilter(filter);



				}
			}
		}
	};

	private JButton addButton(final JButton button ,final String label, final Icon icon, final int x,
			final int y, final int width, final int height,
			JComponent panel, final ActionEvent action,final Color color )
	{
		panel.add( button );
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
		//System.out.println("ADDED");
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				//System.out.println("CLICKED");
				doAction(action);
			}
		});

		return button;
	}
	/**
	 * Draw the painted traces on the display image
	 */
/*	private void drawExamples(){
		for(String key: featureManager.getClassKeys()){
			ArrayList<Roi> rois=(ArrayList<Roi>) featureManager.
					getExamples(key,learningType.getSelectedItem().toString());
			roiOverlayList.get(key).setColor(featureManager.getClassColor(key));
			roiOverlayList.get(key).setRoi(rois);
		}

		getImagePlus().updateAndDraw();
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
	public static void main(String[] args) {
		new ImageJ();

		//projectInfo.setProjectName("testproject");
		IProjectManager projectManager= new ProjectManagerImp();
		projectManager.loadProject("C:\\Users\\sumit\\Documents\\testproject\\testproject.json");
		IFeatureManagerNew featureManagerNew = new FeatureManagerNew(projectManager);
		new ViewFilterResults(projectManager,featureManagerNew,createImageIcon("no-image.jpg"));
		//new ImageWindow(new ImagePlus("C:/Users/HP/Documents/DATASET/Stack-2-0001.tif"));
	}*/

}