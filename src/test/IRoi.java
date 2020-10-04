package test;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/*
 * Injectable functionality for ROIs
 */
public interface IRoi {

	byte polygon=0, rect=1, oval=2, line=3, freeline=4, polyline=5, noRoi=6, freehand=7, traced=8, angle=9;
	 /**
     * 
     * @param roiType
     * @return
     */
	default byte mapRoiType(int roiType) {
        
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

	/**
	 * 
	 * @param roi
	 * @return
	 */
	default byte[] encodeROI(Roi roi){
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
        catch (IOException ex) {
        	ex.printStackTrace();
            return null;
        }
        
    }

	/**
     * 
     * @param v
     * @return
     */
	default  byte[] putShort(int v) {
        byte[] data=new byte[2];
        data[0] = (byte)(v>>>8);
        data[1] = (byte)v;
        return data;
    }

	/**
     * 
     * @param v
     * @return
     */
 
	default byte[]  putFloat(float v) {
        byte[] data=new byte[4];
        int tmp = Float.floatToIntBits(v);
        data[0] = (byte)(tmp>>24);
        data[1] = (byte)(tmp>>16);
        data[2] = (byte)(tmp>>8);
        data[3] = (byte)tmp;
        return data;
    }
}