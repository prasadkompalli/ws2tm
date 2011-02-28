/**
 * 
 */
package de.unileipzig.ws2tm.request;

import java.util.HashMap;


import javax.xml.namespace.QName;

import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.RequestObject;

/**
 * <b>Class TMQLRequest</b>
 * 
 * <p>
 * This class extends class {@link RequestObject}, which represents the basis for every possible request.
 * TMQLRequest tries to abstract a tmql request, which is inform a part of a topic map or a statement concluding in a topic map.
 * This statement or topic map needs to be added to transform them to an {@link Operation}. The operations are required to
 * encapsulate the information in a soap request, which can be done via WebService2TopicMapFactory and its instances.
 * </p>
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/15)
 *
 */
public class TMQLRequest extends RequestObject {

	
	/**
	 * 
	 */
	public TMQLRequest() {
	}

	@Override
	public void addOperation(QName name, HashMap<QName, String> paramters) {
		Operation op = new Operation(name);
		ops.add(op);
	}

}
