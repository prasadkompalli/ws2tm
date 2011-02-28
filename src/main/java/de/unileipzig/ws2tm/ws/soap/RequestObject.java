/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * <b>Interface RequestObject</b>
 * 
 * <p>
 * This interface tries to abstract the required functions and parameters for a request to a SOAP web service.
 * 
 * </p>
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/14)
 *
 */
public class RequestObject {

	protected Set<Operation> ops;
	
	public RequestObject() {
		ops = new HashSet<Operation>();
	}
	
	/**
	 * This method returns the created set of operations in a defined order as a {@link Collection}.
	 * 
	 * @return a set of operations in a predefined order
	 * @see #getOperation(int)
	 */
	public Collection<Operation> getOperations() {
		return this.ops;
	}
	
	/**
	 * This operation returns the instance of class {@link Operation} at the assigned index.
	 * 
	 * @param index - integer value greater than 0 and index 
	 * @return Operation at assigned index
	 */
	public Operation getOperation(int index) {
		return (Operation) this.ops.toArray()[index];		
	}
	
	/**
	 * Add operations by calling this method. The first parameter assigns the name of the operation, while the
	 * second parameter contains all parameters for the operation itself with a key-value pairs.
	 * 
	 * @param name - name of operation
	 * @param parameters - list of key-value pairs (parameters of operation, name and their value)
	 * 
	 * @see #addOperation(QName, Parameter...)
	 */
	public void addOperation(QName name, HashMap<QName, String> parameters) {
		Operation op = new Operation(name);
		for (QName para : parameters.keySet()) {
			op.addParameter(para, parameters.get(para));
		}
		this.addOperation(op);
	}

	/**
	 * This method is similar to method {@link #addOperation(QName, HashMap)}. While the parameters differ from each other.
	 * The first parameter remains the name of the operation to use. The second and more parameters are however the parameters
	 * for the operation itself. The second parameter is the name of the first parameter of the operation, the third is its value,
	 * the fourth the name of the second parameter, the fifth the value of it and so fourth. This method should be used 
	 * especially for calls with an operation name, and only zero to one parameter.
	 * 
	 * @param name - name of operation
	 * @param parameters - list of key-value pairs as a list of strings (key, value, key, value etc.)
	 * 
	 * @see #addOperation(QName, HashMap)
	 */
	public Operation addOperation(QName name, Parameter... parameters) {
		Operation op = new Operation(name);
		for (Parameter param : parameters) {
			op.addParameter(param);
		}
		return this.addOperation(op);
	}
	
	/**
	 * @param op
	 * @return
	 */
	public Operation addOperation(Operation op) {
		this.ops.add(op);
		return op;
	}
	
}

