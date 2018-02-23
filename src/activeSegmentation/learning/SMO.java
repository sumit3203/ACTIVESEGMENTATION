/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package activeSegmentation.learning;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Instance;

/**
 * @author Oscar Gabriel Reyes Pupo
 */
public class SMO extends weka.classifiers.functions.SMO {

    /**
	 * 
	 */
	private static final long serialVersionUID = -221052987032617441L;

	/**
     * Empty constructor
     */
    public SMO() {

        super();
        
        setBuildLogisticModels(true);

        //To be linear kernel, equals to used in the related papers
        setC(1.0);

        PolyKernel poly = new PolyKernel();
        poly.setExponent(1.0);

        setKernel(poly);

    }

    /**
     *
     * @return The array of Binary SMO
     */
    public BinarySMO[][] getM_classifiers() {
        return m_classifiers;
    }

    /**
     *
     * @param m_classifiers the array of binary SMO
     */
    public void setM_classifiers(BinarySMO[][] m_classifiers) {
        this.m_classifiers = m_classifiers;

    }
    
    public double SVMOutput(Instance instance){
        try {
            return m_classifiers[0][1].SVMOutput(-1, instance);
        } catch (Exception ex) {
            Logger.getLogger(SMO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}