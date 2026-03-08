package activeSegmentation.gui;

import activeSegmentation.ASCommon;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ProjectManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Selector GUI class.
 *
 * <p>Refactored to use {@link GridBagLayout} instead of absolute positioning,
 * making the panel responsive and fully compatible with visual UI designers
 * such as Eclipse WindowBuilder.</p>
 *
 * <p>Each navigation button is declared as an explicit named field, and each
 * uses its own {@link GridBagConstraints} instance so that Eclipse WindowBuilder
 * can parse, display, and edit all components correctly in Design view.</p>
 *
 * @author Sumit Vohra, Dimiter Prodanov
 */
public class GuiPanel extends JFrame implements ASCommon {

    private JFrame mainFrame;
    private JPanel controlPanel;
    private LearningPanel learningPanel;
    private FilterPanel filterPanel;
    private FeaturePanel featurePanel;
    private ViewFilterOutputPanel filterOutputPanel;

    // Navigation buttons — declared as named fields for WindowBuilder compatibility
    private JButton btnSelectFilters;
    private JButton btnFilterVisualization;
    private JButton btnFeatureExtraction;
    private JButton btnModelLearning;
    private JButton btnEvaluation;
    private JButton btnViewSessions;
    private JButton btnVisualization;
    private JButton btnBack;
    private JButton btnExit;

    final ActionEvent FEATURE_BUTTON_PRESSED       = new ActionEvent(this, 0, "Feature"      );
    final ActionEvent FILTER_BUTTON_PRESSED        = new ActionEvent(this, 1, "Filter"       );
    final ActionEvent LEARNING_BUTTON_PRESSED      = new ActionEvent(this, 2, "Learning"     );
    final ActionEvent EVALUATION_BUTTON_PRESSED    = new ActionEvent(this, 3, "Evaluation"   );
    final ActionEvent FILTERVIS_BUTTON_PRESSED     = new ActionEvent(this, 4, "FilterVis"    );
    final ActionEvent SESSIONGUI_BUTTON_PRESSED    = new ActionEvent(this, 5, "SessionGUI"   );
    final ActionEvent VISUALIZATION_BUTTON_PRESSED = new ActionEvent(this, 8, "Visualization");
    final ActionEvent BACK_BUTTON_PRESSED          = new ActionEvent(this, 6, "Back"         );
    final ActionEvent EXIT_BUTTON_PRESSED          = new ActionEvent(this, 7, "Exit"         );

    private FeatureManager featureManager;
    private ClassifierManager learningManager;
    private ProjectManager projectManager;
    private EvaluationPanel evaluationPanel;
    private VisualizationPanel visualizationPanel;

    /**
     * Constructs a {@code GuiPanel} and initialises all sub-managers and the UI.
     *
     * @param projectManager the active project manager instance
     */
    public GuiPanel(ProjectManager projectManager) {
        this.projectManager = projectManager;
        learningManager = new ClassifierManager(this.projectManager);
        featureManager  = new FeatureManager(this.projectManager, this.learningManager);
        initGUI();
    }

    /**
     * Returns the main control panel.
     *
     * @return the {@link JPanel} containing all navigation controls
     */
    public JPanel getMainPanel() {
        return controlPanel;
    }

