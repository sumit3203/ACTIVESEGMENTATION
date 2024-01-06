package activeSegmentation.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import activeSegmentation.DbManager;




public class SessionManager {
	
	private DbManager man=new DbManager();
	
	
	
	 public  void createIMtable(Statement lock) throws SQLException {
	        String query = "CREATE TABLE IF NOT EXISTS `images` (\r\n" +
	                " `img_id` INTEGER PRIMARY KEY, \r\n" +
	                " `session_id` INTEGER, \r\n" +
	                " `image_id` INTEGER, \r\n" +
	                " `image_name` VARCHAR(50) \r\n" +
	                ");";
	        lock.execute(query);
	        System.out.println("IMAGES created");
	    }
	    
	    public  void createCPtable(Statement lock) throws SQLException {
	        String query = "CREATE TABLE IF NOT EXISTS `class_probabilities` (\r\n" +
	                " `cp_id` INTEGER PRIMARY KEY, \r\n" +
	                " `session_id` INTEGER, \r\n" +
	                " `class_label` VARCHAR(50), \r\n" +
	                " `probability` FLOAT \r\n" +
	                ");";
	        lock.execute(query);
	        System.out.println("CLASS_PROBABILITIES created");
	    }
	    
	    public  void createSStable(Statement lock) throws SQLException {
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
	    
	    public  void createCLtable(Statement lock) throws SQLException {
	        String query = "CREATE TABLE IF NOT EXISTS `class_list` (\r\n" +
	                " `class_id` INTEGER PRIMARY KEY, \r\n" +
	                " `session_id` INTEGER,\r\n" +
	                " `image_name` VARCHAR(50), \r\n" +
	                " `class_label` VARCHAR(50) \r\n" +
	                ");";
	        lock.execute(query);
	        System.out.println("CLASS_LIST created");
	    }
	    
	    public  void createFStable(Statement lock) throws SQLException {
	        String query = "CREATE TABLE IF NOT EXISTS `features` (\r\n" +
	                " `feature_id` INTEGER PRIMARY KEY, \r\n" +
	                " `session_id` INTEGER, \r\n" +
	                " `feature_name` VARCHAR(50), \r\n" +
	                " `feature_parameter` VARCHAR(50) \r\n" +
	                ");";
	        lock.execute(query);
	        System.out.println("FEATURES created");
	    }
	    
	    public  void createFVtable(Statement lock) throws SQLException {
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
	    public void createTable() {
	        try {
	        	Connection conn=man.getConnection();
	            Statement stmt = conn.createStatement();
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
	        } catch (SQLException e) {
	            //System.out.println(e.getMessage());
	            man.logError(e);
	        }
	    }

}
