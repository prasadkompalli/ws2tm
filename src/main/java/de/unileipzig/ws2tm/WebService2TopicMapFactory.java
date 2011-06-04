/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;


import org.apache.log4j.Logger;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;
import de.unileipzig.ws2tm.ws.soap.Authentication;
import de.unileipzig.ws2tm.ws.soap.RequestObject;
import de.unileipzig.ws2tm.ws.soap.impl.AuthenticationImpl;
import de.unileipzig.ws2tm.ws.soap.impl.SOAP2TMImpl;
import de.unileipzig.ws2tm.ws.wsdl.impl.WSDL2TMImpl;

/**
 * <b>Factory WebService2TopicMap</b> is the main access point to access the
 * functionality of the programm <i>web service to topic map</i>. This factory
 * receives requests made by the user. The user has to implement interface
 * {@link RequestObject} to send requests. The requests can encapsulate e.g.
 * TMQL requests or web requests consisting only out of strings.
 * 
 * The request will be processed and the result will be returned in form of a
 * topic map.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/017)
 * 
 */
public class WebService2TopicMapFactory implements Factory {

	private static WebService2TopicMapFactory FACTORY;

	private static Logger log = Logger.getLogger(WebService2TopicMapFactory.class);
	
	public final static String NS_WebService = WebServiceConfigurator.getNameSpaceWS2TM();

	public final static String NS_SOAP2TM = NS_WebService + "/SOAP2TM/";

	public final static String NS_WSDL2TM = NS_WebService + "/WSDL2TM/";

	public final static String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";

	public final static String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";

	public final static String NS_XSD = "http://www.w3.org/2001/XMLSchema";

	public  final static String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	private List<WebService2TopicMap> instances;

	private WebService2TopicMapFactory() {
		instances = new ArrayList<WebService2TopicMap>();
	}

	public static WebService2TopicMap createWebService() {
		if (FACTORY == null) {
			FACTORY = new WebService2TopicMapFactory();
		}

		return FACTORY.createNewInstance();
	}

	private WebService2TopicMap createNewInstance() {
		WebService2TopicMap ws2tm = new WebService2TopicMapImpl();
		instances.add(ws2tm);
		return ws2tm;
	}
	
	private class WebService2TopicMapImpl implements WebService2TopicMap {

		protected WSDL2TMImpl wsdl2tm;
		
		protected SOAP2TMImpl soap2tm;
		
		private Authentication auth;
		
		private boolean merged = false;
		
		private boolean changeOccurred = false;
		
		private TopicMap mergedTopicMap;
		
		private Collection<URL> connectionParameters;
		
		@Override
		public TopicMap mergeTopicMaps() {
			if (merged == true && changeOccurred == false) {
				return mergedTopicMap;
			}
			if (changeOccurred == true || this.mergedTopicMap == null) {
				this.mergedTopicMap = wsdl2tm.load();
				this.mergedTopicMap.mergeIn(soap2tm.load());
			}
			return this.mergedTopicMap;
		}

		@Override
		public TopicMapAccessObject newWebService(String wsdlPath) throws IOException, InitializationException {
			//BROKENWINDOW what happens if the wsdl2tm will be overwritten by calling this function twice with different wsdl paths (web service definitions)
			TopicMap tm = null;
			try {
				tm = TopicMapEngine.newInstance().createNewTopicMapInstance(new File(WebServiceConfigurator.getFileWSDL2TM()), NS_WSDL2TM);
				Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlPath);
				this.wsdl2tm = new WSDL2TMImpl(wsdl, tm);
				TopicMapEngine.newInstance().write(tm);
				for (Topic t : tm.getTopics()) {
					if (t.getTypes().contains(tm.getTopicBySubjectIdentifier(tm.createLocator(NS_WSDL+"Service")))) {
						this.setConnectionParameter(t);
					}
				}
			} catch (FactoryConfigurationException e) {
				log.fatal("The topic map engine could not be initialized. See error log for more detail.",e);
			} catch (WSDLException e) {
				throw new IOException("WSDL could not be retrieved from path "+ wsdlPath, e);
			}
			return wsdl2tm;
		}

