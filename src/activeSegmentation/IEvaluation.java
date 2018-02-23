package activeSegmentation;

import java.util.List;

/**
 * 				
 *   
 * 
 * @author Sumit Kumar Vohra and Dimiter Prodanov , IMEC
 *
 *
 * @contents
 * Interface for evaluation, It is genric interace for evaluating our 
 * classification model, This interface is still under development
 * New updates will be soon
 * 
 * 
 * @license This library is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU Lesser General Public
 *      License as published by the Free Software Foundation; either
 *      version 2.1 of the License, or (at your option) any later version.
 *
 *      This library is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *       Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public
 *      License along with this library; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
public interface IEvaluation {
	
	/**
	 * This method is used to get metrics for evaluation
	 * @return List of metrics
	 */
	public List<String> getMetrics();
	
	/**
	 * This method is used to test the testdata set using selected metrics
	 * @param  instances
	 * @param  selection
	 * @return string
	 */
	public String testModel(IDataSet instances,List<String> selection);
}
