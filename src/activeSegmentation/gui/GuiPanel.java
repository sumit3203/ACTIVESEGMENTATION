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
 * @author Sumit Vohra, Dimiter Prodanov
 */
public class GuiPanel extends JFrame implements ASCommon {

    private JFrame mainFrame;
    private JPanel controlPanel;
    private LearningPanel learningPanel;
    private FilterPanel filterPanel;
    private FeaturePanel featurePanel;
    private ViewFilterOutputPanel filterOutputPanel;

    final ActionEvent FEATURE_BUTTON_PRESSED     = new ActionEvent(this, 0, "Feature"      );
    final ActionEvent FILTER_BUTTON_PRESSED      = new ActionEvent(this, 1, "Filter"       );
    final ActionEvent LEARNING_BUTTON_PRESSED    = new ActionEvent(this, 2, "Learning"     );
    final ActionEvent EVALUATION_BUTTON_PRESSED  = new ActionEvent(this, 3, "Evaluation"   );
    final ActionEvent FILTERVIS_BUTTON_PRESSED   = new ActionEvent(this, 4, "FilterVis"    );
    final ActionEvent SESSIONGUI_BUTTON_PRESSED  = new ActionEvent(this, 5, "SessionGUI"   );
    final ActionEvent VISUALIZATION_BUTTON_PRESSED = new ActionEvent(this, 8, "Visualization");
    final ActionEvent BACK_BUTTON_PRESSED        = new ActionEvent(this, 6, "Back"         );
    final ActionEvent EXIT_BUTTON_PRESSED        = new ActionEvent(this, 7, "Exit"         );

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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Initialises and displays the main application window.
     *
     * <p>The control panel uses a {@link BorderLayout} at the frame level and a
     * {@link GridBagLayout} for the button grid, replacing the former
     * absolute-positioning approach.  This makes the layout responsive to
     * window resizing and fully parseable by visual designers such as Eclipse
     * WindowBuilder.</p>
     */
    private void initGUI() {
        // --- Main frame setup ------------------------------------------------
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

        // --- Control panel ---------------------------------------------------
        this.controlPanel = new JPanel(new BorderLayout(10, 10));
        this.controlPanel.setBackground(Color.GRAY);
        this.controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Title label (NORTH)
        JLabel titleLabel = new JLabel("Active Segmentation", SwingConstants.CENTER);
        titleLabel.setFont(largeFONT);
        titleLabel.setForeground(Color.ORANGE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        this.controlPanel.add(titleLabel, BorderLayout.NORTH);

        // Button grid (CENTER) ------------------------------------------------
        JPanel buttonGrid = new JPanel(new GridBagLayout());
        buttonGrid.setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.weightx = 0.5;
        gbc.ipadx   = 20;
        gbc.ipady   = 10;

        // Row 0
        addButtonGBC(buttonGrid, gbc, "Select Filters",       0, 0, this.FILTER_BUTTON_PRESSED);
        addButtonGBC(buttonGrid, gbc, "Filter Visualization", 1, 0, this.FILTERVIS_BUTTON_PRESSED);

        // Row 1
        addButtonGBC(buttonGrid, gbc, "Feature Extraction",   0, 1, this.FEATURE_BUTTON_PRESSED);
        addButtonGBC(buttonGrid, gbc, "Model Learning",       1, 1, this.LEARNING_BUTTON_PRESSED);

        // Row 2
        addButtonGBC(buttonGrid, gbc, "Evaluation",           0, 2, this.EVALUATION_BUTTON_PRESSED);
        addButtonGBC(buttonGrid, gbc, "View Sessions",        1, 2, this.SESSIONGUI_BUTTON_PRESSED);

        // Row 3
        addButtonGBC(buttonGrid, gbc, "Visualization",        0, 3, this.VISUALIZATION_BUTTON_PRESSED);

        // Back and Exit share the right column of row 3 in a sub-panel
        JPanel backExitPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        backExitPanel.setBackground(Color.GRAY);
        backExitPanel.add(createButton("Back", this.BACK_BUTTON_PRESSED));
        backExitPanel.add(createButton("Exit", this.EXIT_BUTTON_PRESSED));

        gbc.gridx = 1;
        gbc.gridy = 3;
        buttonGrid.add(backExitPanel, gbc);

        this.controlPanel.add(buttonGrid, BorderLayout.CENTER);

        // --- Attach and show -------------------------------------------------
        this.mainFrame.setContentPane(this.controlPanel);
        this.mainFrame.setVisible(true);
    }

    /**
     * Creates a styled {@link JButton} and adds it to {@code panel} at the
     * grid position specified by {@code col} and {@code row}.
     *
     * @param panel  the target {@link JPanel} using {@link GridBagLayout}
     * @param gbc    the shared {@link GridBagConstraints} (gridx/gridy are set here)
     * @param label  the button text
     * @param col    the column index in the grid
     * @param row    the row index in the grid
     * @param action the {@link ActionEvent} to dispatch when the button is clicked
     */
    private void addButtonGBC(JPanel panel, GridBagConstraints gbc,
                               String label, int col, int row, ActionEvent action) {
        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(createButton(label, action), gbc);
    }

    /**
     * Creates and returns a fully styled {@link JButton}.
     *
     * @param label  the button text
     * @param action the {@link ActionEvent} to dispatch on click
     * @return a configured {@link JButton}
     */
    private JButton createButton(String label, final ActionEvent action) {
        JButton button = new JButton(label);
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
        return button;
    }
}
