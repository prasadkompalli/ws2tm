/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

import java.util.Set;

import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import de.unileipzig.ws2tm.ws.soap.factory.SOAPEngine;

/**
 * <b>Interface Message</b>
 * 
 * <p>
 * This interface acts as the main interface between SOAP and SOAP2TM or a TM itself. This interface
 * collects the information about the send request and it received response. The implementation is done
 * via class {@link SOAPEngine} and its inner class. First a request will be created via the soap engine.
 * After the request will be send to the web service if a connection can be established, and finally a
 * response generated depending if an error occurred or the server responded with a soap response itself.
 * </p>
 * 
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/14)
 *
 *
 */
public interface Message {
	
	public SOAPMessage getRequest();
	
	public SOAPMessage getResponse();
	
	public boolean errorOccurred();
	
	public Set<SOAPFault> getErrors();
	
}
