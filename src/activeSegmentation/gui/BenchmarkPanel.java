package activeSegmentation.gui;

import activeSegmentation.ASCommon;
import activeSegmentation.benchmark.ProfilingManager;
import activeSegmentation.benchmark.ProfilingManager.ComparisonRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 BenchmarkPanel
 CPU vs GPU Comparison UI.
 Displays a side by side comparison of filter performance.
 */
public class BenchmarkPanel extends JFrame {

    private static final String[] COLUMNS = {"Filter Name", "CPU (ms)", "GPU (ms)", "Speedup"};
    private static final Color COLOR_CPU = new Color(52, 152, 219);
    private static final Color COLOR_GPU = new Color(46, 204, 113);
    private static final Color COLOR_SPEEDUP = new Color(155, 89, 182);
    private static final Color COLOR_HEADER = new Color(44, 62, 80);
    private static final Color COLOR_SUMMARY_BG = new Color(236, 240, 241);

    private final String exportDir;

    public BenchmarkPanel(String exportDir) {
        this.exportDir = exportDir;
        buildUI();
    }

    private void buildUI() {
        List<ComparisonRecord> results = ProfilingManager.getComparisonResults();
        
        setTitle("Performance Comparison: CPU vs GPU");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 480);
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit().getImage(BenchmarkPanel.class.getResource("logo.png")));

        // Header
        JLabel header = new JLabel("CPU vs GPU Parallel Benchmark", SwingConstants.CENTER);
        header.setFont(ASCommon.largeFONT);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        //Table Model
        DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        long totalCpu = 0;
        long totalGpu = 0;

        for (ComparisonRecord r : results) {
            String cpuStr = r.cpuTime > -1 ? String.valueOf(r.cpuTime) : "N/A";
            String gpuStr = r.gpuTime > -1 ? String.valueOf(r.gpuTime) : "N/A";
            model.addRow(new Object[]{r.filterName, cpuStr, gpuStr, r.getSpeedup()});
            
            if (r.cpuTime > 0) totalCpu += r.cpuTime;
            if (r.gpuTime > 0) totalGpu += r.gpuTime;
        }

        // Table Rendering
        JTable table = new JTable(model);
        table.setFont(ASCommon.FONT);
        table.setRowHeight(28);
        table.getTableHeader().setFont(ASCommon.FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(COLOR_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 249));
                    setForeground(Color.DARK_GRAY);
                }
                if (col == 1 && value != null && !value.equals("N/A")) setForeground(COLOR_CPU);
                if (col == 2 && value != null && !value.equals("N/A")) setForeground(COLOR_GPU);
                if (col == 3 && value != null && !value.equals("N/A")) {
                    setForeground(COLOR_SPEEDUP);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Summary Panel
        double totalSpeedup = (totalGpu > 0 && totalCpu > 0) ? (double)totalCpu/totalGpu : 0;
        String speedupText = totalSpeedup > 0 ? String.format("%.2fx Faster", totalSpeedup) : "N/A";
        
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3));
        summaryPanel.setBackground(COLOR_SUMMARY_BG);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        summaryPanel.add(createSummaryLabel("Total CPU: " + totalCpu + " ms", COLOR_CPU));
        summaryPanel.add(createSummaryLabel("Total GPU: " + totalGpu + " ms", COLOR_GPU));
        summaryPanel.add(createSummaryLabel("AVG Speedup: " + speedupText, COLOR_SPEEDUP));

        // Buttons
        JButton exportBtn = new JButton("Export CSV");
        final long finalTotalCpu = totalCpu;
        final long finalTotalGpu = totalGpu;
        exportBtn.addActionListener(e -> exportToCSV(results, finalTotalCpu, finalTotalGpu));
        
        JButton clearBtn = new JButton("Clear All");
        clearBtn.setBackground(new Color(231, 76, 60));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.addActionListener(e -> {
            ProfilingManager.clear();
            dispose();
            JOptionPane.showMessageDialog(null, "Benchmark data cleared.");
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(COLOR_SUMMARY_BG);
        btnPanel.add(clearBtn);
        btnPanel.add(exportBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(summaryPanel, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel createSummaryLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(ASCommon.FONT.deriveFont(Font.BOLD));
        l.setForeground(color);
        return l;
    }

    private void exportToCSV(List<ComparisonRecord> results, long totalCpu, long totalGpu) {
        String path = exportDir + File.separator + "performance_comparison.csv";
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("Filter,CPU(ms),GPU(ms),Speedup\n");
            for (ComparisonRecord r : results) {
                fw.write(String.format("%s,%d,%d,%s\n", r.filterName, r.cpuTime, r.gpuTime, r.getSpeedup()));
            }
            fw.write(String.format("TOTAL,%d,%d,%.2fx\n", totalCpu, totalGpu, (totalGpu>0?(double)totalCpu/totalGpu:0)));
            JOptionPane.showMessageDialog(this, "Exported to: " + path);
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}
