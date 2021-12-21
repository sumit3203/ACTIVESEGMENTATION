package test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database_Init_Del {
	
	public Database_Init_Del() {
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
            createVtable(lock);
            createILtable(lock);
            createVLtable(lock);
            createCLtable(lock);
            createFNtable(lock);
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

    private static void createVtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `vectors`";  
        lock.execute(query);
        query="CREATE TABLE `vectors` (\r\n" + 
    			" `v_id` INTEGER PRIMARY KEY, \r\n" +  
    			" `image_name` TEXT \r\n" + 
    			");";  
        lock.execute(query);
        System.out.println("VECTORS created");
    }
    
    private static void createILtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `image_list`";
 
        lock.execute(query);
        query="CREATE TABLE `image_list` (\r\n" + 
    			" `img_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `image_name` TEXT, \r\n" + 
    			" `instance_type` TEXT, \r\n" +
    			" `cell_type` TEXT, \r\n" +
    			" `v_id` INTEGER, \r\n" +
    			" FOREIGN KEY(v_id) REFERENCES vectors(v_id)\r\n" + 
    			");";
 
        lock.execute(query);
        System.out.println("IMAGE_LIST created");
    }
    
    private static void createVLtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `vector_list`";
        lock.execute(query);
        query="CREATE TABLE `vector_list` (\r\n" + 
    			" `vl_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `v_id` INTEGER, \r\n" + 
    			" `value` REAL, \r\n" +
    			" FOREIGN KEY(v_id) REFERENCES vectors(v_id)\r\n" +
    			")";
        lock.execute(query);
        System.out.println("VECTOR_LIST created");
    }    
    
    private static void createCLtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `class_list`";
        lock.execute(query);
        query="CREATE TABLE `class_list` (\r\n" + 
    			" `cl_id` INTEGER PRIMARY KEY,\r\n" + 
    			" `cl_name` TEXT \r\n" + 
    			")";
        lock.execute(query);
        System.out.println("CLASS_LIST created");
    }
    
    private static void createFNtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features_names`";
        lock.execute(query);
        query="CREATE TABLE `features_names` (\r\n" + 
    			" `fn_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `f_name` TEXT \r\n" +
    			")";
        lock.execute(query);
        System.out.println("FEATURES_NAMES created");
    }
    
    private static void createFVtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `features_values`" ;
        lock.execute(query);
        query="CREATE TABLE `features_values` (\r\n" + 
    			" `f_id` INTEGER PRIMARY KEY, \r\n" + 
    			" `f_value` REAL, \r\n" + 
    			" `fn_id` INTEGER, \r\n" + 
    			" `cl_id` INTEGER, \r\n" + 
    			"FOREIGN KEY(fn_id) REFERENCES features_names(fn_id), \r\n" +
    			"FOREIGN KEY(cl_id) REFERENCES class_list(cl_id) \r\n" +
    			")";
     
        lock.execute(query);
        System.out.println("FEATURES_VALUES created");
    }
   
	public static void main(String[] args) {
		String path="C:\\Users\\billa\\Documents\\GitHub\\ACTIVESEGMENTATION\\classif.db";
		connect(path);	 
	}

}


