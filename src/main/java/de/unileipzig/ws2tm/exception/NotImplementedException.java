/**
 * 
 */
package de.unileipzig.ws2tm.exception;

/**
 * This exception tries to abstract every possible error, which occurs while accessing options, parameters
 * and possible if-then loops which are not supported or were not implemented yet.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/24)
 *
 */
public class NotImplementedException extends UnsupportedOperationException {

	/**
	 * Default Serial VERSION UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @see NotImplementedException
	 */
	public NotImplementedException() {
		this("The method does not supported the action you tried to do.");
	}

	/**
	 * Constructor with 1 parameter
	 * @param message - detailed message
	 * @see NotImplementedException
	 */
	public NotImplementedException(String message) {
		super(message);
	}
	
	/**
	 * Constructor with 2 parameter
	 * @param message - detailed message
	 * @param e - exception stack which let to this exception
	 * @see NotImplementedException
	 */
	public NotImplementedException(String message, Throwable e) {
		super(message,e);
	}
	
}
