/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap.impl;

import de.unileipzig.ws2tm.ws.soap.Authentication;

/**
 * <b>Class AuthenticationImpl</b> implements class {@link Authentication}
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/31)
 *
 */
public class AuthenticationImpl implements Authentication {

	private String pw;
	private String userName;

	
	public AuthenticationImpl(String user, String pw) {
		this.setUserName(user);
		this.setUserPassword(pw);
	}

	@Override
	public boolean securityRequired() {
		if (this.pw != null && this.userName != null) {
			return true;
		}
		return false;
	}

	@Override
	public String getUserPassword() {
		return this.pw;
	}
	

	@Override
	public String getUserName() {
		return this.userName;
	}
	
	@Override
	public void setUserPassword(String pw) {
		if (pw == null || pw.length() == 0) {
			throw new NullPointerException();
		}
		this.pw = pw;
	}
	
	@Override
	public void setUserName(String name) {
		if (name == null || name.length() == 0) {
			throw new NullPointerException();
		}
		this.userName = name;
	}}