    /**
     * Dispatches a button-press action to the appropriate panel.
     *
     * @param event the {@link ActionEvent} representing the button that was pressed
     */
    public void doAction(ActionEvent event) {
        if (event.equals(this.FILTER_BUTTON_PRESSED)) {
            filterPanel = new FilterPanel(projectManager, featureManager);
            SwingUtilities.invokeLater(filterPanel);
        }
        if (event.equals(this.FILTERVIS_BUTTON_PRESSED)) {
            filterOutputPanel = new ViewFilterOutputPanel(projectManager, featureManager);
            SwingUtilities.invokeLater(this.filterOutputPanel);
        }
        if (event.equals(this.FEATURE_BUTTON_PRESSED)) {
            featurePanel = new FeaturePanel(featureManager);
            SwingUtilities.invokeLater(this.featurePanel);
        }
        if (event.equals(this.LEARNING_BUTTON_PRESSED)) {
            learningPanel = new LearningPanel(projectManager, learningManager);
            SwingUtilities.invokeLater(learningPanel);
        }
        if (event.equals(this.EVALUATION_BUTTON_PRESSED)) {
            evaluationPanel = new EvaluationPanel(projectManager, null);
            SwingUtilities.invokeLater(evaluationPanel);
        }
        if (event.equals(this.SESSIONGUI_BUTTON_PRESSED)) {
            new SessionGUI(projectManager);
        }
        if (event.equals(this.VISUALIZATION_BUTTON_PRESSED)) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Visualization Panel");
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
                frame.add(new VisualizationPanel());
                frame.setVisible(true);
            });
        }
        if (event.equals(this.BACK_BUTTON_PRESSED)) {
            mainFrame.dispose();
            new CreateOpenProjectGUI(projectManager).run();
        }
        if (event.equals(this.EXIT_BUTTON_PRESSED)) {
            int response = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }

    /**
     * Initialises and displays the main application window.
     *
     * <p>Uses {@link BorderLayout} at the frame level and {@link GridBagLayout}
     * for the button grid, replacing the former absolute-positioning approach.
     * Each button uses its own {@link GridBagConstraints} instance for full
     * Eclipse WindowBuilder Design view compatibility.</p>
     */
    private void initGUI() {
        this.mainFrame = new JFrame("Active Segmentation v." + version);
        this.mainFrame.getContentPane().setBackground(Color.LIGHT_GRAY);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setSize(frameWidth, frameHeight);
        this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(
                        mainFrame,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // Control panel with BorderLayout
        this.controlPanel = new JPanel(new BorderLayout(10, 10));
        this.controlPanel.setBackground(Color.GRAY);
        this.controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Title label in NORTH
        JLabel titleLabel = new JLabel("Active Segmentation", SwingConstants.CENTER);
        titleLabel.setFont(largeFONT);
        titleLabel.setForeground(Color.ORANGE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        this.controlPanel.add(titleLabel, BorderLayout.NORTH);

        // Button grid with GridBagLayout in CENTER
        JPanel buttonGrid = new JPanel(new GridBagLayout());
        buttonGrid.setBackground(Color.GRAY);

        // Row 0 - col 0
        btnSelectFilters = new JButton("Select Filters");
        styleButton(btnSelectFilters, FILTER_BUTTON_PRESSED);
        GridBagConstraints gbc00 = new GridBagConstraints();
        gbc00.fill = GridBagConstraints.HORIZONTAL;
        gbc00.insets = new Insets(6, 6, 6, 6);
        gbc00.weightx = 0.5; gbc00.ipadx = 20; gbc00.ipady = 10;
        gbc00.gridx = 0; gbc00.gridy = 0;
        buttonGrid.add(btnSelectFilters, gbc00);

        // Row 0 - col 1
        btnFilterVisualization = new JButton("Filter Visualization");
        styleButton(btnFilterVisualization, FILTERVIS_BUTTON_PRESSED);
        GridBagConstraints gbc01 = new GridBagConstraints();
        gbc01.fill = GridBagConstraints.HORIZONTAL;
        gbc01.insets = new Insets(6, 6, 6, 6);
        gbc01.weightx = 0.5; gbc01.ipadx = 20; gbc01.ipady = 10;
        gbc01.gridx = 1; gbc01.gridy = 0;
        buttonGrid.add(btnFilterVisualization, gbc01);

        // Row 1 - col 0
        btnFeatureExtraction = new JButton("Feature Extraction");
        styleButton(btnFeatureExtraction, FEATURE_BUTTON_PRESSED);
        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.insets = new Insets(6, 6, 6, 6);
        gbc10.weightx = 0.5; gbc10.ipadx = 20; gbc10.ipady = 10;
        gbc10.gridx = 0; gbc10.gridy = 1;
        buttonGrid.add(btnFeatureExtraction, gbc10);

        // Row 1 - col 1
        btnModelLearning = new JButton("Model Learning");
        styleButton(btnModelLearning, LEARNING_BUTTON_PRESSED);
        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.insets = new Insets(6, 6, 6, 6);
        gbc11.weightx = 0.5; gbc11.ipadx = 20; gbc11.ipady = 10;
        gbc11.gridx = 1; gbc11.gridy = 1;
        buttonGrid.add(btnModelLearning, gbc11);

        // Row 2 - col 0
        btnEvaluation = new JButton("Evaluation");
        styleButton(btnEvaluation, EVALUATION_BUTTON_PRESSED);
        GridBagConstraints gbc20 = new GridBagConstraints();
        gbc20.fill = GridBagConstraints.HORIZONTAL;
        gbc20.insets = new Insets(6, 6, 6, 6);
        gbc20.weightx = 0.5; gbc20.ipadx = 20; gbc20.ipady = 10;
        gbc20.gridx = 0; gbc20.gridy = 2;
        buttonGrid.add(btnEvaluation, gbc20);

        // Row 2 - col 1
        btnViewSessions = new JButton("View Sessions");
        styleButton(btnViewSessions, SESSIONGUI_BUTTON_PRESSED);
        GridBagConstraints gbc21 = new GridBagConstraints();
        gbc21.fill = GridBagConstraints.HORIZONTAL;
        gbc21.insets = new Insets(6, 6, 6, 6);
        gbc21.weightx = 0.5; gbc21.ipadx = 20; gbc21.ipady = 10;
        gbc21.gridx = 1; gbc21.gridy = 2;
        buttonGrid.add(btnViewSessions, gbc21);

        // Row 3 - col 0
        btnVisualization = new JButton("Visualization");
        styleButton(btnVisualization, VISUALIZATION_BUTTON_PRESSED);
        GridBagConstraints gbc30 = new GridBagConstraints();
        gbc30.fill = GridBagConstraints.HORIZONTAL;
        gbc30.insets = new Insets(6, 6, 6, 6);
        gbc30.weightx = 0.5; gbc30.ipadx = 20; gbc30.ipady = 10;
        gbc30.gridx = 0; gbc30.gridy = 3;
        buttonGrid.add(btnVisualization, gbc30);

        // Row 3 - col 1: Back and Exit side by side
        JPanel backExitPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        backExitPanel.setBackground(Color.GRAY);

        btnBack = new JButton("Back");
        styleButton(btnBack, BACK_BUTTON_PRESSED);
        backExitPanel.add(btnBack);

        btnExit = new JButton("Exit");
        styleButton(btnExit, EXIT_BUTTON_PRESSED);
        backExitPanel.add(btnExit);

        GridBagConstraints gbc31 = new GridBagConstraints();
        gbc31.fill = GridBagConstraints.HORIZONTAL;
        gbc31.insets = new Insets(6, 6, 6, 6);
        gbc31.weightx = 0.5; gbc31.ipadx = 20; gbc31.ipady = 10;
        gbc31.gridx = 1; gbc31.gridy = 3;
        buttonGrid.add(backExitPanel, gbc31);

        this.controlPanel.add(buttonGrid, BorderLayout.CENTER);

        this.mainFrame.setContentPane(this.controlPanel);
        this.mainFrame.setVisible(true);
    }

    /**
     * Applies consistent visual styling to a {@link JButton} and registers
     * its action listener.
     *
     * @param button the button to style
     * @param action the {@link ActionEvent} to dispatch when clicked
     */
    private void styleButton(JButton button, final ActionEvent action) {
        button.setFont(labelFONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(buttonBGColor);
        button.setForeground(Color.WHITE);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAction(action);
            }
        });
    }
}
