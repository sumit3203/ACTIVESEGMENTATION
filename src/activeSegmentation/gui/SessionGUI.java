package activeSegmentation.gui;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import javafx.scene.control.Dialog;

import javax.swing.table.DefaultTableModel;



public class SessionGUI {
    JTextField jtf_sessionId, jtf_startTime, jtf_endTime, jtf_datasetPath, jtf_classifierOutput;
    JButton jb_add, jb_delete, jb_search, jb_viewDetail;
    JTable jt;
    JFrame frame;
    JLabel lbl_sessionId, lbl_startTime, lbl_endTime, lbl_datasetPath, lbl_classifierOutput;
    ArrayList<Session> sessionList;
    Session session;
    JButton jb_refresh;
    JButton jb_viewFeatureDetail;
    // JTextArea jta_cellText;
    public final static String driver="org.sqlite.JDBC";
	
	private Connection conn;
    String header[] = new String[] {
        "ID",
        "Session ID",
        "Start Time",
        "End Time",
        "Dataset Path",
        "Classifier Output"
    };
    DefaultTableModel dtm;
    Statement stmt;
    ResultSet rs;

    // Constructor
    public SessionGUI() {
        conn = null;
        stmt = null;
        rs = null;
        createConnection();
        createTable();
        loadData();
        mainInterface();
    }

    // Create database connection
    private void createConnection() {
        connStart("C:\\Users\\aarya\\Desktop\\gsoc23\\ACTIVESEGMENTATION\\sqliteTest.db");
    }

    boolean connStart(String dbName) {
		//connecting to database
    	String dbUrl="jdbc:sqlite:"+ dbName;
        try {
            //Class.forName("org.gjt.mm.mysql.Driver");
            Class.forName(driver);
        } catch(Exception ex) {
            IJ.log("Can't find Database driver class: " + ex);
            return false;
        }
        try {
            conn = DriverManager.getConnection(dbUrl);
            IJ.log("Connected to " + dbUrl);
            return true;
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
            return false;
        }
    }

    private static void createIMtable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `images` (\r\n" +
                " `img_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +
                " `image_id` INTEGER, \r\n" +
                " `image_name` VARCHAR(50) \r\n" +
                ");";
        lock.execute(query);
        System.out.println("IMAGES created");
    }
    
    private static void createCPtable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `class_probabilities` (\r\n" +
                " `cp_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +
                " `class_label` VARCHAR(50), \r\n" +
                " `probability` FLOAT \r\n" +
                ");";
        lock.execute(query);
        System.out.println("CLASS_PROBABILITIES created");
    }
    
    private static void createSStable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `sessions` (\r\n" +
                " `ss_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +
                " `start_time` VARCHAR(50), \r\n" +
                " `end_time` VARCHAR(50), \r\n" +
                " `dataset_path` TEXT, \r\n" +
                " `classifier_output` TEXT \r\n" +
                ");";
        lock.execute(query);
        System.out.println("SESSIONS created");
    }
    
    private static void createCLtable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `class_list` (\r\n" +
                " `class_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER,\r\n" +
                " `image_name` VARCHAR(50), \r\n" +
                " `class_label` VARCHAR(50) \r\n" +
                ");";
        lock.execute(query);
        System.out.println("CLASS_LIST created");
    }
    
    private static void createFStable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `features` (\r\n" +
                " `feature_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +
                " `feature_name` VARCHAR(50), \r\n" +
                " `feature_parameter` VARCHAR(50) \r\n" +
                ");";
        lock.execute(query);
        System.out.println("FEATURES created");
    }
    
    private static void createFVtable(Statement lock) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS `features_values` (\r\n" +
                " `fvalue_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +
                " `feature_name` VARCHAR(50), \r\n" +
                " `feature_value` FLOAT, \r\n" +
                " `image_id` INTEGER, \r\n" +
                " FOREIGN KEY(image_id) REFERENCES vectors(image_id)\r\n" +
                ");";
    
        lock.execute(query);
        System.out.println("FEATURES_VALUES created");
    }
    

