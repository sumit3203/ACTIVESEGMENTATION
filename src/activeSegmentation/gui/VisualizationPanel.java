package activeSegmentation.gui;

import activeSegmentation.learning.weka.WekaDataSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * VisualizationPanel provides the user interface for selecting an ARFF file
 * and visualizing machine learning evaluation metrics such as ROC Curve,
 * Precision-Recall Curve, and generating a detailed report. The panel integrates
 * Weka's visualization and analysis functions.
 */
public class VisualizationPanel extends JPanel {
    private JButton chooseFileButton;
    private JButton rocButton;
    private JButton precisionRecallButton;
    private JButton reportButton;
    private JPanel chartPanelContainer; // Panel for displaying charts
    private JTextArea reportArea; // Added JTextArea for report display
    private String arffFilePath; // Field to store the selected file path
    private final Color buttonBGColor = new Color(160, 160, 160);

    /**
     * Constructs the VisualizationPanel and initializes the UI components.
     */
    public VisualizationPanel() {
        setLayout(new BorderLayout());

        // Create and configure buttons
        JPanel buttonPanel = new JPanel();
        chooseFileButton = new JButton("Choose ARFF File");
        rocButton = new JButton("Visualize ROC Curve");
        precisionRecallButton = new JButton("Visualize Precision-Recall Curve");
        reportButton = new JButton("Generate Report");

        configureButton(chooseFileButton);
        configureButton(rocButton);
        configureButton(precisionRecallButton);
        configureButton(reportButton);

        // Add action listeners to buttons
        chooseFileButton.addActionListener(e -> selectFile());
        rocButton.addActionListener(e -> visualizeROC());
        precisionRecallButton.addActionListener(e -> visualizePrecisionRecall());
        reportButton.addActionListener(e -> generateReport());

        // Add buttons to panel
        buttonPanel.add(chooseFileButton);
        buttonPanel.add(rocButton);
        buttonPanel.add(precisionRecallButton);
        buttonPanel.add(reportButton);

        // Panel for displaying charts and report
        chartPanelContainer = new JPanel(new BorderLayout());

        // Initialize the report area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        JScrollPane reportScrollPane = new JScrollPane(reportArea);

        // Add panels to the main panel
        add(buttonPanel, BorderLayout.NORTH);
        add(chartPanelContainer, BorderLayout.CENTER);
    }

