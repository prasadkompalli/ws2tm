package de.unileipzig.ws2tm.util;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class WebServiceConfiguratorTest {

	
	@Test
	public void existingConfiguration() throws IOException {	
		assertNotNull(WebServiceConfigurator.getTrustStore());
		assertNotNull(WebServiceConfigurator.getTrustStorePassword());
	}
	
	@Test
	public void newConfiguration() throws IOException {	
		try {
			/*
			 * the following call should fail because the xml file does not exist. 
			 * Therefore the old configuration should be used further.
			 */
			WebServiceConfigurator.newInstance().loadOtherConfiguration("./src/main/resources/test.xml");
		} catch (FileNotFoundException e) {
			// this exception is intended!!!!!
		}
		assertNotNull(WebServiceConfigurator.getTrustStore());
		assertNotNull(WebServiceConfigurator.getTrustStorePassword());
	}
	
}
