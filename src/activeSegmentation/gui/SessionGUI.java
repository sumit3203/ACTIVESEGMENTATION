package activeSegmentation.gui;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
//import ij.*;


import javax.swing.table.DefaultTableModel;

import activeSegmentation.ASCommon;
import activeSegmentation.DbManager;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.session.FeatureDetail;
import activeSegmentation.session.FeatureValue;
import activeSegmentation.session.Session;


/**
 * 
 * @author prodanov
 *
 */
public class SessionGUI extends JFrame implements ASCommon {
    JTextField jtf_sessionId, jtf_startTime, jtf_endTime, jtf_datasetPath, jtf_classifierOutput;
    JButton jb_add, jb_delete, jb_search, jb_viewDetail;
    JTable table;
    //JFrame frame;
    JLabel lbl_sessionId, lbl_startTime, lbl_endTime, lbl_datasetPath, lbl_classifierOutput;
    ArrayList<Session> sessionList= new ArrayList<>();
    Session session;
    JButton jb_refresh;
    JButton jb_viewFeatureDetail;
    // JTextArea jta_cellText;
 
	
	private DbManager man=new DbManager();
	
    String header[] = new String[] {
        "ID",
        "Session ID",
        "Start Time",
        "End Time",
        "Dataset Path",
        "Classifier Output"
    };
    
    DefaultTableModel dtm;
 
    
    

    // Constructor
    public SessionGUI(ProjectManager projectManager) {
    	if (projectManager!=null) {
    		ProjectInfo pi=projectManager.getMetaInfo();
    		String sessFile=pi.getSessionFile();
    		man.loadDB(sessFile);
    	} else {
    		man.loadDB("C:\\GitHub\\ACTIVESEGMENTATION\\sqliteTest.db" );
    	}
        // TODO move to a SessionManager class
        //createTable();
        loadData(sessionList);
        mainInterface();
    }

 
   

    // Load data from database into the sessionList
    private void loadData(ArrayList<Session> sessionList) {
        try {
            //sessionList = new ArrayList<>();
            final Connection conn=man.getConnection();
            final Statement stmt = conn.createStatement();
            if (conn!=null) {
            	final ResultSet rs = stmt.executeQuery("SELECT * FROM sessions");
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
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
            man.logError(e);
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
        @Override
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
        @Override
		public void actionPerformed(ActionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) table.getValueAt(selectedRow, 1); // Session ID from the selected row
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
                    @Override
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
        @Override
		public void actionPerformed(ActionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) table.getValueAt(selectedRow, 1); // Session ID from the selected row
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
                    @Override
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
    
                // Create a new JFrame for the "Session Details" GUI
                JFrame sessionDetailFrame = new JFrame("Session Details");
                sessionDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                sessionDetailFrame.getContentPane().add(panel);
                sessionDetailFrame.pack();
                sessionDetailFrame.setVisible(true);
    
                sessionDetailFrame.addWindowListener(new WindowAdapter() {
                    @Override
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
            Connection conn=man.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, imageName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                imageId = rs.getInt("image_id");
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
            man.logError(e);
        }
        return imageId;
    }

    // Delete session button listener
    ActionListener deleteSessionListener = new ActionListener() {
        @Override
		public void actionPerformed(ActionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) table.getValueAt(selectedRow, 1);
                
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
        @Override
		public void actionPerformed(ActionEvent e) {
            String searchQuery = JOptionPane.showInputDialog(null, "Enter a session ID to search:",
                    "Search Session", JOptionPane.PLAIN_MESSAGE);
            if (searchQuery != null && !searchQuery.isEmpty()) {
                ArrayList<Session> searchResult = fetchSession(Integer.parseInt(searchQuery));
                populateTable(searchResult);
            }
        }
    };
    
 // Refresh button listener
    ActionListener refreshListener = new ActionListener() {
        @Override
		public void actionPerformed(ActionEvent e) {
            loadData(sessionList); // Reload data from the database
            populateTable(sessionList); // Update the table with new data
        }
    };
    


    // Insert session data into database
    private void insertData(int sessionId, String startTime, String endTime, String datasetPath,
            String classifierOutput) {
        try {
            String sql = "INSERT INTO sessions (session_id, start_time, end_time, dataset_path, classifier_output) VALUES (?, ?, ?, ?, ?)";
            Connection conn=man.getConnection();
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
    	Connection conn=man.getConnection();
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
        } catch (SQLException e) {
            // Handle any exceptions, and roll back the transaction if an error occurs
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: ");
                man.logError(ex);
            }
            System.out.println(e.getMessage());
        } finally {
            try {
                // Reset auto-commit mode
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Failed to set auto-commit mode: ");
                man.logError(ex);
            }
        }
    }
    
    
 // Load class_list values into the GUI
    private ArrayList<ClassList> getClassListBySessionId(int sessionId) {
        ArrayList<ClassList> classList = new ArrayList<>();
        Connection conn=man.getConnection();
        try {
            String sql = "SELECT image_name, class_label FROM class_list_details_view WHERE session_id = ?";
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
	    Connection conn=man.getConnection();
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
	       // System.out.println(e.getMessage());
	    	 man.logError(e);
	    }
	    return probability;
	}


    // fetch featureDetailsbySessionID
    private ArrayList<FeatureDetail> getFeatureListBySessionId(int sessionId) {
        ArrayList<FeatureDetail> featureList = new ArrayList<>();
        Connection conn=man.getConnection();
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
            //System.out.println(e.getMessage());
        	 man.logError(e);
        }
        return featureList;
    }
    
    // Fetch feature values based on sessionId and imageId
    private ArrayList<FeatureValue> getFeatureValues(int sessionId, int imageId) {
        ArrayList<FeatureValue> featureValues = new ArrayList<>();
        Connection conn=man.getConnection();
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
            //System.out.println(e.getMessage());
        	 man.logError(e);
        }
        return featureValues;
    }

