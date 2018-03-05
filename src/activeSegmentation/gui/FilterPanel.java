package activeSegmentation.gui;



import ij.IJ;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;












import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;





import javax.swing.SwingUtilities;

import activeSegmentation.Common;
import activeSegmentation.IFilterManager;
import activeSegmentation.IProjectManager;
import activeSegmentation.filterImpl.FilterManager;

public class FilterPanel implements Runnable {

	private IFilterManager filterManager;
	private IProjectManager projectManager;
	private JTabbedPane pane;
	private JList filterList;
	private Map<String,List<JTextField>> textMap;
	public static final Font FONT = new Font( "Arial", Font.BOLD, 13 );
	
	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent( this, 1, "Previous" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent( this, 2, "Compute" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	//final ActionEvent LOAD_BUTTON_PRESSED = new ActionEvent( this, 3, "Load" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent( this, 4, "Save" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent DEFAULT_BUTTON_PRESSED = new ActionEvent( this, 5, "Default" );
	
	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent VIEW_BUTTON_PRESSED = new ActionEvent( this, 6, "View" );
	
	final JFrame frame = new JFrame("FILTER");

	public FilterPanel(IProjectManager projectManager) {
		this.projectManager= projectManager;
		this.filterManager =new FilterManager(projectManager);
		this.filterList =Util.model();
		this.filterList.setForeground(Color.ORANGE);
		textMap= new HashMap<String, List<JTextField>>();
	}

	@Override
	public void run() {
		
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pane = new JTabbedPane();
		pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		pane.setFont(Common.FONT);
		//pane.setBackground(Color.GRAY);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(Common.FONT);
        panel.setBackground(Color.GRAY);
        loadFilters();
		pane.setSize(600, 300);
		filterList.addMouseListener(mouseListener);
		JScrollPane scrollPane = Util.addScrollPanel(filterList,null);
		scrollPane.setBounds(605,20,100,280);
		scrollPane.setBackground(Color.GRAY);
		panel.add(scrollPane);
		updateFiterList();
		addButton( new JButton(),"COMPUTE",null , 20, 320, 100, 50,panel,COMPUTE_BUTTON_PRESSED,null );
		addButton(new JButton(), "DEFAULT",null , 240, 320, 100, 50,panel,DEFAULT_BUTTON_PRESSED,null );
		addButton(new JButton(), "SAVE",null , 350, 320, 100, 50,panel,SAVE_BUTTON_PRESSED,null );
		addButton(new JButton(), "VIEW",null , 460, 320, 100, 50,panel,VIEW_BUTTON_PRESSED,null );


		frame.add(pane);
		frame.add(panel);
		frame.setSize(730, 420);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	
	private void loadFilters(){
		Set<String> filters= filterManager.getFilters();  
		System.out.println(filters.size());
		int filterSize=1;
		for(String filter: filters){
			if(filterManager.isFilterEnabled(filter)){
				pane.addTab(filter,null,createTab(filterManager.getFilterSetting(filter),
						filterManager.getFilterImage(filter), 
						filterSize, filters.size(),filter));
				pane.setForeground(Color.BLACK);
				filterSize++;
			}

		}
		
	}

	private JPanel createTab( Map<String , String> settingsMap, Image image, 
			int size, int maxFilters,String filter) {
		JPanel p = new JPanel();
		p.setLayout(null);
		//p.setBackground(Color.GRAY);
		int  y=10;
		if(size!=1)
			addButton( new JButton(), "Previous", null, 10, 90, 95, 38,p,PREVIOUS_BUTTON_PRESSED , null);
		if(size != maxFilters)
			addButton( new JButton(), "Next", null, 480, 90, 70, 38,p ,NEXT_BUTTON_PRESSED , null);
		
		if(image!=null){
			Icon icon = new ImageIcon( image );
			JLabel imagelabel= new JLabel(icon);
			imagelabel.setBounds(100, 3,210,225);
			p.add(imagelabel);
		}

		List<JTextField> jtextList= new ArrayList<JTextField>();
		for (String key: settingsMap.keySet()){

			JLabel label= new JLabel(key);
			label.setFont(Common.FONT);
			label.setForeground(Color.BLACK);
			label.setBounds( 330, y, 70, 25 );
			p.add(label);

			JTextField textArea= new JTextField(settingsMap.get(key));
			textArea.setFont(Common.FONT);
			textArea.setBounds(400, y, 70, 25 );
			p.add(textArea);   
			jtextList.add(textArea);
			y=y+50;
		}

		textMap.put(filter, jtextList);
		JButton button= new JButton();
		ActionEvent event = new ActionEvent( button,1 , filter);
			addButton( button,Common.ENABLED, null, 480,220 , 90, 20,p ,event, Color.GREEN);
		

		return p;
	}

	public void doAction( final ActionEvent event )
	{
		System.out.println("IN DO ACTION");
		System.out.println(event.toString());

		Set<String> filters= filterManager.getFilters();  
		for(String filter : filters){
			if(event.getActionCommand()== filter){

				filterManager.enableFilter(filter);
				System.out.println(filter);
				pane.removeAll();
				loadFilters();
				updateFiterList();
			}
		}
		if(event == PREVIOUS_BUTTON_PRESSED ){

			System.out.println("BUTTON PRESSED");
			pane.setSelectedIndex(pane.getSelectedIndex()-1);
		}
		if(event==NEXT_BUTTON_PRESSED){

			pane.setSelectedIndex(pane.getSelectedIndex()+1);
		}

		if(event==COMPUTE_BUTTON_PRESSED){

			
				filterManager.applyFilters();

		}
		if(event==SAVE_BUTTON_PRESSED){

			System.out.println("");
			String key= pane.getTitleAt( pane.getSelectedIndex());
			int i=0;
			Map<String,String> settingsMap= new HashMap<String, String>();
			for (String settingsKey: filterManager.getFilterSetting(key).keySet()){
				settingsMap.put(settingsKey, textMap.get(key).get(i).getText());	
				i++;
			}
			filterManager.updateFilterSetting(key, settingsMap);		
			filterManager.saveFiltersMetaData();
			IJ.log("FILTER SETTINGS SAVED");

		}

		if(event==DEFAULT_BUTTON_PRESSED){

			String key= pane.getTitleAt( pane.getSelectedIndex());
			System.out.println(key);
			filterManager.setDefault(key);
			updateTabbedGui(key);
			

		}

		if(event==VIEW_BUTTON_PRESSED){
	      // filterManager.getFinalImage().show();
			new ViewFilterResults(this.projectManager);
				//viewPanel.show();
		}

	}

	private void updateTabbedGui(String key){
		int i=0;
		Map<String,String> settingsMap=filterManager.getFilterSetting(key);
		for (String settingsKey: settingsMap.keySet() ){
			
			 textMap.get(key).get(i).setText(settingsMap.get(settingsKey));
			 i++;
		}

	}

	private void updateFiterList() {
		// TODO Auto-generated method stub
		Set<String> filters= filterManager.getFilters();  
		Vector listModel = new Vector();
		for(String filter : filters){
			if(!filterManager.isFilterEnabled(filter)){

				listModel.addElement(filter);
			}
		}
		filterList.setListData(listModel);
		filterList.setForeground(Color.ORANGE);

	}

	private  MouseListener mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					String filter=(String)filterList.getSelectedValue();
					filterManager.enableFilter(filter);
					pane.removeAll();
					loadFilters();
					updateFiterList();
					
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
		button.setBounds( x, y, width, height );
		System.out.println("ADDED");
		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				System.out.println("CLICKED");
				doAction(action);
			}
		});

		return button;
	}

}