    // Create sessions table if it does not exist
    private void createTable() {
        try {
            stmt = conn.createStatement();
            createCPtable(stmt);
            createSStable(stmt);
            createCLtable(stmt);
            createFStable(stmt);
            createFVtable(stmt);
            createIMtable(stmt);
            String sql = "CREATE TABLE IF NOT EXISTS sessions (\n"
                    + " ss_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " session_id INTEGER,\n"
                    + " start_time TEXT,\n"
                    + " end_time TEXT,\n"
                    + " dataset_path TEXT,\n"
                    + " classifier_output TEXT\n"
                    + ");";
            stmt.execute(sql);
            System.out.println("Table and View created successfully.");
            
            String createSessionDetailsView = "CREATE VIEW IF NOT EXISTS session_details_view AS " +
            "SELECT s.session_id, s.start_time, s.end_time, i.image_id, i.image_name " +
            "FROM sessions s " +
            "INNER JOIN images i ON s.session_id = i.session_id;";
            stmt.execute(createSessionDetailsView);

            String createClassListDetailsView = "CREATE VIEW IF NOT EXISTS class_list_details_view AS " +
            "SELECT cl.session_id, cl.image_name, cl.class_label, i.image_id " +
            "FROM class_list cl " +
            "INNER JOIN images i ON cl.session_id = i.session_id AND cl.image_name = i.image_name;";
            stmt.execute(createClassListDetailsView);

            String createFeatureDetailsView = "CREATE VIEW IF NOT EXISTS feature_details_view AS " +
            "SELECT f.session_id, f.feature_name, f.feature_parameter " +
            "FROM features f;";
            stmt.execute(createFeatureDetailsView);

            String createClassProbabilitiesView = "CREATE VIEW IF NOT EXISTS class_probabilities_view AS " +
            "SELECT cp.session_id, cp.class_label, cp.probability " +
            "FROM class_probabilities cp;";
            stmt.execute(createClassProbabilitiesView);

            String createFeatureValuesView = "CREATE VIEW IF NOT EXISTS feature_values_view AS " +
            "SELECT fv.session_id, fv.image_id, fv.feature_name, fv.feature_value " +
            "FROM features_values fv;";
            stmt.execute(createFeatureValuesView);

            System.out.println("Table and View created successfully.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Load data from database into the sessionList
    private void loadData() {
        try {
            sessionList = new ArrayList<>();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM sessions");
            while (rs.next()) {
                int ss_id = rs.getInt("ss_id");
                int sessionId = rs.getInt("session_id");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String datasetPath = rs.getString("dataset_path");
                String classifierOutput = rs.getString("classifier_output");

                sessionList.add(new Session(ss_id, sessionId, startTime, endTime, datasetPath, classifierOutput));
                System.out.println(startTime);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Populate the JTable with session data
    private void populateTable(ArrayList<Session> sessionList) {
        dtm.setRowCount(0);
        for (Session session : sessionList) {
            Object[] row = new Object[] {
                session.getSSId(),
                session.getSessionId(),
                session.getStartTime(),
                session.getEndTime(),
                session.getDatasetPath(),
                session.getClassifierOutput()
            };
            dtm.addRow(row);
        }
    }

    // Add session button listener
    ActionListener addSessionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            try {
            	int ss_id = sessionList.size() + 1;
                int sessionId = Integer.parseInt(jtf_sessionId.getText());
                String startTime = jtf_startTime.getText();
                String endTime = jtf_endTime.getText();
                String datasetPath = jtf_datasetPath.getText();
                String classifierOutput = jtf_classifierOutput.getText();

                sessionList.add(new Session(ss_id, sessionId, startTime, endTime, datasetPath, classifierOutput));
                insertData(sessionId, startTime, endTime, datasetPath, classifierOutput);

                populateTable(sessionList);

                // Clear input fields
                jtf_sessionId.setText("");
                jtf_startTime.setText("");
                jtf_endTime.setText("");
                jtf_datasetPath.setText("");
                jtf_classifierOutput.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid session ID.", "Invalid Session ID",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    
    ActionListener viewFeatureDetailListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = jt.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) jt.getValueAt(selectedRow, 1); // Session ID from the selected row
                System.out.println("sessionId = " + sessionId);
                ArrayList<FeatureDetail> featureList = getFeatureListBySessionId(sessionId);

                JFrame featureDetailFrame = new JFrame("Feature Detail");
                featureDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                // Create and populate a new table to show feature details
                Object[][] featureListData = new Object[featureList.size()][3];
                for (int i = 0; i < featureList.size(); i++) {
                    FeatureDetail featureItem = featureList.get(i);
                    featureListData[i] = new Object[] {
                        featureItem.getSessionId(),
                        featureItem.getFeatureName(),
                        featureItem.getFeatureParameter()
                    };
                }

                String[] featureListHeader = new String[] {
                    "Session ID",
                    "Feature Name",
                    "Feature Parameter"
                };

                JTable featureListTable = new JTable(featureListData, featureListHeader);
                JScrollPane featureListScrollPane = new JScrollPane(featureListTable);
                featureListTable.setFillsViewportHeight(true);

                featureDetailFrame.getContentPane().add(featureListScrollPane);
                featureDetailFrame.pack();
                featureDetailFrame.setVisible(true);

                // Show the table in a dialog
                featureDetailFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        // Handle any cleanup or actions when the user closes the "Feature Detail" GUI
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "Please select a session to view details.", "No Session Selected",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    ActionListener viewSessionDetailListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = jt.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) jt.getValueAt(selectedRow, 1); // Session ID from the selected row
                System.out.println("sessionId = " + sessionId);
                ArrayList<ClassList> classList = getClassListBySessionId(sessionId);
    
                // Populate a new table to show session details
                Object[][] classListData = new Object[classList.size()][4];
                for (int i = 0; i < classList.size(); i++) {
                    ClassList classItem = classList.get(i);
                    double classProbability = getClassProbability(sessionId, classItem.getImageLabel());
                    classListData[i] = new Object[] {
                        classItem.getSessionId(),
                        classItem.getImageName(),
                        classItem.getImageLabel(),
                        classProbability
                    };
                }
    
                String[] classListHeader = new String[] {
                    "Session ID",
                    "Image Name",
                    "Class Label",
                    "Class Probability"
                };
    
                DefaultTableModel dtmClassList = new DefaultTableModel(classListData, classListHeader);
                JTable classListTable = new JTable(dtmClassList);
    
                JScrollPane classListScrollPane = new JScrollPane(classListTable);
                classListTable.setFillsViewportHeight(true);
    
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(classListScrollPane, BorderLayout.CENTER);
    
                JButton viewFeatureValuesButton = new JButton("View Feature Values");
                viewFeatureValuesButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int selectedRow = classListTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String imageName = (String) classListTable.getValueAt(selectedRow, 1); // image Name from the selected row
                        int imageId = getImageId(sessionId, imageName); // Image ID from the selected row
                        System.out.println("sessionId = " + sessionId);
                        System.out.println("imageId = " + imageId);
                        ArrayList<FeatureValue> featureValues = getFeatureValues(sessionId, imageId);
                        Object[][] featureValuesData = new Object[featureValues.size()][3];
                        for (int i = 0; i < featureValues.size(); i++) {
                            FeatureValue featureValue = featureValues.get(i);
                            featureValuesData[i] = new Object[] {
                                featureValue.getSessionId(),
                                featureValue.getFeatureName(),
                                featureValue.getFeatureValue()
                            };
                        }
                        String[] featureValuesHeader = new String[] {
                            "Session ID",
                            "Feature Name",
                            "Feature Value"
                        };
                        JTable featureValuesTable = new JTable(featureValuesData, featureValuesHeader);
                        JScrollPane featureValuesScrollPane = new JScrollPane(featureValuesTable);
                        featureValuesTable.setFillsViewportHeight(true);
                        JFrame featureValuesFrame = new JFrame(imageName + " values");
                        featureValuesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        featureValuesFrame.getContentPane().add(featureValuesScrollPane);
                        featureValuesFrame.pack();
                        featureValuesFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select an image to view feature values.", "No Image Selected",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    }
                });
    
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(viewFeatureValuesButton);
                panel.add(buttonPanel, BorderLayout.SOUTH);
    
                // Create a new JFrame for the "Session Detail" GUI
                JFrame sessionDetailFrame = new JFrame("Session Detail");
                sessionDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                sessionDetailFrame.getContentPane().add(panel);
                sessionDetailFrame.pack();
                sessionDetailFrame.setVisible(true);
    
                sessionDetailFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        // Handle any cleanup or actions when the user closes the "Session Detail" GUI
                        // For example, enable interaction with the parent frame here if needed.
                        // or close feature_values GUI
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "Please select a session to view details.", "No Session Selected",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    private int getImageId(int sessionId, String imageName) {
        int imageId = -1; // Default value if not found
        try {
            String sql = "SELECT image_id FROM session_details_view WHERE session_id = ? AND image_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, imageName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                imageId = rs.getInt("image_id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return imageId;
    }

    // Delete session button listener
    ActionListener deleteSessionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = jt.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) jt.getValueAt(selectedRow, 1);
                
                // Show confirmation prompt
                int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this session?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                
                if (confirmation == JOptionPane.YES_OPTION) {
                    deleteData(sessionId);
                    sessionList.remove(selectedRow);
                    populateTable(sessionList);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a row to delete.", "No Row Selected",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    // Search session button listener
    ActionListener searchSessionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String searchQuery = JOptionPane.showInputDialog(null, "Enter a session ID to search:",
                    "Search Session", JOptionPane.PLAIN_MESSAGE);
            if (searchQuery != null && !searchQuery.isEmpty()) {
                ArrayList<Session> searchResult = searchSession(Integer.parseInt(searchQuery));
                populateTable(searchResult);
            }
        }
    };
    
 // Refresh button listener
    ActionListener refreshListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            loadData(); // Reload data from the database
            populateTable(sessionList); // Update the table with new data
        }
    };
    
