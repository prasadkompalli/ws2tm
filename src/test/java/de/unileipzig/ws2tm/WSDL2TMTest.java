/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.wsdl.WSDLException;

import org.junit.Test;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.factory.TopicMapEngine;

/**
 * <b>Test class for testing the functionality of class {@link WSDL2TM}
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/05)
 */
public class WSDL2TMTest {

	@Test
	public void createWSDLInstance1() throws URISyntaxException, WSDLException {
		File file = new File("wsdl/MyWSDL-XSD-Example.wsdl");
		TopicMapEngine.OVERWRITE = true;
		WebService2TopicMap ws2tm = WebService2TopicMapFactory.createWebService();
		try {
			ws2tm.newWebService(file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
