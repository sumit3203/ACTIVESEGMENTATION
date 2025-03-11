package activeSegmentation.gui;


import activeSegmentation.ASCommon;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterManager;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.filter.FilterManager;
import activeSegmentation.moment.MomentsManager;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.GuiUtil;
import ij.IJ;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class FilterPanel extends JFrame implements Runnable, ASCommon {

	private IFilterManager filterManager;

	private JTabbedPane pane;
	private JList<String> filterList;
	private Map<String,List<JComponent>> filerMap = new HashMap<>();
	private JProgressBar progressBar;
	private Thread computationThread;
	
	//private Map<String,List<JCheckBox>> filerMap2  = new HashMap<>();

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent( this, 1, "Previous" );

	/** This {@link ActionEvent} is fired when the 'compute' button is pressed. */
	final ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent( this, 2, "Compute" );

	/** This {@link ActionEvent} is fired when the 'save' button is pressed. */
	final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent( this, 4, "Save" );

	/** This {@link ActionEvent} is fired when the 'default' button is pressed. */
	final ActionEvent DEFAULT_BUTTON_PRESSED = new ActionEvent( this, 5, "Default" );

	/** This {@link ActionEvent} is fired when the 'help' button is pressed. */
	final ActionEvent HELP_BUTTON_PRESSED = new ActionEvent( this, 6, "Help" );

	final ActionEvent CANCEL_BUTTON_PRESSED = new ActionEvent( this, 7, "Cancel" );
	
	//final JFrame frame = new JFrame("Filters");
	
	/**
	 * Constructor 
	 * @param projectManager
	 * @param featureManager
	 */
	public FilterPanel(ProjectManager projectManager, FeatureManager  featureManager) {
		System.out.println("FilterPanel: init ");
		if(projectManager.getMetaInfo().getProjectType() != ProjectType.SEGM) {
			this.filterManager =new MomentsManager(projectManager, featureManager);
		}else {
			this.filterManager =new FilterManager(projectManager, featureManager);

		}
	
		this.filterList =GuiUtil.getFilterJList();
		this.filterList.setForeground(Color.ORANGE);

		progressBar = new JProgressBar(0, 100); // Initialize progress bar
		progressBar.setStringPainted(true); // Show the progress percentage
		progressBar.setVisible(false); // Initially hidden
		getContentPane().add(progressBar, BorderLayout.SOUTH); // Add progress bar to the bottom of the frame
		
		showPanel();

	}

	@Override
	public void run() {
		if (!isRunning)
			showPanel();
	}
	
	boolean isRunning=false;

	/**
	 * 
	 */
	private void showPanel() {
		setTitle("Filters");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pane = new JTabbedPane();
		pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		pane.setFont(ASCommon.FONT);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(ASCommon.FONT);
		panel.setBackground(Color.GRAY);
		
		loadFilters();
		pane.setSize(600, 400);
		filterList.addMouseListener(mouseListener);
		JScrollPane scrollPane = GuiUtil.addScrollPanel(filterList,null);
		scrollPane.setBounds(605,20,100,380);
		scrollPane.setBackground(Color.GRAY);
		panel.add(scrollPane);
		updateFilterList();
		addButton(new JButton(), "Compute",null , 40,  420, 110, 35, panel, COMPUTE_BUTTON_PRESSED, null );
		addButton(new JButton(), "Default",null , 266, 420, 100, 35, panel, DEFAULT_BUTTON_PRESSED, null );
		addButton(new JButton(), "Save"   ,null , 376, 420, 100, 35, panel, SAVE_BUTTON_PRESSED,    null );
		addButton(new JButton(), "Cancel"   ,null , 486, 420, 100, 35, panel, CANCEL_BUTTON_PRESSED,    null );
		addButton(new JButton(), "Help"   ,null , 605, 420, 100, 35, panel, HELP_BUTTON_PRESSED,    null );

		getContentPane().add(pane);
		getContentPane().add(panel);
		setSize(730, 520);
		setLocationRelativeTo(null);
		setVisible(true);
		isRunning=true;
	}


	private void loadFilters(){
		// gets all detected filters in the path
		Set<String> filters= filterManager.getAllFilters();  

		System.out.println(filters);
		int tabNum=1;
		for(String filter: filters){
			if(filterManager.isFilterEnabled(filter)){
				Map<String,String> settings =filterManager.getDefaultFilterSettings(filter);
				Map<String,String> annotations = IFilterManager.getFieldAnnotations(filter);
				//filterManager.get
				if (annotations.isEmpty()) {
					System.out.println("tab-"+filter);
					pane.addTab(filter,null,
							createTab(settings,	filterManager.getFilterImage(filter), tabNum, filters.size(),filter)
							);
				} else {
					pane.addTab(filter,null,
							createTabAnnotations(settings, annotations, filterManager.getFilterImage(filter), tabNum, filters.size(),filter)
							);
				}
				pane.setForeground(Color.BLACK);
				tabNum++;
			}

		}

	}

	JPanel createTab( Map<String , String> settingsMap, Image image, 
			int size, int maxFilters,String filterName) {
		JPanel p = new JPanel();
		p.setLayout(null);
		//p.setBackground(Color.GRAY);
		int  y=25;
		if(size!=1)
			addButton( new JButton(), "Previous", null, 10, 340, 90, 25, p, PREVIOUS_BUTTON_PRESSED , null);
		IFilter instance=filterManager.getInstance(filterName);
		String longname=instance.getName();
		

		// kernel plot
		if (image!=null){
			Icon icon = new ImageIcon( image );
			JLabel imagelabel= new JLabel(icon);
			int offset1=15;
			imagelabel.setBounds(50, offset1, 210, 225);
			p.add(imagelabel);
			offset1+=225+2;
			
			JLabel label= new JLabel(longname);
			
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			
			label.setBounds( 55, offset1, 210, 25 );
			p.add(label);
			
		}
				
		if(size != maxFilters-1)
			addButton( new JButton(), "Next", null, 495, 340, 90, 25, p , NEXT_BUTTON_PRESSED , null);

//		addButton( new JButton(),  "Help", null, 495, 305, 90, 25, p , HELP_BUTTON_PRESSED , null);
	
//		List<JTextField> jtextList= new ArrayList<>();
		List<JComponent> inputComponents = new ArrayList<>();

		for (String key: settingsMap.keySet()){
			JLabel label= new JLabel(key);
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			label.setBounds( 280, y, 70, 25 );
			p.add(label);

			JComponent inputComponent = createInputComponent(settingsMap.get(key));
			inputComponent.setFont(ASCommon.FONT);
			inputComponent.setBounds(380, y, 40, 25);
			p.add(inputComponent);
			inputComponents.add(inputComponent);

			y += 40;
		}

		filerMap.put(filterName, inputComponents);
		JButton button= new JButton();
		ActionEvent event = new ActionEvent( button, 1 , filterName);
		addButton( button,ASCommon.ENABLED, null, 495, 300 , 90, 30,p ,event, Color.GREEN);
		return p;
	}

	/*
	 * annotation code will be handled here
	 */
	JPanel createTabAnnotations( Map<String , String> settingsMap, Map<String , String> fieldsMap, Image image, 
			int size, int maxFilters,String filterName) {
		JPanel panel = new JPanel();
		panel.setLayout(null);

		int  y=25;
		// previous button
		if (size!=1)
			addButton( new JButton(), "Previous", null, 10, 340, 90, 25, panel, PREVIOUS_BUTTON_PRESSED , null);
		
		IFilter instance=filterManager.getInstance(filterName);
		String longname=instance.getName();
		
		// kernel plot
		if (image!=null){
			Icon icon = new ImageIcon( image );
			JLabel imagelabel= new JLabel(icon);
			int offset1=15;
			imagelabel.setBounds(50, offset1, 210, 225);
			panel.add(imagelabel);
			offset1+=225+2;
			
			JLabel label= new JLabel(longname);
			
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			
			label.setBounds( 55, offset1, 210, 25 );
			panel.add(label);
			
		}
		
		// next button
		if (size != maxFilters-1)
			addButton( new JButton(), "Next", null, 495, 340, 90, 25, panel ,NEXT_BUTTON_PRESSED , null);

		// help button
//		addButton( new JButton(), "Help", null, 495, 305, 90, 25, panel ,HELP_BUTTON_PRESSED , null);

//		List<JTextField> jtextList= new ArrayList<>();
		//List<Pair<String, String>> skvList=new ArrayList<>();
//		List<JCheckBox> jcboxList= new ArrayList<>();

		List<JComponent> inputComponents = new ArrayList<>();
		
		for (String key: settingsMap.keySet()){

			JLabel label= new JLabel(fieldsMap.get(key));
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			label.setBounds( 280, y, 70, 25 );
			panel.add(label);

			String value=settingsMap.get(key);
			System.out.println("in tab value "+ value);

			JComponent inputComponent = createInputComponent(settingsMap.get(key));
			inputComponent.setFont(ASCommon.FONT);
			inputComponent.setBounds(380, y, 40, 25);
			panel.add(inputComponent);
			inputComponents.add(inputComponent);

			y += 40;
		}

		filerMap.put(filterName, inputComponents);

//		filerMap2.put(filterName, jcboxList);
		
		// enable button
		JButton button= new JButton();
		ActionEvent event = new ActionEvent( button,1 , filterName);
		addButton( button,ASCommon.ENABLED, null, 495, 300 , 90, 30, panel ,event, Color.GREEN);


		return panel;
	}

	private JComponent createInputComponent(String value) {
		if (value.matches("\\d+")) {
			return new JSpinner(new SpinnerNumberModel(Integer.parseInt(value), 0, Integer.MAX_VALUE, 1));
		} else if (value.matches("\\d+\\.\\d+")) {
			return new JSpinner(new SpinnerNumberModel(Double.parseDouble(value), 0, Double.MAX_VALUE, 0.1));
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setSelected(Boolean.parseBoolean(value));
			return checkBox;
		} else {
			JTextField textField = new JTextField(value);
			textField.setFont(ASCommon.FONT);
			return textField;
		}
	}


	/*
	 * 
	 */
	public void doAction( final ActionEvent event )	{
		Set<String> filters= filterManager.getAllFilters();  
		for(String filter : filters){
			if(event.getActionCommand()== filter){

				filterManager.enableFilter(filter);
				//System.out.println(filter);
				pane.removeAll();
				loadFilters();
				updateFilterList();
			}
		}
		if(event == PREVIOUS_BUTTON_PRESSED ){
			//TODO check for the bonds
			pane.setSelectedIndex(pane.getSelectedIndex()-1);
		}
		if(event==NEXT_BUTTON_PRESSED){
			//TODO check for the bonds
			pane.setSelectedIndex(pane.getSelectedIndex()+1);
		}
		if(event==COMPUTE_BUTTON_PRESSED){

//			filterManager.applyFilters();

			// Show the progress bar when computation starts
			progressBar.setVisible(true);
			progressBar.setValue(0);

			// Run computation in a separate thread
			computationThread = new Thread(() -> {
				try {
					filterManager.applyFilters(progress -> {
						// This method is called periodically by filterManager.applyFilters with the progress percentage
						SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
					});
				} catch (InterruptedException e) {
					System.out.println("Computation was canceled.");
				} finally {
					// Hide the progress bar when computation is done or canceled
					SwingUtilities.invokeLater(() -> {
						progressBar.setVisible(false);
						progressBar.setValue(0); // reset
					});
				}
			});

			computationThread.start();
		}
		if(event==SAVE_BUTTON_PRESSED){

			//System.out.println("");
			final String key= pane.getTitleAt( pane.getSelectedIndex());
//			int i=0;
			final Map<String,String> settingsMap= new HashMap<>();

//			List<JTextField> l=filerMap.get(key);
//			ListIterator<JTextField> iter=l.listIterator();
			//Set<String> ks=filterManager.getDefaultFilterSettings(key).keySet();
			//System.out.println(ks);

			List<JComponent> inputComponents = filerMap.get(key);
			Iterator<JComponent> iter = inputComponents.iterator();

			for (String settingsKey: filterManager.getDefaultFilterSettings(key).keySet()){

				JComponent inputComponent = iter.next();
				String value = "";

				if (inputComponent instanceof JTextField) {
					value = ((JTextField) inputComponent).getText();
				} else if (inputComponent instanceof JSpinner) {
					value = ((JSpinner) inputComponent).getValue().toString();
				} else if (inputComponent instanceof JCheckBox) {
					value = Boolean.toString(((JCheckBox) inputComponent).isSelected());
				}

				settingsMap.put(settingsKey, value);
				System.out.println("save/button " + settingsKey + " " + value);

//					settingsMap.put(settingsKey, f.getText());
				//	settingsMap.put(settingsKey, filerMap.get(key).get(i).getText());	
				//settingsMap.put(settingsKey, l.get(i).getText());	
//				final String strval= iter.next().getText();
//				System.out.println("save/button "+settingsKey+" " + strval );
//				settingsMap.put(settingsKey, strval );
	
//				System.out.println("save/button "+settingsKey+" "+ l.get(i).getText() +" :: " + strval );
//				List<JCheckBox> l2 = filerMap2.get(key);
//				for (JCheckBox c:l2) {
//					String bs=   Boolean.toString( c.isSelected());
//					settingsMap.put(settingsKey, bs );
//				}
//				i++;
			}
			/*
			 List<JCheckBox> lb = filerMap2.get(key);
			 ListIterator<JCheckBox> iter2=lb.listIterator();
				for (String settingsKey: filterManager.getDefaultFilterSettings(key).keySet()){
					final String strval= iter2.next().getText();
					settingsMap.put(settingsKey, strval );
				}
			*/
			filterManager.updateFilterSettings(key, settingsMap);		
			filterManager.saveFiltersMetaData();
			IJ.log("FILTER SETTINGS SAVED");

		}

		if(event==DEFAULT_BUTTON_PRESSED){

			String key= pane.getTitleAt( pane.getSelectedIndex());
			//System.out.println(key);
			filterManager.setDefault(key);
			updateTabbedGui(key);


		}

		if (event.getActionCommand() == CANCEL_BUTTON_PRESSED.getActionCommand()) {
			// Handle cancel button press
			if (computationThread != null && computationThread.isAlive()) {
				computationThread.interrupt();
			}
			setVisible(false); // Hide the JFrame
			return; // Exit the method
		}

		if (event== HELP_BUTTON_PRESSED) {
			//System.out.println("Help pressed");
			String key= pane.getTitleAt( pane.getSelectedIndex());
			//System.out.println("title: "+key);
	 
			String url=	filterManager.getHelpInfo(key);
			
			// https://stackoverflow.com/questions/24320014/how-to-call-launch-more-than-once-in-java
			if (!javaFxLaunched ) {
				/*		 new Thread() {

					@SuppressWarnings("restriction")
					@Override 
					public void run() {
						IJ.log("starting help url: "+url);
				        Platform.setImplicitExit(false);
						Application.launch(WebHelper.class, url);


					} }.start(); */
				new Thread(()->{
					IJ.log("starting help url: "+url);
					Platform.setImplicitExit(false); 
					Application.launch(WebHelper.class,  url);  
				}).start();
				javaFxLaunched=true;
			} else {
				System.out.println("JavaFx already Launched");
				Platform.runLater(()->{
					try {
						Application application = WebHelper.class.newInstance();
						Stage primaryStage = new Stage();
						application.start(primaryStage);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

		}

		
	}
	
	private  boolean javaFxLaunched = false;

	private void updateTabbedGui(String key){
		//int i=0;
		Map<String,String> settingsMap=filterManager.getDefaultFilterSettings(key);
//		List<JTextField> l=filerMap.get(key);
//		ListIterator<JTextField> iter=l.listIterator();

		List<JComponent> inputComponents = filerMap.get(key);
		Iterator<JComponent> iter = inputComponents.iterator();
		
		for (String settingsKey: settingsMap.keySet() ){

			String value = settingsMap.get(settingsKey);
			JComponent inputComponent = iter.next();

			if (inputComponent instanceof JTextField) {
				((JTextField) inputComponent).setText(value);
			} else if (inputComponent instanceof JSpinner) {
				if (value.matches("\\d+")) {
					((JSpinner) inputComponent).setValue(Integer.parseInt(value));
				} else if (value.matches("\\d+\\.\\d+")) {
					((JSpinner) inputComponent).setValue(Double.parseDouble(value));
				}
			} else if (inputComponent instanceof JCheckBox) {
				((JCheckBox) inputComponent).setSelected(Boolean.parseBoolean(value));
			}

			System.out.println("default/button " + settingsKey + " " + value);

			//filerMap.get(key).get(i).setText(settingsMap.get(settingsKey));
//			final String strval= settingsMap.get(settingsKey);
//			iter.next().setText(strval);
//			System.out.println("default/button "+settingsKey+" " + strval );
			//i++;
		}

	}

	private void updateFilterList() {
		Set<String> filters= filterManager.getAllFilters();  
		Vector<String> listModel = new Vector<>();
		for(String filter : filters){
			if(!filterManager.isFilterEnabled(filter)){
				listModel.addElement(filter);
			}
		}
		filterList.setListData(listModel);
		filterList.setForeground(Color.ORANGE);

	}

	private  MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			JList<?> theList = ( JList<?>) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String filter=filterList.getSelectedValue();
					filterManager.enableFilter(filter);
					pane.removeAll();
					loadFilters();
					updateFilterList();

				}
			}
		}
	};



	private JButton addButton(final JButton button ,final String label, final Icon icon, final int x,
			final int y, final int width, final int height,
			JComponent panel, final ActionEvent action,final Color color )	{

		panel.add( button );
		button.setText( label );
		button.setIcon( icon );
		button.setFont( labelFONT );
		button.setBorderPainted(false); 
		button.setFocusPainted(false); 
		button.setBackground(new Color(192, 192, 192));
		button.setForeground(Color.WHITE);
		button.setBounds( x, y, width, height );

		button.addActionListener( new ActionListener()	{
			@Override
			public void actionPerformed( final ActionEvent e ){
				doAction(action);
			}
		});

		return button;
	}

}