    /**
     * Configures the appearance of the given button.
     * @param button The button to configure.
     */
    private void configureButton(JButton button) {
        Font labelFONT = new Font("Arial", Font.BOLD, 12);
        button.setFont(labelFONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(buttonBGColor);
        button.setForeground(Color.WHITE);
    }

    /**
     * Opens a file chooser dialog for selecting an ARFF file and updates the file path.
     */
    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            arffFilePath = selectedFile.getAbsolutePath();
            // Validate file extension
            if (!selectedFile.getName().endsWith(".arff")) {
                JOptionPane.showMessageDialog(this, "Please select a valid .arff file.", "Invalid File Type", JOptionPane.WARNING_MESSAGE);
//            } else {
//                // Display the selected file path
//                JOptionPane.showMessageDialog(this, "Selected file: " + arffFilePath, "File Selected", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Visualizes the ROC Curve using the selected ARFF file.
     * Displays an error message if no file is selected or if an error occurs during visualization.
     */
    private void visualizeROC() {
        if (arffFilePath == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Load dataset and build classifier
            WekaDataSet wekaDataSet = new WekaDataSet(arffFilePath);
            Instances data = wekaDataSet.getDataset();
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Build and evaluate classifier
            Classifier classifier = new J48();
            classifier.buildClassifier(data);

            // Determine the number of folds for cross-validation
            int numInstances = data.numInstances();
            int numFolds = Math.min(10, numInstances);
            if (numFolds < 2) {
                throw new IllegalArgumentException("Number of folds must be greater than 1");
            }

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, new java.util.Random(1));

            // Generate ROC Curve data
            ThresholdCurve tc = new ThresholdCurve();
            Instances result = tc.getCurve(eval.predictions());

            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("ROC Curve");

            // Add data points to the series
            for (int i = 0; i < result.numInstances(); i++) {
                double falsePositiveRate = result.instance(i).value(result.attribute("False Positive Rate"));
                double truePositiveRate = result.instance(i).value(result.attribute("True Positive Rate"));
                series.add(falsePositiveRate, truePositiveRate);
            }

            dataset.addSeries(series);

            // Create and display the ROC Curve chart
            JFreeChart rocChart = ChartFactory.createXYLineChart(
                    "ROC Curve",
                    "False Positive Rate",
                    "True Positive Rate",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize the chart's appearance
            XYPlot plot = rocChart.getXYPlot();
            plot.setDomainGridlinePaint(Color.BLACK); // Set grid color to black
            plot.setRangeGridlinePaint(Color.BLACK);
            plot.setBackgroundPaint(Color.WHITE);     // Set background color to white

            ChartPanel chartPanel = new ChartPanel(rocChart);
            chartPanelContainer.removeAll();
            chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error visualizing ROC curve: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Visualizes the Precision-Recall Curve using the selected ARFF file.
     * Displays an error message if no file is selected or if an error occurs during visualization.
     */
    private void visualizePrecisionRecall() {
        if (arffFilePath == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Load dataset from ARFF file
            WekaDataSet wekaDataSet = new WekaDataSet(arffFilePath);
            Instances data = wekaDataSet.getDataset();
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Build and evaluate classifier
            Classifier classifier = new J48();
            classifier.buildClassifier(data);

            // Determine the number of folds
            int numInstances = data.numInstances();
            int numFolds = Math.min(10, numInstances);
            if (numFolds < 2) {
                throw new IllegalArgumentException("Number of folds must be greater than 1");
            }

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, new java.util.Random(1));

            // Generate Precision-Recall Curve data
            ThresholdCurve tc = new ThresholdCurve();
            Instances result = tc.getCurve(eval.predictions());

            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("Precision-Recall Curve");

            // Add data points to the series
            for (int i = 0; i < result.numInstances(); i++) {
                double recall = result.instance(i).value(result.attribute("Recall"));
                double precision = result.instance(i).value(result.attribute("Precision"));
                series.add(recall, precision);
            }

            dataset.addSeries(series);

            // Create and display the Precision-Recall Curve chart
            JFreeChart prChart = ChartFactory.createXYLineChart(
                    "Precision-Recall Curve",
                    "Recall",
                    "Precision",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize the chart's appearance
            XYPlot plot = prChart.getXYPlot();
            plot.setDomainGridlinePaint(Color.BLACK); // Set grid color to black
            plot.setRangeGridlinePaint(Color.BLACK);  // Set grid color to black
            plot.setBackgroundPaint(Color.WHITE);     // Set background color to white

            ChartPanel chartPanel = new ChartPanel(prChart);
            chartPanelContainer.removeAll();
            chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error visualizing Precision-Recall curve: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Generates a report based on the selected ARFF file.
     * Displays the report in the JTextArea.
     */
    private void generateReport() {
        if (arffFilePath == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Load dataset from ARFF file
            WekaDataSet wekaDataSet = new WekaDataSet(arffFilePath);
            Instances data = wekaDataSet.getDataset();
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Build and evaluate classifier
            Classifier classifier = new J48();
            classifier.buildClassifier(data);

            // Determine the number of folds
            int numInstances = data.numInstances();
            int numFolds = Math.min(10, numInstances);
            if (numFolds < 2) {
                throw new IllegalArgumentException("Number of folds must be greater than 1");
            }

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(classifier, data, numFolds, new java.util.Random(1));

            // Generate report
            StringBuilder report = new StringBuilder();
            report.append("Dataset: ").append(arffFilePath).append("\n\n");
            report.append("Summary Statistics:\n").append(eval.toSummaryString()).append("\n\n");
            report.append("Detailed Accuracy By Class:\n").append(eval.toClassDetailsString()).append("\n\n");
            report.append("Confusion Matrix:\n").append(eval.toMatrixString()).append("\n");

            // Display the report in the JTextArea
            reportArea.setText(report.toString());
            chartPanelContainer.removeAll();
            chartPanelContainer.add(new JScrollPane(reportArea), BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

            // Add Export button to the GenerateReport panel
            JButton exportButton = new JButton("Export Report");
            exportButton.addActionListener(e -> exportReportAsText(String.valueOf(report)));
            chartPanelContainer.add(exportButton, BorderLayout.SOUTH);

            // Revalidate and repaint the chartPanelContainer to show the Export button
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to save the report as a text file
    private void exportReportAsText(String report) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setSelectedFile(new File("report.txt"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            saveReportToTextFile(fileToSave.getAbsolutePath(), report);
        }
    }

    // Method to actually write the content to a file
    private void saveReportToTextFile(String filePath, String report) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(report);
            JOptionPane.showMessageDialog(this, "Report saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    /**
//     * Main method to run the VisualizationPanel as a standalone application.
//     */
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Visualization Panel");
//            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//            frame.setSize(800, 600);
//            frame.setLocationRelativeTo(null); // Center the frame
//            frame.add(new VisualizationPanel());
//            frame.setVisible(true);
//        });
//    }
}
