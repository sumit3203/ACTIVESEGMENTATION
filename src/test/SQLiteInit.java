package test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFileChooser;

/* This is a plugin for integration of ImageJ to SQLite database
 * based on the SQLResults plugin, version 1.1.5, 1 July 2008
 
 *     @version 1.0 
 *
 *     @author	Dimiter Prodanov
 *     @author  IMEC 
 *
 *      Copyright (C) 2020 Dimiter Prodanov
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *
 */
public class SQLiteInit {
	

	public SQLiteInit() {
		// TODO Auto-generated constructor stub
	}
	
	/**
     * Connect to a sample database
     */
    public static void connect(String dbname) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:"+dbname;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
            String query="PRAGMA foreign_keys = ON;";
            Statement lock=conn.createStatement();
            lock.execute(query);
            createMItable(lock);
            createMCtable(lock);
            createRtable(lock);
            createMtable(lock);
            createMLtable(lock);
            createCtable(lock);
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

    
    private static void createMItable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `microimages`";
        lock.execute(query);
        query="CREATE TABLE  `microimages` (\r\n" + 
    			"  `im_id` INTEGER PRIMARY KEY,\r\n" + 
    			"  `url` TEXT,\r\n" + 
    			"  `name` TEXT,\r\n" + 
    			"  `notes` TEXT,\r\n" + 
    			"  `mime` TEXT,\r\n" + 
    			"  `thumb` BLOB,\r\n" + 
    			"  `width` FLOAT,\r\n" + 
    			"  `height` FLOAT\r\n" + 
    			")";
        lock.execute(query);
        query="CREATE INDEX iname ON microimages(name)";
        lock.execute(query);
        System.out.println("microimages");
    }
    
    private static void createMCtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `mes_columns`";
        lock.execute(query);
        query="CREATE TABLE `mes_columns` (\r\n" + 
    			"`col_id`  INTEGER PRIMARY KEY, \r\n" + 
    			"`name` TEXT, \r\n" + 
    			"`dim` INTEGER\r\n" + 
    			")";
        lock.execute(query);
        System.out.println("mes_columns created");
    }
    
    private static void createRtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `rois`" ;
        lock.execute(query);
        query="CREATE TABLE `rois` (\r\n" + 
    			"`r_id` INTEGER PRIMARY KEY, \r\n" + 
    			"`im_id` INTEGER, \r\n" + 
    			"`r_type` INTEGER, \r\n" + 
    			"`roi` BLOB, \r\n" + 
    			"`roi_xml` TEXT,\r\n" + 
    			"FOREIGN KEY(im_id) REFERENCES microimages(im_id)\r\n" + 
    			")";
     
        lock.execute(query);
        System.out.println("rois created");
    }
    
    private static void createMtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `measurements`" ;
  
        lock.execute(query);
        query=	"CREATE TABLE `measurements` (\r\n" + 
    			"`m_id` INTEGER PRIMARY KEY, \r\n" + 
    			"`im_id` INTEGER, \r\n" + 
    			"`slide_no` INTEGER, \r\n" + 
    			"`notes` TEXT, \r\n" + 
    			"`c_id` INTEGER, \r\n" + 
    			"`r_id` INTEGER,\r\n" + 
    			"FOREIGN KEY(im_id) REFERENCES microimages(im_id),\r\n" + 
    			"FOREIGN KEY(r_id) REFERENCES rois(r_id),\r\n" + 
    			"FOREIGN KEY(c_id) REFERENCES calibrations(c_id)\r\n" + 
    			");";
  
        lock.execute(query);
        System.out.println("measurements created");
    }
    
    private static void createMLtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `measurements_list`";
 
        lock.execute(query);
        query="CREATE TABLE `measurements_list` (\r\n" + 
    			"`ml_id` INTEGER PRIMARY KEY, \r\n" + 
    			"`m_id` INTEGER, \r\n" + 
    			"`col_id` INTEGER, \r\n" + 
    			"`m_no` INTEGER, \r\n" + 
    			"`value` REAL,\r\n" + 
    			"FOREIGN KEY(m_id) REFERENCES measurements(m_id),\r\n" + 
    			"FOREIGN KEY(col_id) REFERENCES mes_columns(col_id)\r\n" + 
    			");";
 
        lock.execute(query);
        System.out.println("measurements_list created");
    }
    
    private static void createCtable(Statement lock) throws SQLException {
    	String query="DROP TABLE IF EXISTS `calibrations`";
        lock.execute(query);
        query="CREATE TABLE `calibrations` (\r\n" + 
    			"`c_id` INTEGER PRIMARY KEY, \r\n" + 
    			"`fX` REAL, \r\n" + 
    			"`fY` REAL, \r\n" + 
    			"`fZ` REAL, \r\n" + 
    			"`cal_string` TEXT, \r\n" + 
    			"`unit` TEXT\r\n" + 
    			")";
        lock.execute(query);
        System.out.println("calibrations created");
    }
    final static String testdb="imagemes.db";
    final static String fs=System.getProperty("file.separator");
    
	public static void main(String[] args) {
		
		JFileChooser fileChooser = new JFileChooser();
		String path="";
		// For Directory
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			path=fileChooser.getSelectedFile().toString();
	 		System.out.println(path);
		 
	 		String filename=path+fs+testdb;
	 		System.out.println(filename);
	 		File dbf=new File(filename);
	 		if (dbf.exists()) 
	 			connect(filename);
	 		 else
		 		try {
					dbf.createNewFile();
					connect(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
	 
		}
		
		 
	}

}
