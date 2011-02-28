/**
 * 
 */
package de.unileipzig.ws2tm.exception;

/**
 * <b>InitializationException</b> is thrown if an exception occurred during the initialization of a
 * web service description, its topic map or the whole request system. However, other exceptions are
 * also possible. See the cause of the exception and the log files if this error occurred.
 * 
 * 
 * @author Torsten Grigull
 *
 */
public class InitializationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InitializationException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public InitializationException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public InitializationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InitializationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
