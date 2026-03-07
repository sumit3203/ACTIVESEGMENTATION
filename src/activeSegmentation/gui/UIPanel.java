package activeSegmentation.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import activeSegmentation.ASCommon;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ProjectManager;

/**
 * Alternative navigation panel for the Active Segmentation plugin.
 *
 * @deprecated As of version 1.2, {@link GuiPanel} is the canonical navigation
 *             panel and should be used in preference to this class.
 *             {@code UIPanel} duplicates the functionality of {@link GuiPanel}
 *             and will be removed in a future release. All new development
 *             should target {@link GuiPanel}.
 *
 * <p>This class has been retained for backwards compatibility only. The layout
 * has been migrated from absolute positioning to {@link GridBagLayout} to
 * maintain consistency with {@link GuiPanel} during the transition period.</p>
 *
 * @author prodanov
 * @see GuiPanel
 */
@Deprecated
public class UIPanel extends JFrame implements ASCommon {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;

    // Navigation buttons — named fields for WindowBuilder compatibility
    private JButton btnFilters;
    private JButton btnFilterViz;
    private JButton btnFeatures;
    private JButton btnLearning;
    private JButton btnEvaluation;
    private JButton btnSessions;

    private LearningPanel learningPanel;
    private FilterPanel filterPanel;
    private FeaturePanel featurePanel;
    private ViewFilterOutputPanel filterOutputPanel;
    private ProjectManager projectManager;
    private EvaluationPanel evaluationPanel;
    private SessionGUI sessionPanel;

    private FeatureManager featureManager;
    private ClassifierManager learningManager;

    /**
     * Launch the application.
     *
     * @deprecated Use {@link GuiPanel} instead.
     */
    @Deprecated
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIPanel frame = new UIPanel(null);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Constructs the {@code UIPanel} frame and initialises all sub-managers.
     *
     * @param projMan the active {@link ProjectManager} instance
     * @deprecated Use {@link GuiPanel#GuiPanel(ProjectManager)} instead.
     */
    @Deprecated
    public UIPanel(ProjectManager projMan) {
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                UIPanel.class.getResource("/activeSegmentation/gui/logo.png")));
        setTitle("Active Segmentation v." + version);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 400);

        this.projectManager = projMan;
        learningManager = new ClassifierManager(this.projectManager);
        featureManager  = new FeatureManager(this.projectManager, this.learningManager);

        initComponents();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds and assembles all UI components using {@link GridBagLayout},
     * consistent with the canonical {@link GuiPanel} implementation.
     */
    private void initComponents() {
        contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(10, 20, 20, 20));
        contentPane.setBackground(Color.GRAY);
        setContentPane(contentPane);

        // Title label (NORTH)
        JLabel titleLabel = new JLabel("Active Segmentation", SwingConstants.CENTER);
        titleLabel.setForeground(Color.ORANGE);
        titleLabel.setFont(largeFONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        contentPane.add(titleLabel, BorderLayout.NORTH);

        // Button grid (CENTER)
        JPanel buttonGrid = new JPanel(new GridBagLayout());
        buttonGrid.setBackground(Color.GRAY);

        // Row 0 - col 0
        btnFilters = new JButton("Select Filters");
        styleButton(btnFilters);
        btnFilters.addActionListener(e -> {
            filterPanel = new FilterPanel(projectManager, featureManager);
            filterPanel.setVisible(true);
        });
        GridBagConstraints gbc00 = new GridBagConstraints();
        gbc00.fill = GridBagConstraints.HORIZONTAL;
        gbc00.insets = new Insets(6, 6, 6, 6);
        gbc00.weightx = 0.5; gbc00.ipadx = 20; gbc00.ipady = 10;
        gbc00.gridx = 0; gbc00.gridy = 0;
        buttonGrid.add(btnFilters, gbc00);

        // Row 0 - col 1
        btnFilterViz = new JButton("Filter Visualization");
        styleButton(btnFilterViz);
        btnFilterViz.addActionListener(e -> {
            filterOutputPanel = new ViewFilterOutputPanel(projectManager, featureManager);
            filterOutputPanel.setVisible(true);
        });
        GridBagConstraints gbc01 = new GridBagConstraints();
        gbc01.fill = GridBagConstraints.HORIZONTAL;
        gbc01.insets = new Insets(6, 6, 6, 6);
        gbc01.weightx = 0.5; gbc01.ipadx = 20; gbc01.ipady = 10;
        gbc01.gridx = 1; gbc01.gridy = 0;
        buttonGrid.add(btnFilterViz, gbc01);

        // Row 1 - col 0
        btnFeatures = new JButton("Feature Extraction");
        styleButton(btnFeatures);
        btnFeatures.addActionListener(e -> {
            featurePanel = new FeaturePanel(featureManager);
            featurePanel.setVisible(true);
        });
        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        gbc10.insets = new Insets(6, 6, 6, 6);
        gbc10.weightx = 0.5; gbc10.ipadx = 20; gbc10.ipady = 10;
        gbc10.gridx = 0; gbc10.gridy = 1;
        buttonGrid.add(btnFeatures, gbc10);

        // Row 1 - col 1
        btnLearning = new JButton("Model Learning");
        styleButton(btnLearning);
        btnLearning.addActionListener(e -> {
            learningPanel = new LearningPanel(projectManager, learningManager);
            learningPanel.setVisible(true);
        });
        GridBagConstraints gbc11 = new GridBagConstraints();
        gbc11.fill = GridBagConstraints.HORIZONTAL;
        gbc11.insets = new Insets(6, 6, 6, 6);
        gbc11.weightx = 0.5; gbc11.ipadx = 20; gbc11.ipady = 10;
        gbc11.gridx = 1; gbc11.gridy = 1;
        buttonGrid.add(btnLearning, gbc11);

        // Row 2 - col 0
        btnEvaluation = new JButton("Evaluation");
        styleButton(btnEvaluation);
        btnEvaluation.addActionListener(e -> {
            evaluationPanel = new EvaluationPanel(projectManager, null);
            evaluationPanel.setVisible(true);
        });
        GridBagConstraints gbc20 = new GridBagConstraints();
        gbc20.fill = GridBagConstraints.HORIZONTAL;
        gbc20.insets = new Insets(6, 6, 6, 6);
        gbc20.weightx = 0.5; gbc20.ipadx = 20; gbc20.ipady = 10;
        gbc20.gridx = 0; gbc20.gridy = 2;
        buttonGrid.add(btnEvaluation, gbc20);

        // Row 2 - col 1
        btnSessions = new JButton("Sessions");
        styleButton(btnSessions);
        btnSessions.addActionListener(e -> {
            sessionPanel = new SessionGUI(projectManager);
        });
        GridBagConstraints gbc21 = new GridBagConstraints();
        gbc21.fill = GridBagConstraints.HORIZONTAL;
        gbc21.insets = new Insets(6, 6, 6, 6);
        gbc21.weightx = 0.5; gbc21.ipadx = 20; gbc21.ipady = 10;
        gbc21.gridx = 1; gbc21.gridy = 2;
        buttonGrid.add(btnSessions, gbc21);

        contentPane.add(buttonGrid, BorderLayout.CENTER);
    }

    /**
     * Applies consistent visual styling to a {@link JButton}.
     *
     * @param button the button to style
     */
    private void styleButton(JButton button) {
        button.setFont(labelFONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(buttonBGColor);
        button.setForeground(Color.WHITE);
    }
}
