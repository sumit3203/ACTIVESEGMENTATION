package test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite_Init_Del {
	
	public SQLite_Init_Del() {
	}
	
    public static void connect(String dbname) {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:"+dbname;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
            String query="PRAGMA foreign_keys = ON";
            Statement lock=conn.createStatement();
            lock.execute(query);
            createCPtable(lock);
            createSStable(lock);
            createCLtable(lock);
            createFStable(lock);
            createFVtable(lock);
            createIMtable(lock);
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

    private static void createIMtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `images`";  
        lock.execute(query);
        query="CREATE TABLE `images` (\r\n" + 
                " `img_id` INTEGER PRIMARY KEY, \r\n" +
                " `session_id` INTEGER, \r\n" +  
                " `image_id` INTEGER, \r\n" +  
    			" `image_name` VARCHAR(50) \r\n" + 
    			");";
        lock.execute(query);
        System.out.println("IMAGES created");
    }

    private static void createCPtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `class_probabilities`";  
        lock.execute(query);
        query="CREATE TABLE `class_probabilities` (\r\n" + 
                " `cp_id` INTEGER PRIMARY KEY, \r\n" +
    			" `session_id` INTEGER, \r\n" +  
    			" `class_label` VARCHAR(50), \r\n" + 
    			" `probability` FLOAT \r\n" + 
    			");";
        lock.execute(query);
        System.out.println("CLASS_PROBABILITIES created");
    }
    
    private static void createSStable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `sessions`";
        lock.execute(query);
        query="CREATE TABLE `sessions` (\r\n" + 
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
    	String query="DROP TABLE IF EXISTS `class_list`";
        lock.execute(query);
        query="CREATE TABLE `class_list` (\r\n" + 
                " `class_id` INTEGER PRIMARY KEY, \r\n" +
    			" `session_id` INTEGER,\r\n" + 
    			" `image_name` VARCHAR(50), \r\n" + 
    			" `class_label` VARCHAR(50) \r\n" + 
    			");";
        lock.execute(query);
        System.out.println("CLASS_LIST created");
    }
    
    private static void createFStable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features`";
        lock.execute(query);
        query="CREATE TABLE `features` (\r\n" + 
                " `feature_id` INTEGER PRIMARY KEY, \r\n" +
    			" `session_id` INTEGER, \r\n" + 
    			" `feature_name` VARCHAR(50), \r\n" +
    			" `feature_parameter` VARCHAR(50) \r\n" +
    			");";
        lock.execute(query);
        System.out.println("FEATURES created");
    }
    
    private static void createFVtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features_values`" ;
        lock.execute(query);
        query="CREATE TABLE `features_values` (\r\n" + 
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
   
	public static void main(String[] args) {
		String path="C:\\Users\\aarya\\Desktop\\gsoc23\\ACTIVESEGMENTATION\\sqliteTest.db";
		connect(path);	 
	}

}