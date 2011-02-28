/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

/**
 * <b>Interface Authentication</b>
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/31)
 *
 */
public interface Authentication {

	/**
	 * This method returns <code>true</code> only if the security parameters like user name and password
	 * are set. 
	 * 
	 * @return <code>true</code if the security parameters are set and therefore required
	 * 
	 * @see #setUserName(String)
	 * @see #setUserPassword(String)
	 */
	public boolean securityRequired();

	/**
	 * Getter method to return the user password or security token to authenticate the SOAP request.
	 * 
	 * @return the user password or security token
	 * @see #getUserName()
	 */
	public String getUserPassword();

	/**
	 * Setter method to set the user name or identity token to identify the user against the existing
	 * web service for a web service request (SOAP request).
	 * 
	 * @param pw - the password or security token to authenticate a user against a web service registration
	 * 
	 * @see #setUserName(String)
	 */
	public void setUserPassword(String pw);

	/**
	 * Getter method to return the user name or user identification which could be a hash or simple string.
	 * 
	 * @return the user name or identity token for identifying the user against the web service
	 * 
	 * @see #getUserPassword()
	 */
	public String getUserName();

	/**
	 * Setter method to set the user name or identity token to identify the user against the existing
	 * web service for a web service request (SOAP request).
	 * 
	 * @param name - user name or string of the identity against the web service
	 * 
	 * @see #setUserPassword(String)
	 */
	public void setUserName(String name);

}
