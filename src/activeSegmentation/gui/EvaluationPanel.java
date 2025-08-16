package activeSegmentation.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import weka.gui.explorer.Explorer;
import activeSegmentation.IEvaluation;
//import activeSegmentation.evaluation.EvaluationCurve;
import activeSegmentation.prj.ProjectManager;

public class EvaluationPanel extends JFrame  implements Runnable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -262535262594447708L;
	private ProjectManager projectManager=null;
	private IEvaluation evaluation=null;

	public void showPanel() {
		setTitle("Evaluation");
		setIconImage(Toolkit.getDefaultToolkit().getImage(EvaluationPanel.class.getResource("logo.png")));
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Explorer explorer= new Explorer();
		//explorer.setVisible(true);
		add(explorer);

		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
		isRunning=true;
	}

	public EvaluationPanel(ProjectManager dataManager, IEvaluation evaluation) {
		
		this.projectManager = dataManager;
		this.evaluation = evaluation;
		showPanel();
	}


	@Override
	public void run() {
		if (!isRunning)
			showPanel();
		
	}
	
	boolean isRunning=false;
	
	
	 


}
