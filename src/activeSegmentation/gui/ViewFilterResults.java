package activeSegmentation.gui;


import ij.ImagePlus;

import java.awt.Color;

import java.awt.Insets;
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

import activeSegmentation.Common;
import activeSegmentation.IFilterManager;

public class ViewFilterResults implements Runnable {

	private IFilterManager filterManager;
	private JTabbedPane pane;
	private JList frameList;

	private ImagePlus trainingImage;
	private String path="D:/ij150-win-java8/ImageJ/plugins/activeSegmentation/projects/";
	private String projectName="astrocytes";
	/** This {@link ActionEvent} is fired when the 'next' button is pressed. */
	final ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent( this, 0, "Next" );

	/** This {@link ActionEvent} is fired when the 'previous' button is pressed. */
	final ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent( this, 1, "Previous" );

	
	final JFrame frame = new JFrame("VISUALIZAION");

	public ViewFilterResults(GuiController controller) {

		this.filterManager = controller.getFilterManager();
		this.frameList =Util.model();
		this.frameList.setForeground(Color.BLACK);
		
	}

	@Override
	public void run() {
		
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pane = new JTabbedPane();
		pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		pane.setFont(Common.FONT);
		pane.setBackground(Color.WHITE);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setFont(Common.FONT);

		pane.setSize(600, 300);
		frameList.addMouseListener(mouseListener);
		JScrollPane scrollPane = Util.addScrollPanel(frameList,null);
		scrollPane.setBounds(605,20,100,280);
		panel.add(scrollPane);
		updateFiterList();


		frame.add(pane);
		frame.add(panel);
		frame.setSize(730, 420);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	


	
	public void doAction( final ActionEvent event )
	{
		System.out.println("IN DO ACTION");
		System.out.println(event.toString());

	
		if(event == PREVIOUS_BUTTON_PRESSED ){

			System.out.println("BUTTON PRESSED");
			pane.setSelectedIndex(pane.getSelectedIndex()-1);
		}
		if(event==NEXT_BUTTON_PRESSED){

			pane.setSelectedIndex(pane.getSelectedIndex()+1);
		}

	

	}


	private void updateFiterList() {
		// TODO Auto-generated method stub
		ImagePlus image= filterManager.getOriginalImage();  
		Vector listModel = new Vector();
		for(int i=0; i <image.getImageStack().size(); i++){	
				listModel.addElement("SLICE-"+(i+1));
		}
		frameList.setListData(listModel);
		frameList.setForeground(Color.RED);

	}

	private  MouseListener mouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent mouseEvent) {
			JList theList = ( JList) mouseEvent.getSource();
			if (mouseEvent.getClickCount() == 2) {
				int index = theList.getSelectedIndex();
				if (index >= 0) {
					String item =theList.getSelectedValue().toString();
					String filter=(String)frameList.getSelectedValue();
					//filterManager.enableFilter(filter);
					pane.removeAll();
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
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFont( Common.FONT );
		if(color!=null){
			button.setBackground(color);
		}
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