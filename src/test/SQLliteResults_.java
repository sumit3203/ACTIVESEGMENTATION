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

import com.sun.image.codec.jpeg.*;

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

public class SQLliteResults_ {
	/**
     * the image
     */    
    ImagePlus imp;
    private ResultsTable rt;
    private Hashtable<String, Integer> columnKeys=new Hashtable<>();
    private Calibration cal;
    private int[] ImageID;
    private FileInfo fi;
    private boolean abort;
    private boolean repeated=false;
    /** The frame of the PlugIn */
    public PlugInFrame pf;
    
    //private String calString;
    private float width=0.0f;
    private float height=0.0f;
    
    private Connection con;
    
    private final static String driver="org.sqlite.JDBC";
    
    private String database="test.db";

	public SQLliteResults_() {
		rt=Analyzer.getResultsTable();
	}
	
	public boolean connStart(String dbName) {
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
    public void ConnDestroy() {
        try {
            con.close();
            IJ.log("Disconnected from database ");
        } catch(SQLException ex) {
            IJ.log("SQLException: " + ex);
        }
    }

    public final static String pluginname="SQLite client";
    public final static String version="1.0.0";
    
    
    private void initComponents() {
        pf=new PlugInFrame(pluginname +" "+version);
        panel1 = new java.awt.Panel();
        chbox = new java.awt.Checkbox();
        btnSend = new java.awt.Button();
        btnRestart = new java.awt.Button();
        btnClose = new java.awt.Button();
        panel2 = new java.awt.Panel();
        label1 = new java.awt.Label();
        origfilelist = new java.awt.Choice();
        label2 = new java.awt.Label();
        ta = new java.awt.TextArea();
        
        pf.setLayout(new java.awt.GridLayout(2, 0));
        
        pf.setForeground(java.awt.Color.lightGray);
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
        ConnDestroy();
        pf.close();
    }
    
    private void  btnCloseActionPerformed(ActionEvent evt) {
        //button2ActionPerformed
        abort=true;
        //ta.append(" "+abort);
        IJ.log(" "+abort);
        //notifyAll();
        ConnDestroy();
        pf.close();
        
    }
    
    private void chboxItemStateChanged(ItemEvent evt) {
        // Add your handling code here:
        repeated=chbox.getState();
        //if (repeated)
        //ta.append("repeated checked\r\n");
        
    }
    
    private void origfilelistItemStateChanged(ItemEvent evt) {
        // Add your handling code here:
        
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
        java.sql.Statement Stmt = con.createStatement();
        java.sql.ResultSet Rs=Stmt.executeQuery(query);
        
        while (Rs.next()) {
            x=Rs.getInt("im_id");
        }
        Rs.close();    // Close the ResultSet object.
        Stmt.close();  //
        return x;
        
    }
    
    
  
    @SuppressWarnings("restriction")
	private void writeJPEG(ByteArrayOutputStream out){
        try {
            int width = imp.getWidth()/4;
            int height = imp.getHeight()/4;
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
    
    public boolean showDialog() {
   
       // GenericDialog gd2=new GenericDialog("SQLite client");
       // gd2.setResizable(false);
        // 2nd Dialog box for user input
        //gd2.addMessage("DB Authentication");
        //gd2.addStringField("database", database);

       // gd2.showDialog();
        
        //database=gd2.getNextString();
  
    	
	   //if (gd2.wasCanceled())
	   //     	return false;
    	
    	return true;
     
}
    
    @SuppressWarnings("unused")
	private void insertMeasurements() throws SQLException {
        try {
            con.setAutoCommit(false);
            
            String query="lock tables microimages write, measurements write, measurements_list write, calibrations write, rois write";
            java.sql.Statement lock=con.createStatement();
            lock.execute(query);
            String  update="INSERT INTO calibrations ( fX, fY, fZ, unit, cal_string) VALUES (?, ?,?,?,?)";
            java.sql.PreparedStatement ps = con.prepareStatement(update);
            
            String unit = cal.getUnit();
            /*
            IJ.log("w: " +1/cal.pixelWidth +"  pixels/"+unit);
            IJ.log("h: " +1/cal.pixelHeight +"  pixels/"+unit);
            IJ.log("d: " +1/cal.pixelDepth +"  pixels/"+unit);
             */
            final String micro = "\u00B5";
            unit.trim();
            if (unit.startsWith(micro))
            	unit="um";
            ps.setDouble(1, cal.pixelWidth);
            ps.setDouble(2, cal.pixelHeight);
            ps.setDouble(3, cal.pixelDepth);
            ps.setString(4, unit);
            ps.setString(5, cal.toString());
            int updateCount = ps.executeUpdate();
            IJ.log("calibrations: "+updateCount);
            
            query="select @c:=last_insert_id()";
            lock.execute(query);
            
            ps.clearParameters();
            
            
            if (fi==null) fi=imp.getOriginalFileInfo();
            
            String url=fi.directory+fi.fileName;
            IJ.log("path: "+url);
            int im_id=-1;
            if (repeated){
                im_id=getImageRecord(fi.fileName);
                IJ.log("im_id: "+im_id);
            }
            boolean updated=false;
            
            if (im_id==-1) {
                
                update="INSERT INTO microimages (url, name, notes, thumb, width, height) VALUES (?,?,?,?,?,?)";
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
                }
                catch (IOException ioe) {
                    IJ.log("IOException");
                  
                }
                ps.setFloat(5,this.width);
                ps.setFloat(6,this.height);
                IJ.log("width "+width);
                IJ.log("height "+height);
                updateCount = ps.executeUpdate();
                IJ.log("microimages: "+updateCount);
                
                query="select @d:=last_insert_id()";
                lock.execute(query);
                
                ps.clearParameters();
                updated=true;
            }
				else {
                query="set @d:="+im_id;
                lock.execute(query);
                ps.clearParameters();

			}
		
           // ta.setText("");
            
            Roi roi = imp.getRoi();
            
            
            if (roi!=null){ // ROI
            	
                if (updated) {
                    update="INSERT INTO rois (im_id, r_type, roi) VALUES ( @d,?,?)";
                    ps = con.prepareStatement(update);
                    
                    ps.setInt(1, roi.getType());
                    ps.setBytes(2, encodeROI(roi));
                }
                else {
                    update="INSERT INTO rois (im_id, r_type, roi) VALUES ( ?,?,?)";
                    ps = con.prepareStatement(update);
                    ps.setInt(1, im_id);
                    ps.setInt(2, roi.getType());
                    ps.setBytes(3, encodeROI(roi));
                }
                
                updateCount = ps.executeUpdate();
                IJ.log("rois "+updateCount);
                
                
                query="select @r:=last_insert_id()";
                lock.execute(query);
                update="INSERT INTO measurements (im_id,   c_id, r_id, notes ) VALUES ( @d, @c ,@r,?)";
                
            }
            else { // no ROI
                
                update="INSERT INTO measurements (im_id,  c_id, notes) VALUES ( @d, @c ,?)";
            }

            ps = con.prepareStatement(update);
         
           
            ps.setString(1, ta.getText());
            
            updateCount = ps.executeUpdate();
            IJ.log("measurements: "+updateCount);
            
            query="select @a:=last_insert_id()";
            lock.execute(query);
            ps.clearParameters();
            
            update="INSERT INTO measurements_list (m_id, col_id, value, m_no) VALUES ( @a, ?, ?,?)";
            
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
                Integer colindex=(Integer) columnKeys.get(headings[i]);
                //IJ.log ("colindex "+colindex);
                for( int j=0;j<counter;j++) {
                    ps.setFloat(2,result[j]);
                    ps.setInt(3,j+1);
                    ps.setInt(1,colindex.intValue());
                    ps.addBatch();
                }
                
            }
            int[] updateCounts = ps.executeBatch();
            
            IJ.log("measurements_list: "+updateCounts.length);
            
            ps.close();
            
            query= "unlock tables";
            lock.execute(query);
            //}
            
            con.commit();
            con.setAutoCommit(true);
        }
        catch (SQLException E) {
            IJ.log("SQL message: " + E.getMessage());
            IJ.log("SQL State:     " + E.getSQLState());
            IJ.log("Error Code:  " + E.getErrorCode());
            con.rollback();
        }
        
    }
    
    private void btnRestartActionPerformed(ActionEvent evt) {
        updateColumns();
    }
    
    private void btnSendActionPerformed(ActionEvent evt) {
        try {
            if (rt.getCounter()>0){
                insertMeasurements();
            }
            
        }
        catch (SQLException E) {
            
            IJ.log("SQLException: " + E.getMessage());
            IJ.log("SQLState:     " + E.getSQLState());
            IJ.log("VendorError:  " + E.getErrorCode());
        }
        
        
    }
    
    public String[] getListColumns() {
        StringTokenizer st = new StringTokenizer(rt.getColumnHeadings());
        int n = st.countTokens();
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) {
            
            strings[i] =st.nextToken();
        }
        return strings;
    }
    
    
    
