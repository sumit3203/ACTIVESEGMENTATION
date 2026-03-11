package activeSegmentation.gui;


import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;

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

/**
 * Panel for configuring the machine learning classifier used during
 * the Active Segmentation training pipeline.
 *
 * <p>This is a Weka-specific panel; it is intentional that Weka classes
 * are exposed at this level.</p>
 *
 * @author Sumit Vohra, Dimiter Prodanov
 */
public class LearningPanel extends JFrame implements Runnable, ASCommon {

  // Platform-level variables
  private ProjectManager projectManager;
  private ProjectInfo projectInfo;
  private JList<String> featureSelList;

  // Class-specific variables
  private String defaultOptions = "";
  private String defaultClassifierName = "";
  private boolean hasChanged = false;

  private final ActionEvent LOAD_BUTTON_PRESSED = new ActionEvent(this, 1, "Load");
  private final ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 2, "Save");
  private ClassifierManager learningManager;

  // Weka-specific variables
  private AbstractClassifier aclass = null;
  private GenericObjectEditor wekaClassifierEditor;

  /**
   * Constructs a {@code LearningPanel} and initialises the Weka classifier
   * editor and feature selection list.
   *
   * @param projectManager  the active {@link ProjectManager} instance
   * @param learningManager the {@link ClassifierManager} managing classifiers
   */
  public LearningPanel(ProjectManager projectManager, ClassifierManager learningManager) {
    this.projectManager = projectManager;
    this.learningManager = learningManager;
    this.projectInfo = projectManager.getMetaInfo();
    try {
      this.wekaClassifierEditor = new GenericObjectEditor();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    DefaultListModel<String> model = new DefaultListModel<>();
    featureSelectionUI(model);
    featureSelList = new JList<>(model);
  }

  /**
   * Handles button-press actions for Save and Load operations.
   *
   * <p>Note: {@link ActionEvent#equals(Object)} is used for comparison
   * instead of reference equality ({@code ==}) to ensure correct dispatch
   * regardless of how the event object was constructed.</p>
   *
   * @param event the {@link ActionEvent} to dispatch
   */
  public void doAction(ActionEvent event) {
    if (event.equals(SAVE_BUTTON_PRESSED)) {
      updateClassifier();
      if (aclass != null) {
        IClassifier classifier = new WekaClassifier(aclass);
        learningManager.setClassifier(classifier);
        learningManager.saveLearningMetaData();
        projectManager.updateMetaInfo(projectInfo);

        // Avoid data creep when changing the learning method
        IDataSet data = projectManager.getDataSet();
        if (data != null)
          data.delete();
      }
    } // end SAVE

    if (event.equals(LOAD_BUTTON_PRESSED)) {
      LearningInfo li = learningManager.getLearningMetaData();
      String[] options = li.getOptionsArray();
      String cname = li.getClassifierName();

      try {
        if (!cname.isEmpty()) {
          aclass = (AbstractClassifier) Class.forName(cname).newInstance();
          IClassifier classifier = new WekaClassifier(aclass);
          learningManager.setClassifier(classifier);
          wekaClassifierEditor.setClassType(Classifier.class);
          wekaClassifierEditor.setValue(aclass);
          aclass.setOptions(options);
          defaultOptions = Utils.joinOptions(options);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } // end LOAD
  }

  @Override
  public void run() {
    showPanel();
  }

  /**
   * Builds and displays the learning configuration panel.
   */
  private void showPanel() {
    setTitle("Learning");
    setIconImage(Toolkit.getDefaultToolkit().getImage(
            LearningPanel.class.getResource("logo.png")));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    getContentPane().setBackground(Color.GRAY);
    setLocationRelativeTo(null);
    setSize(600, 300);

    final int xOffsetCol1 = 10;

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

    if (c instanceof OptionHandler) {
      defaultOptions = Utils.joinOptions(((OptionHandler) c).getOptions());
    }

    wekaCEPanel.setBounds(30, 30, 250, 30);
    learningJPanel.add(wekaCEPanel);
    learningJPanel.setBounds(xOffsetCol1, 20, 300, 150);

    JPanel options = new JPanel();
    options.setBorder(BorderFactory.createTitledBorder("Learning Options"));
    options.setBounds(xOffsetCol1, 120, 300, 120);

    JRadioButton pasiveLearning = new JRadioButton("Passive Learning");
    JRadioButton activeLearning = new JRadioButton("Active Learning");
    ButtonGroup bg = new ButtonGroup();
    bg.add(pasiveLearning);
    bg.add(activeLearning);
    options.add(pasiveLearning);
    options.add(activeLearning);
    pasiveLearning.setSelected(true);

    pasiveLearning.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        projectInfo.getLearning().setFeatureSelection(PASSIVELEARNING);
        hasChanged = true;
        updateClassifier();
      }
    });

    activeLearning.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        projectInfo.getLearning().setFeatureSelection(ACTIVELEARNING);
        hasChanged = true;
        updateClassifier();
      }
    });

    final int xOffsetCol2 = 370;

    JPanel featurePanel = new JPanel();
    featurePanel.setBorder(BorderFactory.createTitledBorder("Feature Selection"));
    featurePanel.setBounds(xOffsetCol2, 20, 200, 150);

    JScrollPane classScrolPanel = new JScrollPane(featurePanel);
    classScrolPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    featureSelList.setBackground(Color.WHITE);
    featureSelList.setSelectedIndex(0);
    featureSelList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent evt) {
        TreeMap<String, IFeatureSelection> hm = learningManager.getFeatureSelMap();
        int ind = featureSelList.getSelectedIndex();

        Iterator<Entry<String, IFeatureSelection>> iter = hm.entrySet().iterator();
        List<Entry<String, IFeatureSelection>> result = new ArrayList<>();
        while (iter.hasNext()) {
          result.add(iter.next());
        }
        Entry<String, IFeatureSelection> ee = result.get(ind);
        String fv = ee.getKey();
        projectInfo.getLearning().setLearningOption(fv);
        hasChanged = true;
        updateClassifier();
      }
    });

    featurePanel.add(featureSelList);
    learningJPanel.add(classScrolPanel);

    JPanel IOpanel = new JPanel();
    IOpanel.setBackground(Color.GRAY);
    IOpanel.setBounds(xOffsetCol2, 200, 200, 80);

    IOpanel.add(addButton("Save", null, xOffsetCol2, 120, 200, 50, SAVE_BUTTON_PRESSED));
    IOpanel.add(addButton("Load", null, xOffsetCol2 + 200 + 100, 200, 450, 50, LOAD_BUTTON_PRESSED));

    aPanel.add(learningJPanel);
    aPanel.add(featurePanel);
    aPanel.add(IOpanel);

    add(aPanel);
    setVisible(true);
    setResizable(false);
  }

  /**
   * Populates the feature selection list from the classifier manager.
   *
   * @param model the {@link DefaultListModel} to populate
   */
  private void featureSelectionUI(DefaultListModel<String> model) {
    TreeMap<String, IFeatureSelection> compset = learningManager.getFeatureSelMap();
    Set<String> set = compset.keySet();
    for (String s : set) {
      final String s2 = compset.get(s).getName();
      model.addElement(s2);
    }
  }

  /**
   * Reads the current classifier configuration from the Weka editor and
   * updates the project's {@link LearningInfo} accordingly.
   */
  private void updateClassifier() {
    Object c = wekaClassifierEditor.getValue();
    String[] optionsArray = ((OptionHandler) c).getOptions();

    try {
      final AbstractClassifier cls = (AbstractClassifier) c.getClass().newInstance();
      cls.setOptions(optionsArray);

      final LearningInfo li = projectInfo.getLearning();
      li.setClassifier(cls);
      li.updateOptionList();
      hasChanged = true;
      aclass = cls;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Creates and returns a styled {@link JButton} with the given properties.
   *
   * @param label  button text
   * @param icon   optional button icon
   * @param x      x position (absolute layout)
   * @param y      y position (absolute layout)
   * @param width  button width
   * @param height button height
   * @param action the {@link ActionEvent} to dispatch on click
   * @return a configured {@link JButton}
   */
  private JButton addButton(String label, ImageIcon icon, int x, int y,
                             int width, int height, final ActionEvent action) {
    JButton button = new JButton(label, icon);
    button.setFont(labelFONT);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setBackground(buttonBGColor);
    button.setForeground(buttonColor);
    button.setBounds(x, y, width, height);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        doAction(action);
      }
    });
    return button;
  }
}