    // Search session by session ID in the database
    private ArrayList<Session> fetchSession(int sessionId) {
        ArrayList<Session> searchResult = new ArrayList<>();
        Connection conn=man.getConnection();
        try {
            String sql = "SELECT * FROM sessions WHERE session_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int ss_id = rs.getInt("ss_id");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                String datasetPath = rs.getString("dataset_path");
                String classifierOutput = rs.getString("classifier_output");

                searchResult.add(new Session(ss_id, sessionId, startTime, endTime, datasetPath, classifierOutput));
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        	 man.logError(e);
        }
        return searchResult;
    }

    
    
    // Initialize the main user interface
    private void mainInterface() {
        //frame = new JFrame("Sessions Database");
    	setTitle("Sessions Database");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        dtm = new DefaultTableModel(header, 0);
        table = new JTable(dtm);
        JTextArea jta_cellText = new JTextArea(10, 30);
        jta_cellText.setLineWrap(true);
        jta_cellText.setWrapStyleWord(true);
        JScrollPane jsp_cellText = new JScrollPane(jta_cellText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp_cellText.setBounds(180, 460, 350, 200);

        // Add ListSelectionListener to the JTable
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                int selectedColumn = table.getSelectedColumn();
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
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBounds(20, 20, 750, 300);

        jb_viewDetail = new JButton("View Data");
        jb_viewDetail.setBounds(180, 420, 160, 30);
        configureButton(jb_viewDetail);
        jb_viewDetail.addActionListener(viewSessionDetailListener);
        getContentPane().add(jb_viewDetail);

        jb_viewFeatureDetail = new JButton("View Features");
        jb_viewFeatureDetail.setBounds(370, 420, 160, 30);
        configureButton(jb_viewFeatureDetail);
        jb_viewFeatureDetail.addActionListener(viewFeatureDetailListener);
        getContentPane().add(jb_viewFeatureDetail);
        


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



        jb_delete = new JButton("Delete");
        jb_delete.setBounds(305, 380, 100, 30);
        configureButton(jb_delete);
        jb_delete.addActionListener(deleteSessionListener);

        jb_search = new JButton("Fetch");
        jb_search.setBounds(180, 380, 100, 30);
        configureButton(jb_search);
        jb_search.addActionListener(searchSessionListener);

        jb_refresh = new JButton("Refresh");
        jb_refresh.setBounds(430, 380, 100, 30);
        configureButton(jb_refresh);
        jb_refresh.addActionListener(refreshListener);
        
        getContentPane().add(jb_refresh);
        getContentPane().add(jsp_cellText);

        getContentPane().add(jsp);

        getContentPane().add(jb_delete);
        getContentPane().add(jb_search);


        getContentPane().setLayout(null);
        setVisible(true);
        populateTable(sessionList);
    }

    private void configureButton(JButton button) {
        Font labelFONT = new Font("Arial", Font.BOLD, 12);
        button.setFont(labelFONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(buttonBGColor);
        button.setForeground(Color.WHITE);
    }
    
    // Main method
    public static void main(String[] args) {

        new SessionGUI(null);
    }
}