package activeSegmentation.gui;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

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
import activeSegmentation.learning.weka.WekaClassifier;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;

import activeSegmentation.IClassifier;
import activeSegmentation.IDataSet;
import activeSegmentation.IFeatureSelection;
import activeSegmentation.prj.LearningInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import javax.swing.ImageIcon;

/**
 * This is Weka-specfic panel, so it is OK to expose Weka classes. 
 * @author Sumit Vohra, Dimiter Prodanov
 *
 */
public class LearningPanel implements Runnable, ASCommon {
 
  //platform-level variables
  private ProjectManager projectManager;
  private ProjectInfo projectInfo;	
  private JList<String> featureSelList;	
  
  // class-specific variables
  private String defaultOptions="";
  private String defaultClassifierName="";  
  private boolean hasChanged=false;  
  
  //UI variables
  private final JFrame frame = new JFrame("Learning");
  private final ActionEvent LOAD_BUTTON_PRESSED = new ActionEvent(this, 1, "Load");
  private final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 2, "Save");
  private ClassifierManager learningManager;
  
  //Weka-specific variables
  private AbstractClassifier acls=null;
  private GenericObjectEditor wekaClassifierEditor = new GenericObjectEditor();
   
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
      //updateClassifier(cls);    
      if(acls!=null && hasChanged) {
    	  IClassifier classifier = new WekaClassifier(acls);
          
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
  
    	String cname=li.getClassifierName();
    	System.out.println(cname);
    	try {
			acls = (AbstractClassifier) Class.forName(cname).newInstance();
			//cls.setOptions(options);		
			IClassifier classifier = new WekaClassifier(acls);
	        learningManager.setClassifier(classifier);
	        wekaClassifierEditor.setClassType(Classifier.class);
	        wekaClassifierEditor.setValue(learningManager.getClassifier());
	        defaultOptions = Utils.joinOptions(options);
	        System.out.println(defaultOptions);

		} catch (Exception e) {
			e.printStackTrace();
		}
    
    	
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
    frame.setSize(600, 300);
    
    
    final int xOffsetCol1=10;
    
    JPanel aPanel = new JPanel();
    aPanel.setLayout(null);
    aPanel.setBackground(Color.GRAY);
    
    JPanel learningJPanel = new JPanel();
    learningJPanel.setBorder(BorderFactory.createTitledBorder("Learning"));
    
    PropertyPanel wekaCEPanel = new PropertyPanel(wekaClassifierEditor);
    wekaClassifierEditor.setClassType(Classifier.class);
    wekaClassifierEditor.setValue(learningManager.getClassifier());
    Object c = wekaClassifierEditor.getValue();
    defaultOptions = "";
    defaultClassifierName = c.getClass().getName();
    if ((c instanceof OptionHandler)) {
      defaultOptions = Utils.joinOptions(((OptionHandler)c).getOptions());
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
    pasiveLearning.setSelected(true);
    
    pasiveLearning.addItemListener(new ItemListener() {  
		@Override
		public void itemStateChanged(ItemEvent e) {
			projectInfo.getLearning().setFeatureSelection(PASSIVELEARNING);
			hasChanged=true;
			updateClassifier(); 
		}  
     });  
    activeLearning.addItemListener(new ItemListener() {  
        @Override
		public void itemStateChanged(ItemEvent e) {               
        	projectInfo.getLearning().setFeatureSelection(ACTIVELEARNING);
        	hasChanged=true;
        	updateClassifier(); 
        }  
     });  
    
    final int xOffsetCol2=370;
    
    /////////////////////////////
    JPanel featurePanel = new JPanel();
    featurePanel.setBorder(BorderFactory.createTitledBorder("Feature Selection"));
    featurePanel.setBounds(xOffsetCol2, 20, 200, 80);
    DefaultListModel<String> model = new DefaultListModel<>();
    featureSelectionUI(model);
 
    featureSelList = new JList<>(model);
    featureSelList.setBackground(Color.WHITE);
    featureSelList.setSelectedIndex(0);
    featureSelList.addListSelectionListener(new ListSelectionListener() {
        @Override
		public void valueChanged(ListSelectionEvent evt) {
        	if (!featureSelList.getValueIsAdjusting()) {
        		final String fv=featureSelList.getSelectedValue();
        		//System.out.println(fv);
        		projectInfo.getLearning().setLearningOption(fv);
        		hasChanged=true;
        		updateClassifier(); 
        	}
        }
      });
    
    featurePanel.add(featureSelList);
    
    

    
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
    
    frame.add(aPanel);
    frame.setVisible(true);
    frame.setResizable(false);
}

/**
 * @param model
 */
private void featureSelectionUI(DefaultListModel<String> model) {
	model.addElement("NONE");
	ArrayList<IFeatureSelection> compset=this.learningManager.getFeatureSelList();
	for (IFeatureSelection comp:compset) {
		String s=comp.getName();
		model.addElement(s);
	}
}
  
/**
 * 
 * @return
 */
private void updateClassifier()   {
	System.out.println("Learning panel: in updateClassifier");
	Object c = wekaClassifierEditor.getValue();
	String options = "";
	String[] optionsArray = ((OptionHandler)c).getOptions();
	System.out.println(defaultOptions);
	if (c instanceof OptionHandler) {
		options = Utils.joinOptions(optionsArray);
	}
	if ((!defaultClassifierName.equals(c.getClass().getName())) || (!defaultOptions.equals(options))) {
		try {
			final AbstractClassifier cls = (AbstractClassifier)c.getClass().newInstance();
			cls.setOptions(optionsArray);

			final LearningInfo li=projectInfo.getLearning();
			li.setClassifier( cls);
			li.updateOptionList();
			hasChanged=true;
			acls= cls;
		} catch (Exception ex)    {
			ex.printStackTrace();
		}
	}
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
