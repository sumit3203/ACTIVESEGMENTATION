package activeSegmentation.gui;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import activeSegmentation.ASCommon;
import activeSegmentation.learning.ClassifierManager;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;

import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import activeSegmentation.learning.WekaClassifier;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import javax.swing.ImageIcon;


public class LearningPanel implements Runnable, ASCommon {

  private GenericObjectEditor wekaClassifierEditor = new GenericObjectEditor();
  private String originalOptions;
  String originalClassifierName;
  private ProjectManager projectManager;
  private ProjectInfo projectInfo;
  final JFrame frame = new JFrame("Learning");

  JList<String> featureSelList;
 // final ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent(this, 1, "Compute");
  final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 2, "Save");
  ClassifierManager learningManager;
  
  public LearningPanel(ProjectManager projectManager,ClassifierManager learningManager )  {
    this.projectManager = projectManager;
    this.learningManager=learningManager;
    this.projectInfo = projectManager.getMetaInfo();
  }
  
  public void doAction(ActionEvent event)  {
    if (event == this.SAVE_BUTTON_PRESSED)     {

     // System.out.println("in set classifier");
      AbstractClassifier testClassifier=setClassifier();
    
      if(testClassifier!=null) {
    	  IClassifier classifier = new WekaClassifier(testClassifier);
          
          learningManager.setClassifier(classifier);
          learningManager.saveLearningMetaData();
          projectManager.updateMetaInfo(this.projectInfo);
          // to avoid data creep beacuse we are changing the learning method.
          IDataSet data = projectManager.getDataSet();
          if (data!=null)
        	  data.delete();
      }
     
    }
  }
  
  @Override
public void run()  {
    showPanel();
  }

/**
 * 
 */
private void showPanel() {
	this.frame.setDefaultCloseOperation(1);
    this.frame.getContentPane().setBackground(Color.GRAY);
    this.frame.setLocationRelativeTo(null);
    this.frame.setSize(600, 250);
    JPanel learningP = new JPanel();
    learningP.setLayout(null);
    learningP.setBackground(Color.GRAY);
    
    JPanel learningJPanel = new JPanel();
    learningJPanel.setBorder(BorderFactory.createTitledBorder("Learning"));
    
    PropertyPanel wekaCEPanel = new PropertyPanel(this.wekaClassifierEditor);
    this.wekaClassifierEditor.setClassType(Classifier.class);
    this.wekaClassifierEditor.setValue(this.learningManager.getClassifier());
    Object c = this.wekaClassifierEditor.getValue();
    originalOptions = "";
    this.originalClassifierName = c.getClass().getName();
    if ((c instanceof OptionHandler)) {
      originalOptions = Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    wekaCEPanel.setBounds(30, 30, 250, 30);
    learningJPanel.add(wekaCEPanel);
    learningJPanel.setBounds(10, 20, 300, 80);
    
    /////////////////////////////
    JPanel featureSelection = new JPanel();
    featureSelection.setBorder(BorderFactory.createTitledBorder("Feature Selection"));
    featureSelection.setBounds(370, 20, 200, 80);
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("NONE");
    model.addElement("Principle Component Analysis");
    model.addElement("Correlation Based Selection");
 
    this.featureSelList = new JList<>(model);
    this.featureSelList.setBackground(Color.WHITE);
    this.featureSelList.setSelectedIndex(0);
    featureSelList.addListSelectionListener(new ListSelectionListener() {
        @Override
		public void valueChanged(ListSelectionEvent evt) {
        	if (!featureSelList.getValueIsAdjusting()) {
        		final String fv=featureSelList.getSelectedValue();
        		System.out.println(fv);
        		projectInfo.getLearning().setLearningOption(fv);
        	}
        }
      });
    
    featureSelection.add(this.featureSelList);
    
    
    ////////////////////////////////
    JPanel options = new JPanel();
    options.setBorder(BorderFactory.createTitledBorder("Learning Options"));
    options.setBounds(10, 120, 300, 80);

   // JCheckBox pasiveLearning = new JCheckBox("Passive Learning" );
   // JCheckBox activeLearning = new JCheckBox("Active Learning" );
    JRadioButton  pasiveLearning = new JRadioButton ("Passive Learning" );
    JRadioButton  activeLearning = new JRadioButton ("Active Learning" );
    ButtonGroup bg=new ButtonGroup(); 
    bg.add(pasiveLearning);
    bg.add(activeLearning);
    options.add(pasiveLearning);
    options.add(activeLearning);
    
    pasiveLearning.addItemListener(new ItemListener() {  
		@Override
		public void itemStateChanged(ItemEvent e) {
			projectInfo.getLearning().setFeatureSelection(PASSIVELEARNING);
		}  
     });  
    activeLearning.addItemListener(new ItemListener() {  
        @Override
		public void itemStateChanged(ItemEvent e) {               
        	projectInfo.getLearning().setFeatureSelection(ACTIVELEARNING);
        }  
     });  
    
   
    JPanel resetJPanel = new JPanel();
    resetJPanel.setBackground(Color.GRAY);
    resetJPanel.setBounds(370, 120, 200, 80);
    resetJPanel.add(addButton("SAVE", null, 370, 120, 200, 50, this.SAVE_BUTTON_PRESSED));
    
    learningP.add(learningJPanel);
    learningP.add(featureSelection);
    learningP.add(resetJPanel);
    learningP.add(options);
    
    this.frame.add(learningP);
    this.frame.setVisible(true);
}
  
  private AbstractClassifier setClassifier()   {
    Object c = wekaClassifierEditor.getValue();
    String options = "";
    String[] optionsArray = ((OptionHandler)c).getOptions();
    System.out.println(originalOptions);
    if ((c instanceof OptionHandler)) {
      options = Utils.joinOptions(optionsArray);
    }
    if ((!this.originalClassifierName.equals(c.getClass().getName())) || 
      (!this.originalOptions.equals(options))) {
      try {
        AbstractClassifier cls = (AbstractClassifier)c.getClass().newInstance();
        cls.setOptions(optionsArray);
        projectInfo.getLearning().setClassifier( cls);
        projectInfo.getLearning().setOptionList();
        return cls;
      } catch (Exception ex)    {
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  private JButton addButton(String label, ImageIcon icon, int x, int y, int width, int height, final ActionEvent action)  {
    JButton button = new JButton(label, icon);
    button.setFont(labelFONT);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setBackground(buttonBGColor);
    button.setForeground(buttonColor);
    button.setBounds(x, y, width, height);
    button.addActionListener(new ActionListener()  {   	
	    @Override
		public void actionPerformed(ActionEvent e) {
	         doAction(action);
	      }
	    });
    return button;
  }
}
