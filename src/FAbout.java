

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import activeSegmentation.ASCommon;

import javax.swing.JLabel;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;

public class FAbout extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FAbout frame = new FAbout();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FAbout() {
		setTitle("About AS v."+ ASCommon.version);
		setIconImage(Toolkit.getDefaultToolkit().getImage(FAbout.class.getResource("logo.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(300, 300, 250, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("SansSerif", Font.PLAIN, 12));
		textPane.setText("(C)  Sumit Kumar, Dimiter Prodanov \n\n"+
				"Contributors: \n" +
				"- Victor Jose Garcia Fernandez, intern 2015\n"+
				"- Mukesh Gupta, GSOC 2017\n"+
				"- Sanjeev Dubej, GSOC 2018\n"+
				"- Raghavendra Singh Chauhan, GSOC 2020\n"+
				"- Joanna Stachera, GSOC 2020\n"+
				"- Piyumal Demotte, GSOC 2021\n" +
				"- Purva Chaudhari,   2021 - 2022\n"+
				"- Vasileios-Digenis Akritas, intern 2021\n"+ 
				"- Aaryan Gautam, 2023\n"+
				"- Rikas Ilamdeen, 2024" 
				);
		contentPane.add(textPane, BorderLayout.SOUTH);
		
		ImageIcon imageIcon=new ImageIcon(FAbout.class.getResource("logo-large.png"));
		Image image = imageIcon.getImage(); // transform it 
		Image newimg = image.getScaledInstance(230, -1,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
		imageIcon = new ImageIcon(newimg);  // transform it back
		JLabel lblNewLabel = new JLabel(imageIcon);
		contentPane.add(lblNewLabel, BorderLayout.CENTER);
	}

}
