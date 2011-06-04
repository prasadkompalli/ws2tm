/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.IOException;
import java.util.HashMap;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.junit.Test;
import org.tmapi.core.Name;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.w3c.dom.DOMException;

import de.topicmapslab.majortom.model.core.ITopic;
import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.request.TMQLRequest;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.Parameter;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/01/17)
 *
 */
public class SOAP2TMTest {

	@Test
	public void requestServer() throws SOAPException, DOMException, IOException, WSDLException {
		TMQLRequest request = new TMQLRequest();
		
		Operation op = request.addOperation(new QName("http://litwinconsulting.com/webservices/","GetWeather","ws"));
		op.addParameter("City","Los Angeles");
		
		WebService2TopicMap ws2tm = WebService2TopicMapFactory.createWebService();
		TopicMapEngine.OVERWRITE = true;
		try {
			TopicMap tm = ws2tm.newWebService("wsdl/weathergov.wsdl").load();
			ws2tm.newWebServiceRequest(request);
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
