package activeSegmentation.gui;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import weka.gui.explorer.Explorer;
import activeSegmentation.IEvaluation;
//import activeSegmentation.evaluation.EvaluationCurve;
import activeSegmentation.prj.ProjectManager;

public class EvaluationPanel  implements Runnable {


	private ProjectManager projectManager=null;
	private IEvaluation evaluation=null;

	public EvaluationPanel() {}

	public EvaluationPanel(ProjectManager dataManager, IEvaluation evaluation) {
		this.projectManager = dataManager;
		this.evaluation = evaluation;
	}

	public void doAction(ActionEvent event) {}

	@Override
	public void run()  {
		JFrame frame = new JFrame("Evaluation");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Explorer explorer= new Explorer();
		//explorer.setVisible(true);
		frame.add(explorer);

		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		//frame.setResizable(false);
		frame.setVisible(true);
	}


}
