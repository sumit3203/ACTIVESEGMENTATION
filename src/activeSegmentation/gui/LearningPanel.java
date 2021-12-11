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
import activeSegmentation.prj.LearningInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import javax.swing.ImageIcon;


public class LearningPanel implements Runnable, ASCommon {

  private GenericObjectEditor wekaClassifierEditor = new GenericObjectEditor();
  private String originalOptions;
  private String originalClassifierName;
  private ProjectManager projectManager;
  private ProjectInfo projectInfo;
 
  private final JFrame frame = new JFrame("Learning");

  private JList<String> featureSelList;
  private final ActionEvent LOAD_BUTTON_PRESSED = new ActionEvent(this, 1, "Load");
  private final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 2, "Save");
  private ClassifierManager learningManager;
  
  /**
   * 
   * @param projectManager
   * @param learningManager
   */
  public LearningPanel(ProjectManager projectManager, ClassifierManager learningManager )  {
    this.projectManager = projectManager;
    this.learningManager=learningManager;
    this.projectInfo = projectManager.getMetaInfo();
  }
  
  /**
   * 
   * @param event
   */
  public void doAction(ActionEvent event)  {
    if (event == SAVE_BUTTON_PRESSED)     {
      AbstractClassifier testClassifier=getClassifier();    
      if(testClassifier!=null) {
    	  IClassifier classifier = new WekaClassifier(testClassifier);
          
          learningManager.setClassifier(classifier);
          learningManager.saveLearningMetaData();
          projectManager.updateMetaInfo(projectInfo);
          
          // to avoid data creep because we are changing the learning method.
          IDataSet data = projectManager.getDataSet();
          if (data!=null)
        	  data.delete();
      }
     
    } // end SAVE
    if (event == LOAD_BUTTON_PRESSED)     {
    	LearningInfo li=learningManager.getLearningMetaData();
    	String[] options= li.getOptionsArray();
    	String optionsStr = Utils.joinOptions(options);
    	System.out.println(optionsStr);
    	
    } // end LOAD
  }
  
  @Override
  public void run()  {
    showPanel();
  }

/**
 * 
 */
private void showPanel() {
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().setBackground(Color.GRAY);
    frame.setLocationRelativeTo(null);
    frame.setSize(600, 250);
    
    
    final int xOffsetCol1=10;
    
    JPanel aPanel = new JPanel();
    aPanel.setLayout(null);
    aPanel.setBackground(Color.GRAY);
    
    JPanel learningJPanel = new JPanel();
    learningJPanel.setBorder(BorderFactory.createTitledBorder("Learning"));
    
    PropertyPanel wekaCEPanel = new PropertyPanel(this.wekaClassifierEditor);
    wekaClassifierEditor.setClassType(Classifier.class);
    wekaClassifierEditor.setValue(this.learningManager.getClassifier());
    Object c = this.wekaClassifierEditor.getValue();
    originalOptions = "";
    originalClassifierName = c.getClass().getName();
    if ((c instanceof OptionHandler)) {
      originalOptions = Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    
    wekaCEPanel.setBounds(30, 30, 250, 30);
    learningJPanel.add(wekaCEPanel);
    learningJPanel.setBounds(xOffsetCol1, 20, 300, 80);
    
    ////////////////////////////////
    JPanel options = new JPanel();
    options.setBorder(BorderFactory.createTitledBorder("Learning Options"));
    options.setBounds(xOffsetCol1, 120, 300, 80);

    JRadioButton  pasiveLearning = new JRadioButton ("Passive Learning" );
    JRadioButton  activeLearning = new JRadioButton ("Active Learning" );
    ButtonGroup bg=new ButtonGroup(); 
    bg.add(pasiveLearning);
    bg.add(activeLearning);
    options.add(pasiveLearning);
    options.add(activeLearning);
    pasiveLearning.setSelected(false);
    
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
    
    final int xOffsetCol2=370;
    
    /////////////////////////////
    JPanel featurePanel = new JPanel();
    featurePanel.setBorder(BorderFactory.createTitledBorder("Feature Selection"));
    featurePanel.setBounds(xOffsetCol2, 20, 200, 80);
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
    
    featurePanel.add(this.featureSelList);
    
    

    
   ////////////////////////////
    JPanel IOpanel = new JPanel();
    IOpanel.setBackground(Color.GRAY);
    IOpanel.setBounds(xOffsetCol2, 120, 200, 80);
    IOpanel.add(addButton("Save", null, xOffsetCol2, 120, 200, 50, SAVE_BUTTON_PRESSED));
    
    IOpanel.add(addButton("Load", null, xOffsetCol2+200+100, 120, 200, 50, LOAD_BUTTON_PRESSED));
    
    aPanel.add(learningJPanel);
    aPanel.add(featurePanel);
    aPanel.add(IOpanel);
    aPanel.add(options);
    
    this.frame.add(aPanel);
    this.frame.setVisible(true);
}
  
/**
 * 
 * @return
 */
  public AbstractClassifier getClassifier()   {
	System.out.println("Learning panel: in getClassifier");
    Object c = wekaClassifierEditor.getValue();
    String options = "";
    String[] optionsArray = ((OptionHandler)c).getOptions();
    System.out.println(originalOptions);
    if (c instanceof OptionHandler) {
      options = Utils.joinOptions(optionsArray);
    }
    if ((!originalClassifierName.equals(c.getClass().getName())) || (!originalOptions.equals(options))) {
      try {
        final AbstractClassifier cls = (AbstractClassifier)c.getClass().newInstance();
        cls.setOptions(optionsArray);
        
        final LearningInfo li=projectInfo.getLearning();
        li.setClassifier( cls);
        li.updateOptionList();
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
