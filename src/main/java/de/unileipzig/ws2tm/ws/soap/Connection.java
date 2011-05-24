/**
 * me.master.thesis - de.unileipzig.ws2tm.ws.soap
 *
 * === Connection.java ====
 *
 */
package de.unileipzig.ws2tm.ws.soap;

import java.io.InputStream;

import javax.xml.soap.MimeHeaders;

/**
 * @author Torsten Grigull
 * @version 0.1 (27.04.2011)
 *
 */
//TODO DOCUMENTATION needed!!
public interface Connection {
	
	public MimeHeaders getMimeHeaders();
	
	public InputStream getInputStream();

}
