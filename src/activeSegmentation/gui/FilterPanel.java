package activeSegmentation.gui;



import ij.IJ;
import ij.ImagePlus;
import ijaux.datatype.Pair;
import test.FilterField;
import test.testFilterAnn;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;




import activeSegmentation.ASCommon;
import activeSegmentation.IFilter;
import activeSegmentation.IFilterManager;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.filter.FilterManager;
import activeSegmentation.moment.MomentsManager;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.GuiUtil;

public class FilterPanel implements Runnable, ASCommon {

	private IFilterManager filterManager;

	private JTabbedPane pane;
	private JList<String> filterList;
	private Map<String,List<JTextField>> filerMap = new HashMap<>();
	
	private Map<String,List<JCheckBox>> filerMap2  = new HashMap<>();

	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent( this, 1, "Previous" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent( this, 2, "Compute" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent( this, 4, "Save" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent DEFAULT_BUTTON_PRESSED = new ActionEvent( this, 5, "Default" );


	final JFrame frame = new JFrame("Filters");
	
	/**
	 * Constructor 
	 * @param projectManager
	 * @param featureManager
	 */
	public FilterPanel(ProjectManager projectManager, FeatureManager  featureManager) {
		if(projectManager.getMetaInfo().getProjectType() != ProjectType.SEGM) {
			this.filterManager =new MomentsManager(projectManager, featureManager);
		}else {
			this.filterManager =new FilterManager(projectManager, featureManager);

		}

		this.filterList =GuiUtil.model();
		this.filterList.setForeground(Color.ORANGE);

	}

	@Override
	public void run() {

		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
		addButton( new JButton(),"Compute",null , 20, 420, 100, 50,panel,COMPUTE_BUTTON_PRESSED,null );
		addButton(new JButton(), "Default",null , 240, 420, 100, 50,panel,DEFAULT_BUTTON_PRESSED,null );
		addButton(new JButton(), "Save",null , 350, 420, 100, 50,panel,SAVE_BUTTON_PRESSED,null );

		frame.add(pane);
		frame.add(panel);
		frame.setSize(730, 520);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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
		int  y=10;
		if(size!=1)
			addButton( new JButton(), "Previous", null, 10, 90, 95, 38,p,PREVIOUS_BUTTON_PRESSED , null);
		IFilter instance=filterManager.getInstance(filterName);
		String longname=instance.getName();
		

		// kernel plot
		if (image!=null){
			Icon icon = new ImageIcon( image );
			JLabel imagelabel= new JLabel(icon);
			int offset1=3;
			imagelabel.setBounds(100, offset1, 210, 225);
			p.add(imagelabel);
			offset1+=225+2;
			
			JLabel label= new JLabel(longname);
			
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			
			label.setBounds( 105, offset1, 210, 25 );
			p.add(label);
			
		}
				
		if(size != maxFilters)
			addButton( new JButton(), "Next", null, 480, 90, 70, 38,p ,NEXT_BUTTON_PRESSED , null);

	
		List<JTextField> jtextList= new ArrayList<>();

		for (String key: settingsMap.keySet()){
			JLabel label= new JLabel(key);
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			label.setBounds( 330, y, 70, 25 );
			p.add(label);

			JTextField input= new JTextField(settingsMap.get(key));
			input.setFont(ASCommon.FONT);
			input.setBounds(400, y, 70, 25 );
			p.add(input);   
			jtextList.add(input);
			y=y+50;
		}

		filerMap.put(filterName, jtextList);
		JButton button= new JButton();
		ActionEvent event = new ActionEvent( button, 1 , filterName);
		addButton( button,ASCommon.ENABLED, null, 480, 220 , 90, 20,p ,event, Color.GREEN);
		return p;
	}

	/*
	 * annotation code will be handled here
	 */
	JPanel createTabAnnotations( Map<String , String> settingsMap, Map<String , String> fieldsMap, Image image, 
			int size, int maxFilters,String filterName) {
		JPanel panel = new JPanel();
		panel.setLayout(null);

		int  y=10;
		// previous button
		if (size!=1)
			addButton( new JButton(), "Previous", null, 10, 90, 90, 38, panel, PREVIOUS_BUTTON_PRESSED , null);
		
		IFilter instance=filterManager.getInstance(filterName);
		String longname=instance.getName();
		
		// kernel plot
		if (image!=null){
			Icon icon = new ImageIcon( image );
			JLabel imagelabel= new JLabel(icon);
			int offset1=3;
			imagelabel.setBounds(100, offset1, 210, 225);
			panel.add(imagelabel);
			offset1+=225+2;
			
			JLabel label= new JLabel(longname);
			
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			
			label.setBounds( 105, offset1, 210, 25 );
			panel.add(label);
			
		}
		
		// next button
		if (size != maxFilters)
			addButton( new JButton(), "Next", null, 480, 90, 70, 38, panel ,NEXT_BUTTON_PRESSED , null);

		

		List<JTextField> jtextList= new ArrayList<>();
		
		//List<Pair<String, String>> skvList=new ArrayList<>();
		
//		List<JCheckBox> jcboxList= new ArrayList<>();
		
		for (String key: settingsMap.keySet()){

			JLabel label= new JLabel(fieldsMap.get(key));
			label.setFont(ASCommon.FONT);
			label.setForeground(Color.BLACK);
			label.setBounds( 330, y, 70, 25 );
			panel.add(label);
			String value=settingsMap.get(key);
			System.out.println("in tab value "+ value);
			//TODO change into check boxes
//			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
//				System.out.println(" check box for " + key);
//				JCheckBox cbox = new JCheckBox (); 
//				cbox.setBounds(400, y, 70, 25 );
//				panel.add(cbox);  
//				if (value.equalsIgnoreCase("true"))
//					cbox.setSelected(true);
//				jcboxList.add(cbox);
//	 
//				cbox.addActionListener(new ActionListener() {
//		    	    @Override
//		    	    public void actionPerformed(ActionEvent event) {
//		    	    	JCheckBox cbLog = (JCheckBox) event.getSource();
//		    	        if (cbLog.isSelected()) {
//		    	            System.out.println("cbox is enabled");		    	     
//		    	        } else {
//		    	            System.out.println("cbox is disabled");
//		    	        }
//		    	    }
//		    	});  
//			} else {
				JTextField input= new JTextField(settingsMap.get(key));
				input.setFont(ASCommon.FONT);
				input.setBounds(400, y, 70, 25 );
				panel.add(input);   
				jtextList.add(input);
//			}
			y=y+50;
		}

		filerMap.put(filterName, jtextList);

//		filerMap2.put(filterName, jcboxList);
		
		// enable button
		JButton button= new JButton();
		ActionEvent event = new ActionEvent( button,1 , filterName);
		addButton( button,ASCommon.ENABLED, null, 480,220 , 90, 20, panel ,event, Color.GREEN);


		return panel;
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
			pane.setSelectedIndex(pane.getSelectedIndex()-1);
		}
		if(event==NEXT_BUTTON_PRESSED){
			pane.setSelectedIndex(pane.getSelectedIndex()+1);
		}
		if(event==COMPUTE_BUTTON_PRESSED){

			filterManager.applyFilters();

		}
		if(event==SAVE_BUTTON_PRESSED){

			//System.out.println("");
			final String key= pane.getTitleAt( pane.getSelectedIndex());
//			int i=0;
			final Map<String,String> settingsMap= new HashMap<>();
			List<JTextField> l=filerMap.get(key);
			ListIterator<JTextField> iter=l.listIterator();
			//Set<String> ks=filterManager.getDefaultFilterSettings(key).keySet();
			//System.out.println(ks);
			for (String settingsKey: filterManager.getDefaultFilterSettings(key).keySet()){
//					settingsMap.put(settingsKey, f.getText());
				//	settingsMap.put(settingsKey, filerMap.get(key).get(i).getText());	
				//settingsMap.put(settingsKey, l.get(i).getText());	
				final String strval= iter.next().getText();
				System.out.println("save/button "+settingsKey+" " + strval );
				settingsMap.put(settingsKey, strval );
	
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


	}

	private void updateTabbedGui(String key){
		//int i=0;
		Map<String,String> settingsMap=filterManager.getDefaultFilterSettings(key);
		List<JTextField> l=filerMap.get(key);
		ListIterator<JTextField> iter=l.listIterator();
		
		for (String settingsKey: settingsMap.keySet() ){
			//filerMap.get(key).get(i).setText(settingsMap.get(settingsKey));
			final String strval= settingsMap.get(settingsKey);
			iter.next().setText(strval);
			System.out.println("default/button "+settingsKey+" " + strval );
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