    public Hashtable<String, Integer> getColumnKeys(String[] headings) {
        Hashtable<String, Integer> ht=new Hashtable<>();
        StringBuffer  sb=new StringBuffer();
        for (int i=0;i<headings.length;i++) {
            sb.append("'"+headings[i]+"', ");
            
        }
        // sb.lastIndexOf(",");
        
        //sb.l
        String astr= sb.substring(0,sb.length()-2);
        try {
            String query="select col_id,name from mes_columns where name in ("+astr+")";
            IJ.log(query);
            java.sql.Statement Stmt=con.createStatement();
            java.sql.ResultSet Rs=Stmt.executeQuery(query);
            
            while (Rs.next()) {
                ht.put(Rs.getString("name"), new Integer(Rs.getInt("col_id")));
            }
            Rs.close();    // Close the ResultSet object.
            Stmt.close();  //
        }
        catch (SQLException E) {
            IJ.log("SQL Message: " + E.getMessage());
            IJ.log("SQL State:     " + E.getSQLState());
            IJ.log("Error Code:  " + E.getErrorCode());
        }
        
        
        
        return ht;
        
    }
    
    public int updateColumn(String name) {
        int x=-1;
        String update="insert into mes_columns (name) values ('"+name+"')";
        IJ.log(update);
        try {
            Statement upd=con.createStatement();
            int count=upd.executeUpdate(update);
            IJ.log("rows inserted: "+count);
            
            java.sql.ResultSet  rs = upd.getGeneratedKeys();
            
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
    
    public void run(String arg) {
    	initComponents();
    	
    	imp=WindowManager.getCurrentImage();
        this.width=(float)imp.getWidth();
        this.height=(float)imp.getHeight();
    	cal = imp.getCalibration();
    	if (showDialog()) { 
	        if (connStart(database)){
	            
	            updateColumns();
	            
	        }
    	}
        
    }
    
    private void updateColumns() {
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
                        IJ.log("key: " +headings[i] +" value: "+index);
                    }
                    con.commit();
                    con.setAutoCommit(true);
                }
                catch (SQLException E) {
                    IJ.log("SQLException: " + E.getMessage());
                    IJ.log("SQLState:     " + E.getSQLState());
                    IJ.log("VendorError:  " + E.getErrorCode());
                }
            }
        }
    }
    
    /*------------------------------------------------------------------*/
    void showAbout() {
        IJ.showMessage("About SQLResults.",
        "This plug-in filter interfaces MySQL and ImageJ"
        );
    }
    
    
    
    
    private byte mapRoiType(int roiType) {
        byte polygon=0, rect=1, oval=2, line=3, freeline=4, polyline=5, noRoi=6, freehand=7, traced=8, angle=9;
        byte type;
        switch (roiType) {
            case Roi.POLYGON:
                type = polygon;
                break;
            case Roi.FREEROI:
                type = freehand;
                break;
            case Roi.TRACED_ROI:
                type = traced;
                break;
            case Roi.OVAL:
                type = oval;
                break;
            case  Roi.LINE:
                type = line;
                break;
            case Roi.POLYLINE:
                type = polyline;
                break;
            case Roi.FREELINE:
                type = freeline;
                break;
            case Roi.ANGLE:
                type = angle;
                break;
            default :
                type = rect;
                
        }
        return type;
        
    }
    
    @SuppressWarnings("unused")
	private byte[] encodeROI(Roi roi){
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        int roi_type=roi.getType();
        
        int n=0;
        int[] x=null,y=null;
        try {
            //Rectangle r = roi.getBoundingRect();
            Rectangle r = roi.getBounds();
            // IJ.log("x"+ r.x);
            byte[] header={73,111,117,116}; // "Iout"
            os.write(header);
            
            os.write(putShort(217));
            os.write(mapRoiType(roi_type));
            os.write(0);
            
            if (roi instanceof PolygonRoi) {
                PolygonRoi p = (PolygonRoi)roi;
                n = p.getNCoordinates();
                x = p.getXCoordinates();
                y = p.getYCoordinates();
            }
            
            
            // IJ.log("top: "+os.size());
            os.write(putShort(r.y));			//top
            os.write(putShort(r.x));			//left
            os.write(putShort(r.y+r.height));	//bottom
            os.write(putShort(r.x+r.width));	//right
            os.write(putShort(n));
            
            if (roi instanceof Line) {
                Line l = (Line)roi;
                //IJ.log("line start: "+os.size());
                os.write(putFloat(l.x1));
                os.write(putFloat(l.y1));
                os.write(putFloat(l.x2));
                os.write(putFloat(l.y2));
            }
            //IJ.log("line end: "+os.size());
            int u=64-os.size(); // the header is 64 bytes
            os.write(new byte[u]);
            
            if (n>0) {
                for (int i=0; i<n; i++)
                    os.write(putShort(x[i]));
                
                for (int i=0; i<n; i++)
                    os.write(putShort(y[i]));
                
            }
            byte[] b=os.toByteArray();
            // IJ.log(os.toString());
            os.close();
            return b;
            
        }
        catch (IOException Ex) {
            IJ.log("IOException");
            return null;
        }
        
    }
    
    private  byte[] putShort(int v) {
        byte[] data=new byte[2];
        data[0] = (byte)(v>>>8);
        data[1] = (byte)v;
        return data;
    }
    
    private  byte[]  putFloat(float v) {
        byte[] data=new byte[4];
        int tmp = Float.floatToIntBits(v);
        data[0]   = (byte)(tmp>>24);
        data[1] = (byte)(tmp>>16);
        data[2] = (byte)(tmp>>8);
        data[3] = (byte)tmp;
        return data;
    }
    
    
    // Variables declaration - do not modify
    private java.awt.Button btnClose;
    private java.awt.Button btnSend;
    private java.awt.Button btnRestart;
    private java.awt.Checkbox chbox;
    private java.awt.Label label1;
    private java.awt.Panel panel1;
    private java.awt.Panel panel2;
    private java.awt.TextArea ta;
    private java.awt.Label label2;
    private java.awt.Choice origfilelist;
    // End of variables declaration
    
    
	public static void main(String[] args) {
		/*
		 * try {
		 * 
		 * File f=new File(args[0]);
		 * 
		 * if (f.exists() && f.isDirectory() ) { System.setProperty("plugins.dir",
		 * args[0]);
		 * 
		 * } else {
		 * 
		 * throw new IllegalArgumentException(); } } catch (Exception ex) {
		 * IJ.log("plugins.dir misspecified\n"); ex.printStackTrace(); }
		 */
		new ImageJ();
		IJ.run("Blobs (25K)");
		
		SQLliteResults_ resplugin = new SQLliteResults_();
		
		JFileChooser fileChooser = new JFileChooser();

		// For Directory
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int rVal = fileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			String file=fileChooser.getSelectedFile().toString();
	 		System.out.println(file);
			//resplugin.connStart(file);
			resplugin.run(file);
		}
		
		
		
		

	}

}
