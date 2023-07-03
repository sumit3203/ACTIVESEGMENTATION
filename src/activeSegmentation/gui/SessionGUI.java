package activeSegmentation.gui;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextArea;



public class SessionGUI {
    // Declare the variables and objects
    JTextField jtf_sessionId, jtf_startTime, jtf_endTime, jtf_datasetPath, jtf_classifierOutput;
    JButton jb_add, jb_delete, jb_search, jb_viewDetail;
    JTable jt;
    JFrame frame;
    JLabel lbl_sessionId, lbl_startTime, lbl_endTime, lbl_datasetPath, lbl_classifierOutput;
    ArrayList<Session> sessionList;
    Session session;
    JButton jb_refresh;
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

    // Create sessions table if it does not exist
    private void createTable() {
        try {
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS sessions (\n"
                    + " ss_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " session_id INTEGER,\n"
                    + " start_time TEXT,\n"
                    + " end_time TEXT,\n"
                    + " dataset_path TEXT,\n"
                    + " classifier_output TEXT\n"
                    + ");";
            stmt.execute(sql);
            System.out.println("Table created successfully.");
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
            System.out.println("bye");
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
    
    ActionListener viewSessionDetailListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = jt.getSelectedRow();
            if (selectedRow != -1) {
                int sessionId = (int) jt.getValueAt(selectedRow, 1); // Session ID from the selected row
                System.out.println("sessionId = " + sessionId);
                ArrayList<ClassList> classList = getClassListBySessionId(sessionId);

                // Create and populate a new table to show session details
                Object[][] classListData = new Object[classList.size()][3];
                for (int i = 0; i < classList.size(); i++) {
                    ClassList classItem = classList.get(i);
                    classListData[i] = new Object[] {
                        classItem.getSessionId(),
                        classItem.getImageName(),
                        classItem.getImageLabel()
                    };
                }

                String[] classListHeader = new String[] {
                    "Session ID",
                    "Image Name",
                    "Image Label"
                };

                JTable classListTable = new JTable(classListData, classListHeader);
                JScrollPane classListScrollPane = new JScrollPane(classListTable);
                classListTable.setFillsViewportHeight(true);

                // Show the table in a dialog
                JOptionPane.showMessageDialog(null, classListScrollPane, "Session Detail", JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a session to view details.", "No Session Selected",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    // Delete session button listener
    ActionListener deleteSessionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int selectedRow = jt.getSelectedRow();
            if (selectedRow != -1) {
                int ss_id = (int) jt.getValueAt(selectedRow, 0);
                deleteData(ss_id);
                sessionList.remove(selectedRow);
                populateTable(sessionList);
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
    private void deleteData(int ss_id) {
        try {
            String sql = "DELETE FROM sessions WHERE ss_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ss_id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
 // Load class_list values into the GUI
    private ArrayList<ClassList> getClassListBySessionId(int sessionId) {
        ArrayList<ClassList> classList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM class_list WHERE session_id = ?";
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
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dtm = new DefaultTableModel(header, 0);
        jt = new JTable(dtm);
        JTextArea jta_cellText = new JTextArea(10, 30);
        jta_cellText.setLineWrap(true);
        jta_cellText.setWrapStyleWord(true);
        JScrollPane jsp_cellText = new JScrollPane(jta_cellText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp_cellText.setBounds(380, 460, 350, 100);

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
        jb_viewDetail.setBounds(500, 420, 150, 30);
        jb_viewDetail.addActionListener(viewSessionDetailListener);
        frame.add(jb_viewDetail);

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

        jb_add = new JButton("Add");
        jb_add.setBounds(380, 380, 100, 30);
        jb_add.addActionListener(addSessionListener);

        jb_delete = new JButton("Delete");
        jb_delete.setBounds(500, 380, 100, 30);
        jb_delete.addActionListener(deleteSessionListener);

        jb_search = new JButton("Search");
        jb_search.setBounds(380, 420, 100, 30);
        jb_search.addActionListener(searchSessionListener);
        
        jb_refresh = new JButton("Refresh");
        jb_refresh.setBounds(620, 380, 100, 30);
        jb_refresh.addActionListener(refreshListener);
        frame.add(jb_refresh);

//        jta_cellText = new JTextArea();
    //    jta_cellText.setBounds(380, 460, 200, 60);
    //    jta_cellText.setEditable(false);
        // frame.add(jta_cellText);
//        frame.add(jta_cellText);
        frame.add(jsp_cellText);

        frame.add(jsp);
        frame.add(lbl_sessionId);
        frame.add(jtf_sessionId);
        frame.add(lbl_startTime);
        frame.add(jtf_startTime);
        frame.add(lbl_endTime);
        frame.add(jtf_endTime);
        frame.add(lbl_datasetPath);
        frame.add(jtf_datasetPath);
        frame.add(lbl_classifierOutput);
        frame.add(jtf_classifierOutput);
        frame.add(jb_add);
        frame.add(jb_delete);
        frame.add(jb_search);

        frame.setLayout(null);
        frame.setVisible(true);
        populateTable(sessionList);
    }
    
 // ClassList class
    class ClassList {
        private int sessionId;
        private String imageName;
        private String imageLabel;

        public ClassList(int sessionId, String imageName, String imageLabel) {
            this.sessionId = sessionId;
            this.imageName = imageName;
            this.imageLabel = imageLabel;
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