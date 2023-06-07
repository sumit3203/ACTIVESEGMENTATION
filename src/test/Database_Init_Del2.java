package test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database_Init_Del2 {
	
	public Database_Init_Del2() {
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

    private static void createCPtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `class_probabilities`";  
        lock.execute(query);
        query="CREATE TABLE `class_probabilities` (\r\n" + 
    			" `session_id` INTEGER PRIMARY KEY, \r\n" +  
    			" `class_label` TEXT \r\n" + 
    			" `probability` float \r\n" + 
    			");";
        lock.execute(query);
        System.out.println("CLASS_PROBABILITIES created");
    }
    
    private static void createSStable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `sessions`";
        lock.execute(query);
        query="CREATE TABLE `sessions` (\r\n" + 
    			" `session_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `start_time` timestamp, \r\n" + 
    			" `end_time` timestamp, \r\n" +
    			" `dataset_path` TEXT, \r\n" +
    			" `classifier_output` TEXT, \r\n" +
    			")";
        lock.execute(query);
        System.out.println("SESSIONS created");
    }    
    
    private static void createCLtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `class_list`";
        lock.execute(query);
        query="CREATE TABLE `class_list` (\r\n" + 
    			" `session_id` INTEGER PRIMARY KEY,\r\n" + 
    			" `image_name` TEXT \r\n" + 
    			" `class_label` TEXT \r\n" + 
    			")";
        lock.execute(query);
        System.out.println("CLASS_LIST created");
    }
    
    private static void createFStable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features`";
        lock.execute(query);
        query="CREATE TABLE `features` (\r\n" + 
    			" `session_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `feature_name` TEXT \r\n" +
    			" `feature_parameter` TEXT \r\n" +
    			")";
        lock.execute(query);
        System.out.println("FEATURES created");
    }
    
    private static void createFVtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features_values`" ;
        lock.execute(query);
        query="CREATE TABLE `features_values` (\r\n" + 
    			" `session_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `image_name` TEXT, \r\n" + 
    			" `feature_name` TEXT, \r\n" + 
    			" `feature_value` FLOAT, \r\n" + 
    			")";
     
        lock.execute(query);
        System.out.println("FEATURES_VALUES created");
    }
   
	public static void main(String[] args) {
		String path="C:\\Users\\aarya\\Desktop\\gsoc23\\ACTIVESEGMENTATION\\sqliteTest.db";
		connect(path);	 
	}

}