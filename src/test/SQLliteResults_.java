package test;

import ij.*;
import ij.plugin.filter.*;
import ij.plugin.frame.*;
import ij.measure.*;
import ij.io.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.sun.image.codec.jpeg.*;

/* This is a plugin for integration of ImageJ to SQLite database
 * based on the SQLResults plugin, version 1.1.5, 1 July 2008
 
 *     @version 1.0 4 Oct 2020
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

public class SQLliteResults_ implements IRoi {

    // public
    public final static String pluginname="SQLite client";
    public final static String version="1.0.0";
    public final static String driver="org.sqlite.JDBC";
    public final String micro = "\u00B5";
    
    // private
	/**
     * the image
     */    
    private ImagePlus imp;
    private ResultsTable rt;
    private Hashtable<String, Integer> columnKeys=new Hashtable<>();
    private Calibration cal;
    private int[] ImageID;
    private FileInfo fi;
    private boolean abort;
    private boolean repeated=false;
    /** The frame of the PlugIn */
    private PlugInFrame pf;
    
    //private String calString;
    private float width=0.0f;
    private float height=0.0f;
    
    private Connection con;
    
    private String database="";

    /**
     * 
     */
	public SQLliteResults_() {
		rt=Analyzer.getResultsTable();
	}
	
	
	public void setDatabase(String db) {
		database=db;
	}
	
	/**
	 * 
	 * @param dbName
	 * @return
	 */
	boolean connStart(String dbName) {
    	String dbUrl="jdbc:sqlite:"+ dbName;
        try {
            //Class.forName("org.gjt.mm.mysql.Driver");
            Class.forName(driver);
        } catch(Exception ex) {
            IJ.log("Can't find Database driver class: " + ex);
            return false;
        }
        try {
            con = DriverManager.getConnection(dbUrl);
            IJ.log("Connected to " + dbUrl);
            return true;
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
            return false;
        }
    }
	
	/** closes the connection to the Database server */
    void connDestroy() {
        try {
            con.close();
            IJ.log("Disconnected from database ");
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
        }
    }
    
    
    private void initComponents() {
        pf=new PlugInFrame(pluginname +" "+version);
        panel1 = new Panel();
        chbox = new Checkbox();
        btnSend = new Button();
        btnRestart = new Button();
        btnClose = new Button();
        panel2 = new Panel();
        label1 = new Label();
        origfilelist = new Choice();
        label2 = new Label();
        ta = new TextArea();
        
        pf.setLayout(new GridLayout(2, 0));
        
        pf.setForeground(Color.lightGray);
        pf. setResizable(false);
        //pf.setTitle(pluginname +" "+version);
        pf.setSize(250,250);
        pf.addWindowListener(new WindowAdapter() {
            @Override
			public void windowClosing(WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        btnSend.setName("btnSend");
        btnSend.setLabel("Send");
        btnSend.setBackground(Color.lightGray);
        btnSend.setForeground(Color.black);
        btnSend.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                btnSendActionPerformed(evt);
                IJ.log("btnSend pressed");
            }
        });
        
        panel1.add(btnSend);
        btnRestart.setName("btnRestart");
        btnRestart.setLabel("Restart");
        btnRestart.setBackground(Color.lightGray);
        btnRestart.setForeground(Color.black);
        btnRestart.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                btnRestartActionPerformed(evt);
            }
        });
        
        panel1.add(btnRestart);
        
        btnClose.setName("brnClose");
        btnClose.setLabel("Close");
        btnClose.setBackground(Color.lightGray);
        btnClose.setForeground(Color.black);
        btnClose.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
                btnCloseActionPerformed(evt);
                IJ.log("btnClose pressed");
            }
        });
        
        panel1.add(btnClose);
        
        
        chbox.setForeground(Color.black);
        chbox.setLabel("Repeated measurements");
        chbox.addItemListener(new ItemListener() {
            @Override
			public void itemStateChanged(ItemEvent evt) {
                chboxItemStateChanged(evt);
            }
        });
        panel1.add(chbox);
        
        
        
        pf.add(panel1);
        
        label1.setForeground(Color.black);
        label1.setText("notes");
        panel2.add(label1);
        
        ta.setForeground(Color.black);
        panel2.add(ta);
        origfilelist.setName("origimages");
        origfilelist.setForeground(Color.black);
        ImageID=getOrigImages(origfilelist);
        panel2.add(origfilelist);
        origfilelist.addItemListener(new ItemListener() {
            @Override
			public void itemStateChanged(ItemEvent evt) {
                origfilelistItemStateChanged(evt);
            }
        });
        label2.setForeground(Color.black);
        label2.setText("images");
        panel2.add(label2);
        
        pf. add(panel2);
        
        pf.pack();
        
        pf.setVisible(true);// show();
        
    }
    
    private void exitForm(WindowEvent evt) {
        abort=true;
        connDestroy();
        pf.close();
    }
    
    private void  btnCloseActionPerformed(ActionEvent evt) {
        abort=true;
        IJ.log("closing: "+abort);
        connDestroy();
        pf.close();
    }
    
    private void chboxItemStateChanged(ItemEvent evt) {        
        repeated=chbox.getState();
    }
    
    private void origfilelistItemStateChanged(ItemEvent evt) { 
        int index=origfilelist.getSelectedIndex();
        //    IJ.log(index +" "+ ImageID[index]);
        fi=WindowManager.getImage(ImageID[index]).getOriginalFileInfo();
        IJ.log(fi.fileName);
        
    }
    
    private int[] getOrigImages( Choice list ) {
        int[] wList = WindowManager.getIDList();
        int[] ID= new int [wList.length];
        int c=0;
        for (int i=0; i<wList.length; i++) {
            ImagePlus imp = WindowManager.getImage(wList[i]);
            FileInfo fi=imp.getOriginalFileInfo();
            if (fi!=null) {
                // IJ.log(fi.fileName);
                list.add(imp.getTitle());
                ID[c]=wList[i];
                c++;
            }
        }
        return ID;
    }
    
    private int getImageRecord(String name) throws SQLException {
        int x=-1;
        String query="SELECT im_id from microimages WHERE name='"+name+"'";
        Statement stmt = con.createStatement();
        ResultSet rs=stmt.executeQuery(query);
        
        while (rs.next()) {
            x=rs.getInt("im_id");
        }
        rs.close();    // Close the ResultSet object.
        stmt.close();  //
        return x;
        
    }
    
    private int fr=4;
  
    @SuppressWarnings("restriction")
	private void writeJPEG(ByteArrayOutputStream out){
        try {
            int width = imp.getWidth()/fr;
            int height = imp.getHeight()/fr;
            ImageProcessor ip2=imp.getProcessor().resize(width,height);
            
            BufferedImage   bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = bi.createGraphics();
            g.drawImage(new ImagePlus("",ip2).getImage(), 0, 0, null);
            g.dispose();
            
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
            param.setQuality(0.75f, true);
            encoder.encode(bi, param);
         
            ip2=null;
        } catch (IOException e) {
            IJ.log(e.toString());
        }
    }
       

    @SuppressWarnings("unused")
	private void insertMeasurements() throws SQLException {
    	PreparedStatement ps=null;
        try {
        	
            updateColumns();
            con.setAutoCommit(false);
 
            /////////////////  calibration
            String  update="INSERT INTO calibrations ( fX, fY, fZ, unit, cal_string) VALUES (?,?,?,?,?)";
            
            ps = con.prepareStatement(update);
            
            String unit = cal.getUnit().trim();
            if (unit.startsWith(micro))	unit="um";
            
            ps.setDouble(1, cal.pixelWidth);
            ps.setDouble(2, cal.pixelHeight);
            ps.setDouble(3, cal.pixelDepth);
            ps.setString(4, unit);
            ps.setString(5, cal.toString());
            int updateCount = ps.executeUpdate();
            IJ.log("calibrations: "+updateCount);
                  
      
            
            ResultSet rs;
			int crowid = getID(ps);
             
            IJ.log("crowid "+crowid);
          
            
            ps.clearParameters();
           // ps.close();
            //////////////////
           
            if (fi==null) fi=imp.getOriginalFileInfo();
            String url="";
            if (fi.directory==null) 
            	url=fi.fileName;
            else  
            	url=fi.directory+fi.fileName;
            IJ.log("path: "+url);
            
            int im_id=-1;
            
            if (repeated){
                im_id=getImageRecord(fi.fileName);
                IJ.log("repeated measurement on im_id: "+im_id);
            }
            boolean updated=false;
            
            // repeated measurements
            if (im_id==-1) {
                
                update="INSERT INTO microimages (url, name, notes, mime,      thumb, width, height) "
                					+ "VALUES   (?,    ?,    ?,   'image/jpeg',   ?,     ?,     ? )";
                ps = con.prepareStatement(update);
                ps.setString(1, url);
                ps.setString(2, fi.fileName);
                ps.setString(3, ta.getText());

                try {
                    ByteArrayOutputStream  baos=new  ByteArrayOutputStream();
                    // DataOutputStream daos=new DataOutputStream(baos);
                    writeJPEG(baos);
                    byte[] buf=baos.toByteArray();
                    // IJ.log(os.toString());
                    baos.close();
                    
                    ps.setBytes(4, buf);
                } catch (IOException ioe) {
                    IJ.log("IOException");
                }
                
                ps.setFloat(5,this.width);
                ps.setFloat(6,this.height);
                IJ.log("width "+width);
                IJ.log("height "+height);
                updateCount = ps.executeUpdate();
                IJ.log("microimages: "+updateCount);
                
                final int drowid=getID(ps);
                IJ.log("image_id / drowid "+drowid);
                
                ps.clearParameters();
                //ps.close();
                updated=true;
                im_id=drowid;
            }
				 
                
            Roi roi = imp.getRoi();
            final int z=imp.getZ();
            final int frm=imp.getFrame();
            final int ch=imp.getC();
            
            if (roi!=null){ // ROI
            	
            	update="INSERT INTO rois (im_id, r_type, roi) "
            			+ 		" VALUES ( ?,    ?,       ?)";
            	PreparedStatement rps = con.prepareStatement(update);
             
                rps.setInt(1, im_id);
                rps.setInt(2, roi.getType());
                rps.setBytes(3, encodeROI(roi));
                updateCount = rps.executeUpdate();
                IJ.log("rois "+updateCount);
                
                final int rrowid= getID(rps);
                IJ.log("rrowid "+rrowid);
                
                rps.clearParameters();
                rps.close();
                  
                update="INSERT INTO measurements (im_id, slide_no,  frame, channel, c_id, r_id, notes ) "
                		+ 				 "VALUES (?,      ?,        ?,          ?,     ?,    ?,     ? )";              
                PreparedStatement mps = con.prepareStatement(update);
                mps.setInt(1, im_id);
                mps.setInt(2, z);
                mps.setInt(3, frm);
                mps.setInt(4, ch);
                mps.setInt(5, crowid);
                mps.setInt(6, rrowid);
                mps.setString(7, ta.getText());
                
                updateCount = mps.executeUpdate();
                mps.clearParameters();
                mps.close();             
            }   else { // no ROI
                update="INSERT INTO measurements (im_id,  slide_no,  frame, channel, c_id, notes) "
                		+ 				"VALUES  ( ?,            ?,      ?,       ?,    ?,     ?)";
                PreparedStatement  mps = con.prepareStatement(update);
                mps.setInt(1, im_id);
                mps.setInt(2, z);
                mps.setInt(3, frm);
                mps.setInt(4, ch);
                mps.setInt(5, crowid);          
                mps.setString(6, ta.getText());
                updateCount = mps.executeUpdate();
                mps.clearParameters();
                mps.close();
            }
           
  
           
            final int arowid=getID(ps);
            update="INSERT INTO measurements_list (m_id, col_id, value, m_no) VALUES (?, ?, ?, ?)";
            
            ps = con.prepareStatement(update);        
            
            int col=0;
            int[] index=new int[ResultsTable.MAX_COLUMNS];
            String[] headings=new String[ResultsTable.MAX_COLUMNS];
            for  (int cnt=0;cnt<ResultsTable.MAX_COLUMNS; cnt++) {
                if (rt.columnExists(cnt)){
                    index[col]=cnt;
                    headings[col]=rt.getColumnHeading(cnt);
                    //System.out.println(index[col]);
                    //System.out.println(col);
                    col++;
                }
            }
            int counter=rt.getCounter();
            
            for( int i=0;i<col;i++) {
                //IJ.log(index[i]);
                float[] result=rt.getColumn(index[i]);
                int colindex=  columnKeys.get(headings[i]);
                IJ.log ("colindex "+colindex);
                for( int j=0;j<counter;j++) {
                	ps.setInt(1, arowid);
                	ps.setInt(2, colindex);
                    ps.setFloat(3,result[j]);
                    ps.setInt(4,j+1);
                   
                    ps.addBatch();
                }
                
            }
            int[] updateCounts = ps.executeBatch();
            
            IJ.log("measurements_list: "+updateCounts.length);
            con.commit();
             
        } catch (SQLException E) {
        	IJ.log("SQL message: " + E.getMessage());
        	IJ.log("SQL State:     " + E.getSQLState());
        	IJ.log("M Error Code:  " + E.getErrorCode());
        	con.rollback();
        } finally {
        	 ps.close();
        	con.setAutoCommit(true);
        }
       
    }

	/**
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	private int getID(Statement ps) throws SQLException {
		ResultSet rs= ps.getGeneratedKeys();
		int id=-1;
		if (rs.next())
			id=rs.getInt(1);
		rs.close();
		return id;
	}
    
    /**
     * 
     * @param evt
     */
    private void btnRestartActionPerformed(ActionEvent evt) {
        try {
			updateColumns();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 
     * @param evt
     */
    private void btnSendActionPerformed(ActionEvent evt) {
        try {
            if (rt.getCounter()>0){
                insertMeasurements();
            } else {
            	IJ.log("no measurement present");
            }
            
        } catch (SQLException E) {
            IJ.log("SQLException: " + E.getMessage());
            IJ.log("SQLState:     " + E.getSQLState());
            IJ.log("VendorError:  " + E.getErrorCode());
        }
        
        
    }
    
    /**
     * 
     * @return
     */
    public String[] getListColumns() {
        StringTokenizer st = new StringTokenizer(rt.getColumnHeadings());
        int n = st.countTokens();
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) {
            
            strings[i] =st.nextToken();
        }
        return strings;
    }
    
    
    /**
     * 
     * @param headings
     * @return
     */
    public Hashtable<String, Integer> getColumnKeys(String[] headings) {
        Hashtable<String, Integer> ht=new Hashtable<>();
        StringBuffer  sb=new StringBuffer();
        for (int i=0;i<headings.length;i++) {
            sb.append("'"+headings[i]+"', ");
            
        }
        String astr= sb.substring(0, sb.length()-2);
        try {
            String query="select col_id,name from mes_columns where name in ("+astr+")";
            //IJ.log(query);
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery(query);
            
            while (rs.next()) {
                ht.put(rs.getString("name"), new Integer(rs.getInt("col_id")));
            }
            rs.close();    // Close the ResultSet object.
            stmt.close();  //
        } catch (SQLException E) {
            IJ.log("SQL Message: " + E.getMessage());
            IJ.log("SQL State:     " + E.getSQLState());
            IJ.log("Error Code:  " + E.getErrorCode());
        }
        return ht;      
    }
    
    /**
     * 
     * @param name
     * @return
     */
    private int updateColumn(String name) {
        int x=-1;
        String update="insert into mes_columns (name) values ('"+name+"')";
       // IJ.log(update);
        try {
            Statement upd=con.createStatement();
            int count=upd.executeUpdate(update);
            IJ.log("rows inserted: "+count +" name: "+name);
            
            ResultSet  rs = upd.getGeneratedKeys();
            
            if (rs.next()) {
                x=rs.getInt(1);
            }
            upd.close();
        }
        catch (SQLException E) {
            IJ.log("SQL Message: " + E.getMessage());
            IJ.log("SQL State:     " + E.getSQLState());
            IJ.log("Error Code:  " + E.getErrorCode());
        }
        
        return x;
    }
    
    /**
     * 
     * @param arg - path to database
     */
    public void run(String arg) {
    	
    	if (database=="") {
    		JFileChooser fileChooser = new JFileChooser();

    		// For Directory
    		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    				 
    		fileChooser.setFileFilter(dbFilefilter);
    		//fileChooser.setAcceptAllFileFilterUsed(true);
    		int rVal = fileChooser.showOpenDialog(null);
    		if (rVal == JFileChooser.APPROVE_OPTION) {
    			String file=fileChooser.getSelectedFile().toString();
    	 		//System.out.println(file);
    			//resplugin.connStart(file);
    			database=file;
    		}
    	}
    		 
    	
    	System.out.println("run: "+arg);
    	imp=WindowManager.getCurrentImage();
        this.width=(float)imp.getWidth();
        this.height=(float)imp.getHeight();
    	cal = imp.getCalibration();

        if (connStart(database)){
        	
            initComponents();
 	            
        }
         
    }
    
    /**
     * 
     * @throws SQLException
     */
    private void updateColumns() throws SQLException {
        String[] headings=getListColumns();
        if (headings.length>0) {
            columnKeys=getColumnKeys(headings);
            int index=0;
            for (int i=0; i<headings.length;i++) {
                try {
                    con.setAutoCommit(false);
                    
                    if (!columnKeys.containsKey(headings[i])) {
                        index=updateColumn(headings[i]);
                        columnKeys.put(headings[i], new Integer(index));
                        //IJ.log("key: " +headings[i] +" value: "+index);
                    }
                    con.commit();
                    
                }
                catch (SQLException E) {
                    IJ.log("SQLException: " + E.getMessage());
                    IJ.log("SQLState:     " + E.getSQLState());
                    IJ.log("UC: VendorError:  " + E.getErrorCode());
                    con.rollback();
                } finally {
                	con.setAutoCommit(true);
                }
            }
        }
    }
    
  /**
   * 
   */
   public void showAbout() {
        IJ.showMessage("About SQLResults.",
        "This plug-in filter interfaces MySQL and ImageJ"
        );
    }
    
      
    
    
    // Variables declaration - do not modify
    private Button btnClose;
    private Button btnSend;
    private Button btnRestart;
    private Checkbox chbox;
    private Label label1;
    private Panel panel1;
    private Panel panel2;
    private TextArea ta;
    private Label label2;
    private Choice origfilelist;
    // End of variables declaration
    
    
	public static void main(String[] args) {

		new ImageJ();
		IJ.run("Blobs (25K)");
		
		SQLliteResults_ resplugin = new SQLliteResults_();
		
		resplugin.chooseFile();
	}
	
	private FileFilter dbFilefilter = new FileFilter() {
		     @Override
			public boolean accept(File file) {
		             //if the file extension is .db return true, else false
		             if (file.getName().endsWith(".db")) {
		                return true;
		             }
		             return false;
		      }

			@Override
			public String getDescription() {
				return "SQLite files";
			}
		};


	/**
	 * 
	 */
	void chooseFile() {
		JFileChooser fileChooser = new JFileChooser();

		// For Directory
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				 
		fileChooser.setFileFilter(dbFilefilter);
		//fileChooser.setAcceptAllFileFilterUsed(true);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			String file=fileChooser.getSelectedFile().toString();
	 		//System.out.println(file);
			//resplugin.connStart(file);
			System.out.println("run: "+file);
			database=file;
	    	imp=WindowManager.getCurrentImage();
	        this.width=(float)imp.getWidth();
	        this.height=(float)imp.getHeight();
	    	cal = imp.getCalibration();

	        if (connStart(database)){
	        	
	            initComponents();
	 	            
	        }
		}
	}

} /////////////////////////////////////////
