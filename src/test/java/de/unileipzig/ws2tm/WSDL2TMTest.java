/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.wsdl.WSDLException;

import org.junit.Test;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;

/**
 * <b>Test class for testing the functionality of class {@link WSDL2TM}
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/05)
 */
public class WSDL2TMTest {

	@Test
	public void createWSDLInstance1() throws URISyntaxException, WSDLException {
		File file = new File("wsdl/AWSECommerceService.wsdl");
		TopicMapEngine.OVERWRITE = true;
		WebService2TopicMap ws2tm = WebService2TopicMapFactory.createWebService();
		try {
			TopicMap tm = ws2tm.newWebService(file.getAbsolutePath()).load();

			/*
			Topic ascRequestType = tm.createTopicBySubjectIdentifier(tm.createLocator(WebService2TopicMapFactory.NS_SOAP2TM+"WS/Request"));
			Association ascRequest = tm.createAssociation(ascRequestType);
			
			Topic ascResponseType = tm.createTopicBySubjectIdentifier(tm.createLocator(WebService2TopicMapFactory.NS_SOAP2TM+"WS/Response"));
			Association ascResponse = tm.createAssociation(ascRequestType);
			*/
			
			TopicMapEngine.newInstance().write(new File("tmp/wsdl2tm-new.xtm"), tm, TopicMapEngine.XTM_2_0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
