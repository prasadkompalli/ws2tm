/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.junit.Test;
import org.w3c.dom.DOMException;

import de.unileipzig.ws2tm.factory.SOAPEngine;
import de.unileipzig.ws2tm.request.TMQLRequest;
import de.unileipzig.ws2tm.ws.soap.SOAPMessage;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/01/17)
 *
 */
public class SOAP2TMTest {

	
	@Test
	public void requestServer() throws SOAPException, DOMException, IOException, WSDLException {
		TMQLRequest request = new TMQLRequest();
		request.addOperation(new QName("http://example.org/operation"), new QName("http://example.org/parameter"), new String());
		SOAPMessage msg = SOAPEngine.newRequest(request);
	}
	
}
