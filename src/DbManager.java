

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

import java.util.*;

 

public class DbManager {
	   public final static String driver="org.sqlite.jdbc";
	   
	   private String dbFile="";
	   private Connection con;
	   
	   private boolean active=false;
	   
	   public DbManager() {
	   }
	   
	   public DbManager(String dbfile) {
		   this();
		   loadDB(dbfile);
	   }
	   
	   public void loadDB(String dbfile) {
		   dbFile=dbfile;
		   active=connStart(dbFile);
	   }
	   
	   public Connection getConnection() {
		   return con;
	   }
	   
	   public String getDB() {
		   return dbFile;
	   }
	   
	   public boolean isActive() {
		   return active;
	   }
	   
		/**
		 * 
		 * @param dbName
		 * @return
		 */
		boolean connStart(String dbName) {
	    	String dbUrl="jdbc:sqlite:"+ dbName;

	        try {
	            con = DriverManager.getConnection(dbUrl);
	            System.out.println("Connected to " + dbUrl);
	            return true;
	        } catch(SQLException E) {
	        	logError(E);
	            return false;
	        }
	    }
		
		/** closes the connection to the Database server */
	    boolean connDestroy() {
	        try {
	            con.close();
	            System.out.println("Disconnected from database ");
	            return true;
	        } catch(SQLException E) {
	        	logError(E);
	        	return false;
	        }
	    }
	    
	    /**
		 * @param E
		 */
		public void logError(SQLException E) {
			System.out.println("SQL Message: " + E.getMessage());
			   System.out.println("SQL State:     " + E.getSQLState());
			   System.out.println("Error Code:  " + E.getErrorCode());
		}

	    
	    
		public  DefaultTableModel buildTableModel(ResultSet rs)
		        throws SQLException {

		    final ResultSetMetaData metaData = rs.getMetaData();

		    // names of columns
		    final Vector<String> columnNames = new Vector<>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		    	final String name=metaData.getColumnName(column);
		    	//System.out.println(name);
		        columnNames.add(name);
		    }
		 
		    // data of the table
		    final Vector<Vector<Object>> data = new Vector<>();
		    while (rs.next()) {
		        Vector<Object> vector = new Vector<>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		        	final Object obj=rs.getObject(columnIndex);
		        //	System.out.print(obj+ " ");
		            vector.add(obj);
		        }
		       // System.out.println();
		        data.add(vector);
		    }

		    return new DefaultTableModel(data, columnNames);

		}

 
		
 
 
 
	
}
