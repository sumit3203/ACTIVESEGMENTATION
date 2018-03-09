package activeSegmentation.gui;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImagePanel;
import ij.gui.ImageWindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;













import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import activeSegmentation.IProjectManager;
import activeSegmentation.io.ProjectInfo;
import activeSegmentation.io.ProjectManagerImp;

public class ViewFilterResults extends ImageWindow  {

	private IProjectManager projectManager;
	private ProjectInfo projectInfo;
	private String projectString;

	public static final Font FONT = new Font( "Arial", Font.BOLD, 10 );

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent NEXT_BUTTON_PRESSED;

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	ActionEvent SLICE_NEXT_BUTTON_PRESSED;
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	ActionEvent PREVIOUS_BUTTON_PRESSED ;

	ActionEvent SLICE_PREVIOUS_BUTTON_PRESSED;

	JPanel imagePanel;
	JTextField imageNum;
	JTextField sliceField;
	JLabel totalSlice;
	JLabel total;
	List<String> imageFileNames;
	int sliceNum,featureNum,totalSlices,totalFeatures;

	public ViewFilterResults(IProjectManager projectManager) {
		super(createImageIcon("no-image.jpg"));
		this.projectManager = projectManager;
		this.imageFileNames=new ArrayList<String>();
		this.projectInfo=this.projectManager.getMetaInfo();
		this.projectString=this.projectInfo.getProjectPath()+"/"+this.projectInfo.getProjectName()+"/"+ "Training/filters/";
		//System.out.println(this.projectString);
		this.hide();
		showPanel();
	}


	private static ImagePlus  createImageIcon(String path) {
		java.net.URL imgURL = CreatProject.class.getResource(path);
		if (imgURL != null) {
			return new ImagePlus(imgURL.getPath());
		} else {            
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}   

	private int loadImages(String directory){
		imageFileNames.clear();
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isFile()) {
				imageFileNames.add(file.getName());
			}
		}
		return imageFileNames.size();
	}

	private int loadSlices(String directory){
		int count=0;
		File folder = new File(directory);
		File[] images = folder.listFiles();
		for (File file : images) {
			if (file.isDirectory()) {
				count++;
			}
		}
		return count;
	}

	public void showPanel() {

		JFrame frame = new JFrame("VISUALIZATION");
		NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );
		PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 1, "Previous" );
		SLICE_NEXT_BUTTON_PRESSED = new ActionEvent( this, 3, "Next" );
		SLICE_PREVIOUS_BUTTON_PRESSED= new ActionEvent( this, 4, "Previous" );
		this.totalSlices=loadSlices(projectString);
		if(totalSlices>0){
			this.sliceNum=1;
			this.totalFeatures=loadImages(projectString+"/SLICE-1/");
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
		JPanel roiPanel= new JPanel();
		roiPanel.setBorder(BorderFactory.createTitledBorder("Region Of Interests"));
		frameList.addMouseListener(mouseListener);
		JScrollPane scrollPane = Util.addScrollPanel(frameList,null);
		roiPanel.setBounds(605,250,150,250);
		roiPanel.add(scrollPane);
		panel.add(roiPanel);

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
		frame.add(panel);
		frame.pack();
		frame.setSize(1000,600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}



	private void loadImage(int sliceNum, int featureNum){

		ImagePlus image= new ImagePlus(projectString+"/SLICE-"+sliceNum+"/"+imageFileNames.get(featureNum-1));
		setImage(image);
		updateImage(image);
	}

	public void doAction( final ActionEvent event )
	{
		//System.out.println("IN DO ACTION");
		//System.out.println(event.toString());


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
			this.totalFeatures=loadImages(projectString+"/SLICE-"+sliceNum+"/");
			imageNum.setText(Integer.toString(featureNum));
			sliceField.setText(Integer.toString(sliceNum));
			total.setText(Integer.toString(totalFeatures));
			loadImage(sliceNum, featureNum);
		}
		if(event==SLICE_NEXT_BUTTON_PRESSED && sliceNum< totalSlices){
			featureNum=1;
			sliceNum= sliceNum+1;
			this.totalFeatures=loadImages(projectString+"/SLICE-"+sliceNum+"/");
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

	public static void main(String[] args) {
		new ImageJ();

		//projectInfo.setProjectName("testproject");
		IProjectManager projectManager= new ProjectManagerImp();
		projectManager.loadProject("C:\\Users\\HP\\Documents\\SUMIT\\ACTIVE_SEG\\testproject\\testproject.json");
		new ViewFilterResults(projectManager);
		//new ImageWindow(new ImagePlus("C:/Users/HP/Documents/DATASET/Stack-2-0001.tif"));
	}

}