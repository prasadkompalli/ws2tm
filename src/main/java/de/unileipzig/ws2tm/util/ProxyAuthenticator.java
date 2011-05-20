/**
 * 
 */
package de.unileipzig.ws2tm.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * This class was created to authenticate a user against a proxy or firewall in the 
 * intra-net. If the authentication still fails the network-administration configured
 * the DNS not properly enough or the user and password are wrong.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/07)
 *
 */
public class ProxyAuthenticator extends Authenticator {

	private String user, pw;
	
	/**
	 * @param user 
	 * @param pw 
	 */
	public ProxyAuthenticator(String user, String pw) {
		this.user = user;
		this.pw = pw;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, pw.toCharArray());
	}
	
}