		@Override
		public TopicMapAccessObject newWebServiceRequest(RequestObject request) throws IOException, InitializationException {
			if (wsdl2tm == null) {
				throw new InitializationException("The web service needs to be initialized first before requests can be done. Please call function #newWebService(String) first or consult the documentation.");
			}
			if (soap2tm == null) {
				try {
					soap2tm = new SOAP2TMImpl(this.getConnectionParameter().iterator().next());
				} catch (FactoryConfigurationException e) {
					log.fatal("The topic map engine could not be initialized. See error log for more detail.",e);
				}
			}
			
			this.getConnectionParameter();
			this.soap2tm.request(request);
			
			return this.soap2tm;
		}

		@Override
		public boolean authenticationRequired() {
			return this.auth.securityRequired();
		}

		@Override
		public Authentication getAuthentication() {
			return this.auth;
		}

		@Override
		public Authentication setAuthenticationParameter(String user, String pw) {
			return this.auth = new AuthenticationImpl(user,pw);
		}
		
		/**
		 * This function returns a valid URL pointing to the end point of a web service.
		 * The valid url should contain an existing web service host and an existing resource, which
		 * can be connected. This url will be required to establish a working connection between
		 * client and web server.
		 * 
		 * @return URL containing the host and path to the web service end point
		 * @throws InitializationException if no valid URL could be found. Somehow still a request exists for using one or more connection URLs
		 * They probably can not be created because of an error in the so-called url candidate (invalid sign, invalid protocol)
		 * 
		 * @see #setConnectionParameter(Topic)
		 * @see #addConnectionParameter(Occurrence)
		 * @see #addConnectionParameter(String)
		 */
		private Collection<URL> getConnectionParameter() throws InitializationException {
			if (this.connectionParameters == null || this.connectionParameters.size() == 0) {
				throw new InitializationException("The connection parameters are not initialized. Therefore they are not usable.");
			}
			return this.connectionParameters;
		}
		
		/**
		 * This method adds connection urls depending on the assigned occurrence, which is typed by the topic {@link NS_WSDL2TM}/LocationURI.
		 * The value of the occurrence should 
		 * @param locationURI - the occurrence, which represents the topic type {@link NS_WSDL2TM}/LocationURI
		 * @throws MalformedURLException if the found url in the occurrence is invalid (invalid sign, invalid protocol)
		 * @see #addConnectionParameter(String)
		 */
		/*		
 		private void addConnectionParameter(Occurrence locationURI) throws MalformedURLException {
			if (locationURI.getType().getSubjectIdentifiers().contains(wsdl2tm.load().createLocator(NS_WSDL2TM+ "LocationURI"))) { 
				this.addConnectionParameter(locationURI.getValue());
			}
		}*/
		
		/**
		 * This method tries to add a url depending if the assigned url candidate, can be transformed to a valid url.
		 * 
		 * @param urlCandidate
		 * @throws MalformedURLException if the found url on the assigned url candidate is invalid (invalid sign, invalid protocol)
		 * 
		 * @see #addConnectionParameter(Occurrence)
		 */
		private void addConnectionParameter(String urlCandidate) throws MalformedURLException {
			if (this.connectionParameters == null) {
				this.connectionParameters = new ArrayList<URL>();
			}
			this.connectionParameters.add(new URL(urlCandidate));
		}

		/**
		 * This method takes the topic representing an instance of topic type {@link NS_WSDL}/Service.
		 * It tries to get all occurrences, which are typed by topic {@link NS_WSDL2TM}/LocationURI.
		 * The value of the found occurrences will be added as an url candidate, which probably represents
		 * a connection parameter for the current web service.
		 * 
		 * @param service - topic, which is typed by topic {@link NS_WSDL}/Service
		 * @throws MalformedURLException if the found url on the found url candidate is invalid (invalid sign, invalid protocol)
		 */
		private void setConnectionParameter(Topic service) throws MalformedURLException {
			if (service.getTypes().contains(wsdl2tm.load().getTopicBySubjectIdentifier(wsdl2tm.load().createLocator(WebService2TopicMapFactory.NS_WSDL+"Service")))) {
				for (Occurrence o: service.getOccurrences()) {
					if (o.getType().getSubjectIdentifiers().contains(wsdl2tm.load().createLocator(NS_WSDL2TM+ "LocationURI"))) {
						this.addConnectionParameter(o.getValue());
					}
				}
			}
		}

	}

}