 // Load values into the GUI
    private void loadValues(int sessionId, String startTime, String endTime, String datasetPath, String classifierOutput) {
        jtf_sessionId.setText(String.valueOf(sessionId));
        jtf_startTime.setText(startTime);
        jtf_endTime.setText(endTime);
        jtf_datasetPath.setText(datasetPath);
        jtf_classifierOutput.setText(classifierOutput);
    }

    // Insert session data into database
    private void insertData(int sessionId, String startTime, String endTime, String datasetPath,
            String classifierOutput) {
        try {
            String sql = "INSERT INTO sessions (session_id, start_time, end_time, dataset_path, classifier_output) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, startTime);
            pstmt.setString(3, endTime);
            pstmt.setString(4, datasetPath);
            pstmt.setString(5, classifierOutput);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Delete session data from database
    private void deleteData(int sessionId) {
        try {
            // Use a transaction to ensure atomicity of the operation
            conn.setAutoCommit(false);
    
            // delete related records in other tables
            String[] deleteQueries = {
                "DELETE FROM class_list WHERE session_id = ?",
                "DELETE FROM class_probabilities WHERE session_id = ?",
                "DELETE FROM features WHERE session_id = ?",
                "DELETE FROM features_values WHERE session_id = ?",
                "DELETE FROM images WHERE session_id = ?",
                "DELETE FROM sessions WHERE session_id = ?"
            };
    
            // Create an array of prepared statements
            PreparedStatement[] deleteStatements = new PreparedStatement[deleteQueries.length];
    
            for (int i = 0; i < deleteQueries.length; i++) {
                deleteStatements[i] = conn.prepareStatement(deleteQueries[i]);
                deleteStatements[i].setInt(1, sessionId);
                deleteStatements[i].executeUpdate();
                deleteStatements[i].close();
            }
    
            // Commit the transaction
            conn.commit();
        } catch (Exception e) {
            // Handle any exceptions, and roll back the transaction if an error occurs
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            System.out.println(e.getMessage());
        } finally {
            try {
                // Reset auto-commit mode
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Failed to set auto-commit mode: " + ex.getMessage());
            }
        }
    }
    
    
 // Load class_list values into the GUI
    private ArrayList<ClassList> getClassListBySessionId(int sessionId) {
        ArrayList<ClassList> classList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM class_list_details_view WHERE session_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String imageName = rs.getString("image_name");
                String imageLabel = rs.getString("class_label");
                classList.add(new ClassList(sessionId, imageName, imageLabel));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return classList;
    }

    // Fetch class probabilities based on session_id and class_label
private double getClassProbability(int sessionId, String classLabel) {
    double probability = 0.0;
    try {
        String sql = "SELECT probability FROM class_probabilities_view WHERE session_id = ? AND class_label = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, sessionId);
        pstmt.setString(2, classLabel);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            probability = rs.getDouble("probability");
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return probability;
}


    // fetch featureDetailsbySessionID
    private ArrayList<FeatureDetail> getFeatureListBySessionId(int sessionId) {
        ArrayList<FeatureDetail> featureList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM feature_details_view WHERE session_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String featureName = rs.getString("feature_name");
                String featureParameter = rs.getString("feature_parameter");
                featureList.add(new FeatureDetail(sessionId, featureName, featureParameter));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return featureList;
    }
    
 // Fetch feature values based on sessionId and imageId
    private ArrayList<FeatureValue> getFeatureValues(int sessionId, int imageId) {
        ArrayList<FeatureValue> featureValues = new ArrayList<>();
        try {
            String sql = "SELECT feature_name, feature_value FROM feature_values_view WHERE session_id = ? AND image_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, imageId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String featureName = rs.getString("feature_name");
                String featureValue = rs.getString("feature_value");
                featureValues.add(new FeatureValue(sessionId, featureName, featureValue));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return featureValues;
    }

    // Search session by session ID in the database
    private ArrayList<Session> searchSession(int sessionId) {
        ArrayList<Session> searchResult = new ArrayList<>();
        try {
            String sql = "SELECT * FROM sessions WHERE session_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int ss_id = rs.getInt("ss_id");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String datasetPath = rs.getString("dataset_path");
                String classifierOutput = rs.getString("classifier_output");

                searchResult.add(new Session(ss_id, sessionId, startTime, endTime, datasetPath, classifierOutput));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return searchResult;
    }

    // Initialize the main user interface
    private void mainInterface() {
        frame = new JFrame("Sessions Database");
        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dtm = new DefaultTableModel(header, 0);
        jt = new JTable(dtm);
        JTextArea jta_cellText = new JTextArea(10, 30);
        jta_cellText.setLineWrap(true);
        jta_cellText.setWrapStyleWord(true);
        JScrollPane jsp_cellText = new JScrollPane(jta_cellText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp_cellText.setBounds(180, 460, 350, 200);

        // Add ListSelectionListener to the JTable
        jt.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jt.getSelectedRow();
                int selectedColumn = jt.getSelectedColumn();
                if (selectedRow != -1 && selectedColumn != -1) {
                    Object cellValue = dtm.getValueAt(selectedRow, selectedColumn);
                    if (cellValue != null) {
                        jta_cellText.setText(cellValue.toString());
                        jta_cellText.setEditable(true);
                    } else {
                        jta_cellText.setText("");
                        jta_cellText.setEditable(false);
                    }
                }
            }
        });
        JScrollPane jsp = new JScrollPane(jt);
        jsp.setBounds(20, 20, 750, 300);
        
        jb_viewDetail = new JButton("View Session Detail");
        jb_viewDetail.setBounds(300, 420, 150, 30);
        jb_viewDetail.addActionListener(viewSessionDetailListener);
        frame.add(jb_viewDetail);
        
        jb_viewFeatureDetail = new JButton("View Feature Detail");
        jb_viewFeatureDetail.setBounds(460, 420, 150, 30);
        jb_viewFeatureDetail.addActionListener(viewFeatureDetailListener);
        frame.add(jb_viewFeatureDetail);
        
//        jb_viewFeatureDetail = new JButton("View Feature Detail");
//        jb_viewFeatureDetail.setBounds(660, 420, 150, 30);
//        jb_viewFeatureDetail.addActionListener(viewFeatureDetailListener);
//        frame.add(jb_viewFeatureDetail);

        lbl_sessionId = new JLabel("Session ID");
        lbl_sessionId.setBounds(20, 340, 100, 20);
        jtf_sessionId = new JTextField();
        jtf_sessionId.setBounds(130, 340, 200, 20);

        lbl_startTime = new JLabel("Start Time");
        lbl_startTime.setBounds(20, 380, 100, 20);
        jtf_startTime = new JTextField();
        jtf_startTime.setBounds(130, 380, 200, 20);

        lbl_endTime = new JLabel("End Time");
        lbl_endTime.setBounds(20, 420, 100, 20);
        jtf_endTime = new JTextField();
        jtf_endTime.setBounds(130, 420, 200, 20);

        lbl_datasetPath = new JLabel("Dataset Path");
        lbl_datasetPath.setBounds(20, 460, 100, 20);
        jtf_datasetPath = new JTextField();
        jtf_datasetPath.setBounds(130, 460, 200, 20);

        lbl_classifierOutput = new JLabel("Classifier Output");
        lbl_classifierOutput.setBounds(20, 500, 100, 20);
        jtf_classifierOutput = new JTextField();
        jtf_classifierOutput.setBounds(130, 500, 200, 20);

        // jb_add = new JButton("Add");
        // jb_add.setBounds(180, 380, 100, 30);
        // jb_add.addActionListener(addSessionListener);

        jb_delete = new JButton("Delete");
        jb_delete.setBounds(300, 380, 100, 30);
        jb_delete.addActionListener(deleteSessionListener);

        jb_search = new JButton("Search");
        jb_search.setBounds(180, 420, 100, 30);
        jb_search.addActionListener(searchSessionListener);
        
        jb_refresh = new JButton("Refresh");
        jb_refresh.setBounds(420, 380, 100, 30);
        jb_refresh.addActionListener(refreshListener);
        frame.add(jb_refresh);
        frame.add(jsp_cellText);

        frame.add(jsp);
//        frame.add(jb_add);
        frame.add(jb_delete);
        frame.add(jb_search);
        // DEBUG
//        frame.add(lbl_sessionId);
//        frame.add(jtf_sessionId);
//        frame.add(lbl_startTime);
//        frame.add(jtf_startTime);
//        frame.add(lbl_endTime);
//        frame.add(jtf_endTime);
//        frame.add(lbl_datasetPath);
//        frame.add(jtf_datasetPath);
//        frame.add(lbl_classifierOutput);
//        frame.add(jtf_classifierOutput);

        frame.setLayout(null);
        frame.setVisible(true);
        populateTable(sessionList);
    }
    
 // FeatureValue Class
    class FeatureValue {
        private int sessionId;
        private String featureName;
        private String featureValue;

        public FeatureValue(int sessionId, String featureName, String featureValue) {
            this.sessionId = sessionId;
            this.featureName = featureName;
            this.featureValue = featureValue;
        }

        public int getSessionId() {
            return sessionId;
        }

        public String getFeatureName() {
            return featureName;
        }

        public String getFeatureValue() {
            return featureValue;
        }
    }
    
    // FeatureDetail Class
    class FeatureDetail {
        private int sessionId;
        private String featureName;
        private String featureParameter;

        public FeatureDetail(int sessionId, String featureName, String featureParameter) {
            this.sessionId = sessionId;
            this.featureName = featureName;
            this.featureParameter = featureParameter;
        }

        public int getSessionId() {
            return sessionId;
        }

        public String getFeatureName() {
            return featureName;
        }

        public String getFeatureParameter() {
            return featureParameter;
        }
    }
    
 // ClassList class
    class ClassList {
        private int sessionId;
        private String imageName;
        private String imageLabel;
        private int imageId; // Add the imageId field

        public ClassList(int sessionId, String imageName, String imageLabel) {
            this.sessionId = sessionId;
            this.imageName = imageName;
            this.imageLabel = imageLabel;
        }

        public ClassList(int sessionId, String imageName, String imageLabel, int imageId) {
        this.sessionId = sessionId;
        this.imageName = imageName;
        this.imageLabel = imageLabel;
        this.imageId = imageId;
    }

        public int getSessionId() {
            return sessionId;
        }

        public String getImageName() {
            return imageName;
        }

        public String getImageLabel() {
            return imageLabel;
        }

        public int getImageId() {
        return imageId;
    }
    }

    // Session class
    class Session {
        private int ss_id;
        private int sessionId;
        private String startTime;
        private String endTime;
        private String datasetPath;
        private String classifierOutput;

        public Session(int ss_id, int sessionId, String startTime, String endTime, String datasetPath, String classifierOutput) {
            this.ss_id = ss_id;
            this.sessionId = sessionId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.datasetPath = datasetPath;
            this.classifierOutput = classifierOutput;
        }

        public int getSSId() {
            return ss_id;
        }
        
        public int getSessionId() {
            return sessionId;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getDatasetPath() {
            return datasetPath;
        }

        public String getClassifierOutput() {
            return classifierOutput;
        }
    }

    // Main method
    public static void main(String[] args) {
        new SessionGUI();
    }
}