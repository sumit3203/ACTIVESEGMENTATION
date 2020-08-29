package test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFileChooser;

public class SQLiteTest {
	

	public SQLiteTest() {
		// TODO Auto-generated constructor stub
	}
	
	/**
     * Connect to a sample database
     */
    public static void connect(String path) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:"+path;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

	public static void main(String[] args) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			String path=fileChooser.getSelectedFile().toString();
	 		System.out.println(path);	 
	 		connect(path); 
		}
		

	}

}
