/**
 * 
 */
package de.unileipzig.ws2tm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.persistence.Configuration;
import de.unileipzig.ws2tm.persistence.dao.ConfigurationDAO;

/**
 * <b>WebServiceConfigurator</b> is a factory class providing 
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 *
 */
public class WebServiceConfigurator implements Factory {

	private static Logger log = Logger.getLogger(WebServiceConfigurator.class);
	
	//FIXME this needs a fix, because the configuration will be probably at a different place on any other system (could be in the same directory as the jar, class files etc., or in /etc/ or on a windows file system
	private static final WebServiceConfigurator FACTORY = new WebServiceConfigurator(new File("./src/main/resources/WSConfiguration.xml"));
	
	private Configuration config;
	
	/**
	 * Constructor
	 */
	private WebServiceConfigurator(File file) {
		log.info("Initializing factory instance of class "+WebServiceConfigurator.class.getCanonicalName());
		try {
			config = ConfigurationDAO.newInstance().load(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			log.warn("Could not load configuration from path "+file.getAbsolutePath()+". Will proceed by using an empty configuration.",e);
		} catch (IOException e) {
			log.warn("Could not load configuration from path "+file.getAbsolutePath()+". Will proceed by using an empty configuration.",e);
		}
	}

	public static WebServiceConfigurator newInstance() {
		return FACTORY;
	}
	
	public void loadOtherConfiguration(String filePath) throws FileNotFoundException, IOException {
		config = ConfigurationDAO.newInstance().load(filePath);
	}
	
	private Configuration init() {
		if (config == null) {
			config = ConfigurationDAO.newInstance().create();
			log.info("Created new configuration using method create() of class "+ConfigurationDAO.class.getCanonicalName());
		}
		return config;
	}
	
	public static String getNameSpaceWS2TM() {
		return FACTORY.init().getValue("de.ws2tm.ns.ws2tm");
	}
	
	public static String getTrustStore() {
		return FACTORY.init().getValue("de.ws2tm.ssl.trustStore");
	}
	
	public static String getTrustStorePassword() {
		return FACTORY.init().getValue("de.ws2tm.ssl.trustStore.Password");		
	}

	public static String getFileWSDL2TM() {
		return FACTORY.init().getValue("de.ws2tm.file.wsdl2tm");
	}

	public static String getFileSOAP2TM() {
		return FACTORY.init().getValue("de.ws2tm.file.soap2tm");
	}
	
	public static boolean isProxySettingExisting() {
		if (getProxyPort() != null && getProxyIP() != null) {
			return true;
		}
		return false;
	}
	
	public static String getProxyPort() {
		return FACTORY.init().getValue("de.ws2tm.proxy.port");
	}
	
	public static String getProxyIP() {
		return FACTORY.init().getValue("de.ws2tm.proxy.ip");
	}
	
	public static boolean isProxyAuthenticationExisting() {
		if (getProxyUserName() != null && getProxyUserPassword() != null) {
			return true;
		}
		return false;
	}
	
	public static String getProxyUserName() {
		return FACTORY.init().getValue("de.ws2tm.proxy.user");		
	}
	
	public static String getProxyUserPassword() {
		return FACTORY.init().getValue("de.ws2tm.proxy.userpw");
	}

	/**
	 * @return 
	 */
	public static String getLoggingDirectory() {
		String dir = FACTORY.init().getValue("de.ws2tm.logging.directory");
		if (dir == null) {
			dir = "tmp";
		}
		
		return dir;
	}
	
	
}
