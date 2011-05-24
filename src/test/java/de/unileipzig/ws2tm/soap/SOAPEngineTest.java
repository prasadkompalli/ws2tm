package de.unileipzig.ws2tm.soap;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.junit.Test;

import de.unileipzig.ws2tm.request.TMQLRequest;
import de.unileipzig.ws2tm.ws.soap.Message;
import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.factory.SOAPEngine;

public class SOAPEngineTest {

	public Message msg;
	
	@Test
	public void requestEngine() throws SOAPException, IOException {
		TMQLRequest request = new TMQLRequest();
		Operation op = request.addOperation(new QName("http://example.org/operation#","CallWebService","o"));
		op.addParameter(new QName("parameter"), "64");
		
		msg = SOAPEngine.newInstance(new URL("http://example.org")).sendMessage(request);
	}
		
}
