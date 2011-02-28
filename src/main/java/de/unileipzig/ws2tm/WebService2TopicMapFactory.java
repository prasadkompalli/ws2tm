/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.wsdl.*;
import javax.wsdl.extensions.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.*;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Role;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapExistsException;

import com.sun.xml.xsom.XSType;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;
import de.unileipzig.ws2tm.util.WebServiceConnector;
import de.unileipzig.ws2tm.ws.soap.Authentication;
import de.unileipzig.ws2tm.ws.soap.Parameter;
import de.unileipzig.ws2tm.ws.soap.RequestObject;
import de.unileipzig.ws2tm.ws.soap.impl.AuthenticationImpl;
import de.unileipzig.ws2tm.ws.xsd.SchemaParser;

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
	
	private static String getInfo(Topic ta) {
		if (ta.getNames().size() > 0) {
			return ta.getNames().iterator().next().getValue();
		}
		if (ta.getSubjectIdentifiers().size() > 0) {
			return ta.getSubjectIdentifiers().iterator().next().getReference();
		}
		if (ta.getItemIdentifiers().size() > 0) {
			return ta.getItemIdentifiers().iterator().next().getReference();				
		}
		if (ta.getSubjectLocators().size() > 0) {
			return ta.getSubjectLocators().iterator().next().getReference();				
		}
		return ta.getId();
	}


	private class WebService2TopicMapImpl implements WebService2TopicMap {

		protected WSDL2TMImpl wsdl2tm;
		
		protected SOAP2TMImpl soap2tm;
		
		private Authentication auth;
		
		private boolean merged = false;
		
		private boolean changeOccurred = false;
		
		private TopicMap mergedTopicMap;
		
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
		public TopicMap newWebService(String wsdlPath) throws IOException, InitializationException {
			//TODO what happens if the wsdl2tm will be overwritten by calling this function twice with different wsdl paths (web service definitions)
			TopicMap tm = null;
			try {
				tm = TopicMapEngine.newInstance().createNewTopicMapInstance(new File(WebServiceConfigurator.getFileWSDL2TM()), NS_WSDL2TM);
				Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlPath);
				this.wsdl2tm = new WSDL2TMImpl(wsdl, tm);
				TopicMapEngine.newInstance().write(tm);
			} catch (MalformedIRIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FactoryConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TopicMapExistsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (WSDLException e) {
				throw new IOException("WSDL could not be retrieved from path "+ wsdlPath, e);
			}

			return wsdl2tm.load();
		}

		@Override
		public TopicMap newWebServiceRequest(RequestObject request) throws IOException, InitializationException {
			if (wsdl2tm == null) {
				throw new InitializationException("The web service needs to be initialized first before requests can be done. Please call function #newWebService(String) first or consult the documentation.");
			}
			if (soap2tm == null) {
				try {
					soap2tm = new SOAP2TMImpl(this.getConnectionParameter());
				} catch (MalformedIRIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FactoryConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TopicMapExistsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.getConnectionParameter();
			this.soap2tm.request(this.soap2tm.init(),request);
			
			return this.soap2tm.load();
		}

		@Override
		public boolean authenticationRequired() {
			if (this.auth.securityRequired())
				return true;
			return false;
		}

		@Override
		public Authentication getAuthentication() {
			return this.auth;
		}

		@Override
		public void setAuthenticationParameter(String user, String pw) {
			this.auth = new AuthenticationImpl(user,pw);
		}
		
		/**
		 * This function returns a valid URL pointing to the end point of a web service.
		 * The valid url should contain an existing web service host and an existing resource, which
		 * can be connected. This url will be required to establish a working connection between
		 * client and web server.
		 * 
		 * @return URL containing the host and path to the web service end point
		 * @throws MalformedURLException if the URL can not be created because an error exists in the url candidate (invalid sign, invalid protocol)
		 */
		private URL getConnectionParameter() throws MalformedURLException {
			Topic service = wsdl2tm.load().getTopicBySubjectIdentifier(wsdl2tm.load().createLocator(WebService2TopicMapFactory.NS_WSDL+"Service"));
			String address = null;
			// Retrieve all existing topics of the web service description topic map
			for (Topic t : wsdl2tm.load().getTopics()) {
				// Check if the topic has the type NS_WSDL+"Service"
				if (t.getTypes().contains(service)) {
					// Retrieve all existing occurrences for topic
					for (Occurrence o : t.getOccurrences()) {
						// Check if the type of the occurrence points to NS_WSDL2TM+"LocationURI"
						if (o.getType().getSubjectIdentifiers().contains(wsdl2tm.load().createLocator(NS_WSDL2TM+ "LocationURI"))) {
							address = o.getValue();
							if (log.isDebugEnabled()) {
								log.debug("Found connection url: "+address);
							}
						}
					}
				}
			}
			if (address == null) {
				throw new MalformedURLException("Unable to create a valid url because the provided web service description does not contain any connection address.");
			}
			
			return new URL(address);
		}

	}

	private class SOAP2TMImpl implements TopicMapAccessObject {

		private TopicMap tm;

		private URL url;
		private SOAP2TMImpl(URL url) throws MalformedIRIException, FactoryConfigurationException, IllegalArgumentException, TopicMapExistsException, IOException {
			this.tm = TopicMapEngine.newInstance().createNewTopicMapInstance(new File(WebServiceConfigurator.getFileSOAP2TM()), WebService2TopicMapFactory.NS_SOAP2TM);
			this.url = url;
		}

		private void request(SOAPMessage msg, RequestObject request) {
			
			for (de.unileipzig.ws2tm.ws.soap.Operation op : request.getOperations()) {
				SOAPBodyElement body;
				try {
					body = msg.getSOAPBody().addBodyElement(op.getQName());
					for (Parameter para : op.getParameters()) {
						body.addChildElement(para.getQName()).addTextNode(para.getValue());
					}
				} catch (SOAPException e) {
					log.error("Operation "+op.getName()+" created an error while adding their parameter to a soap message instance.",e);
				}
			}
			
			// the next lines are using class WebServiceConnector to establish to send and receive the response
			
//			WebServiceConnector.setProxySettings("141.80.150.1", "80");
//			WebServiceConnector.setProxyAuthentification("mdcguest", "h47dkAKF");
			
			WebServiceConnector wsc = WebServiceConnector.newConnection(url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(wsc.sendRequest(msg)));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				log.fatal("Received response could not be read. BufferedReader could not read any line probably.",e);
			}
		}

		private SOAPMessage init() {
			SOAPMessage msg = null;
			try {
				msg = MessageFactory.newInstance().createMessage();
			} catch (SOAPException e) {
				log.fatal("It is not possible to create soap message instances through class "+MessageFactory.class.getCanonicalName()+".",e);
			}
			return msg;
		}

		@Override
		public Set<Association> getAssociations() {
			return this.tm.getAssociations();
		}

		@Override
		public Set<Occurrence> getOccurrences() {
			Set<Occurrence> occs = new HashSet<Occurrence>();
			for (Topic t: this.getTopics()){
				occs.addAll(t.getOccurrences());
			}
			return occs;
		}

		@Override
		public Set<Topic> getTopics() {
			return this.tm.getTopics();
		}

		@Override
		public TopicMap load() {
			return tm;
		}

		@Override
		public void save(TopicMap tm) {
			// NOTHING NEEDS TO BE DONE HERE. This function will not be
			// implemented.
		}
	}

	/**
	 * @author Torsten Grigull
	 * 
	 * Service-Port
	 * Port-Binding
	 * Binding-PortType
	 * Binding-BindingOperation
	 * BindingOperation-BindingInput
	 * BindingOperation-BindingOutput
	 * BindingOperation-BindingFault
	 * PortType-Operation
	 * Operation-Input
	 * Operation-Output
	 * Operation-Fault
	 * Input-Message
	 * Output-Message
	 * Message-Part
	 * Part-Types
	 */
	// TODO Documentation required for every defined association
	private enum WSDLAssociation {
		relation_service_port,
		relation_service_operation,
		relation_port_binding,
		relation_binding_operation,
		relation_bindingoperation_message,
		relation_bindingoperation_input,
		relation_bindingoperation_output,
		relation_bindingoperation_fault,
		relation_binding_porttype,
		relation_porttype_operation,
		relation_operation_message,
		relation_operation_input,
		relation_operation_output,
		relation_operation_fault,
		relation_input_message,
		relation_output_message,
		relation_fault_message,
		relation_message_part,
		relation_part_types
	}
	
	private enum WSDLTopic {
		service,
		port,
		binding,
		bindingop,
		bindingop_input,
		bindingop_output,
		bindingop_fault,
		porttype,
		operation,
		operation_input,
		operation_output,
		operation_fault,
		message,
		part,
		types
	}
	
	private enum WSDLRoles {
		service,
		port,
		binding,
		bindingop,
		bindingop_input,
		bindingop_output,
		bindingop_fault,
		porttype,
		operation,
		operation_input,
		operation_output,
		operation_fault,
		message,
		part,
		types		
	}
	
	
	private enum IDs {
		ItemIdentifier,
		SubjectIdentifier,
		SubjectLocator
	}
	
	protected class TopicE {
		private boolean exists;
		private Topic topic;

		public TopicE(Topic topic, boolean exists) {
			if (log.isDebugEnabled()) {
				log.debug("Created new instance of class "+TopicE.class.getCanonicalName()+": "+WebService2TopicMapFactory.getInfo(topic));
			}			
			this.topic = topic;
			this.exists = exists;
		}
		
		public boolean exists() {
			return exists;
		}
		
		public Topic getTopic() {
			return topic;
		}
	}
	
	protected class NameE {
		private String name;
		private Set<Topic> scopes;
		
		public NameE(String name, Topic...scopes){
			log.debug("Created new instance of class "+NameE.class.getCanonicalName()+": "+name);
			this.name = name;
			
			this.scopes = new HashSet<Topic>();
			for (Topic topic : scopes) {
				this.scopes.add(topic);
			}
			
		}
		
		public String getName() {
			return this.name;
		}
		
		public Set<Topic> getScopes() {
			return this.scopes;
		}
		
	}
	
	private class WSDL2TMImpl implements TopicMapAccessObject {
		
		private TopicMap tm;
		private String tns;
		private Topic deutsch;
		private Topic english;
		
		private HashMap<String, URL> namespaces;
		
		private HashMap<WSDLAssociation, Topic> ascs;
		private HashMap<WSDLTopic, Topic> topicTypes;
		private HashMap<WSDLRoles, Topic> topicRoles;
		private String unset = "Not defined";
		private Topic dataType;
		
		private WSDL2TMImpl(Definition wsdl) throws InitializationException,MalformedIRIException, FactoryConfigurationException,IllegalArgumentException, TopicMapExistsException, IOException {
			this(wsdl, WebService2TopicMapFactory.NS_WSDL2TM+ new Random().nextLong());
		}

		private WSDL2TMImpl(Definition wsdl, String namespaceURI) throws InitializationException, MalformedIRIException,FactoryConfigurationException, IllegalArgumentException,TopicMapExistsException, IOException {
			this(wsdl, TopicMapEngine.newInstance().createNewTopicMapInstance(null, namespaceURI));
		}

		private WSDL2TMImpl(Definition wsdl, TopicMap tm) throws InitializationException {
			if (wsdl == null || tm == null) {
				throw new InitializationException("The parameters need to be initialized. Otherwise the web service description cannot be transformed to a topic map.");
			}
			
			log.debug("Created new instance of class "+WSDL2TMImpl.class.getCanonicalName());
			this.tm = tm;
			this.tns = wsdl.getTargetNamespace();
			if (!tns.endsWith("/")) {
				tns = tns+"/";
			}
			log.info("Using web service description "+wsdl.getDocumentBaseURI());
			
			Topic language = this.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/", IDs.ItemIdentifier).getTopic();
			deutsch = this.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/deutsch", IDs.ItemIdentifier).getTopic();
			deutsch.createName("deutsch");
			deutsch.addType(language);
			
			english = this.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/english", IDs.ItemIdentifier).getTopic();
			english.createName("english");
			english.addType(language);
			
			dataType = this.createTopic(NS_WebService+"DataType", IDs.ItemIdentifier).getTopic();
			
			this.ascs = this.createAssociations();
			if (this.ascs.size() > 0) {
				log.info(WSDL2TMImpl.class.getSimpleName()+": Created successfully Assocations");
			}
			this.topicTypes = this.createTopicTypes();
			if (this.topicTypes.size() > 0) {
				log.info(WSDL2TMImpl.class.getSimpleName()+": Created successfully Topic Types");
			}
			this.topicRoles = this.createRoles();
			if (this.topicRoles.size() > 0) {
				log.info(WSDL2TMImpl.class.getSimpleName()+": Created successfully Roles for Topics");
			}
			
			this.namespaces = new HashMap<String, URL>();
			Map<String,String> map = (Map<String,String>) wsdl.getNamespaces();
			for (Map.Entry<String, String> e : map.entrySet()) {
				this.addNameSpace(e.getKey(), e.getValue());
			}
			this.addNameSpace("TNS", this.tns);
			
			Iterator<ExtensibilityElement> it = wsdl.getTypes().getExtensibilityElements().iterator();
			while (it.hasNext()) {
				ExtensibilityElement e = it.next();
				QName name = e.getElementType();
				if (name.getNamespaceURI().equalsIgnoreCase("http://www.w3.org/2001/XMLSchema") && name.getLocalPart().equalsIgnoreCase("schema")) {
					Schema s = (Schema) e;
					
					try {
						SchemaParser.getFactory().addSchema(name.getNamespaceURI());
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					
//					SchemaParser.addSchema(s);
				}
				
				System.out.println("Elements: "+it.next().getElementType().getNamespaceURI());
			}
			
			System.exit(2);
			
			this.init(wsdl);
		}
		
		/**
		 * This method simplifies the adding process for name spaces.
		 * 
		 * @param prefix - prefix for name space
		 * @param url - URL pointing to the name space
		 * 
		 * @see #addNameSpace(String, URL)
		 */
		private void addNameSpace(String prefix, String url) {
			try {
				this.addNameSpace(prefix, new URL(url));
			} catch (MalformedURLException e) {
				log.error("Could not add namespace to list of name spaces because of an mal formed URL",e);
			}
		}
		
		/**
		 * This method simplifies the adding process for name spaces.
		 * 
		 * @param prefix - prefix for name space
		 * @param url - URL pointing to the name space
		 * 
		 * @see #addNameSpace(String, String)
		 */
		private void addNameSpace(String prefix, URL url) {
			this.namespaces.put(prefix.toUpperCase(), url);
			log.debug("Added new name space: "+prefix+":"+url.toString());
		}
		
		/**
		 * This method creates all required associations. The following list
		 * contains all created associations and their purpose.
		 * 
		 * <p>
		 * <ul>
		 * Association <i>sub_category</i>:
		 * <li>WSDL-Service</li>
		 * <li>Service-Port</li>
		 * <li>Service-Binding</li>
		 * <li>Binding-Operation</li>
		 * <li>Binding-PortType</li>
		 * <li>PortType-Operation</li>
		 * <li>PortType-Message</li>
		 * <li>Message-Part</li>
		 * <li>Message-Types</li>
		 * <li>Part-Types</li>
		 * <li>Part-DataTypes</li>
		 * <li>Types-DataTypes</li>
		 * </ul>
		 * </p>
		 */
		private HashMap<WSDLAssociation, Topic> createAssociations() {
			HashMap<WSDLAssociation, Topic> ascs = new HashMap<WSDLAssociation, Topic>();
			ascs.put(WSDLAssociation.relation_service_port, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_service_port.name()),
							new NameE("Relation Service Port"),
							new NameE("Serviceschnittstellen", deutsch)
					));
			ascs.put(WSDLAssociation.relation_port_binding, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_port_binding),
							new NameE("Relation Port Binding"),
							new NameE("Schnittstellenbindungen", deutsch)
					));
			ascs.put(WSDLAssociation.relation_binding_porttype, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_binding_porttype),
							new NameE("Relation Binding PortType"),
							new NameE("PortType-Bindungen", deutsch)
					));
			ascs.put(WSDLAssociation.relation_service_operation,
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_service_operation), 
							new NameE("Service Operations"),
							new NameE("Service-Funktionen", deutsch)
					));
			ascs.put(WSDLAssociation.relation_binding_operation, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_binding_operation.name()),
							new NameE("Relation Binding BindingOperation"),
							new NameE("Service Operationen",deutsch)
					));
			ascs.put(WSDLAssociation.relation_bindingoperation_input, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_input), 
							new NameE("Relation BindingOperation Input")
					));
			ascs.put(WSDLAssociation.relation_bindingoperation_output, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_output), 
							new NameE("Relation BindingOperation Output")
					));
			ascs.put(WSDLAssociation.relation_bindingoperation_fault, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_fault), 
							new NameE("Relation BindingOperation Fault")
					));
			ascs.put(WSDLAssociation.relation_bindingoperation_message, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_message.name()), 
							new NameE("Relation BindingOperation Message")
					));
			ascs.put(WSDLAssociation.relation_porttype_operation, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_porttype_operation),
							new NameE("Relation PortType Operation")
					));
			ascs.put(WSDLAssociation.relation_operation_input, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_input.name()),
							new NameE("Relation PortType Operation Input")
					));
			ascs.put(WSDLAssociation.relation_operation_output, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_output.name()),
							new NameE("Relation PortType Operation Output")
					));
			ascs.put(WSDLAssociation.relation_operation_fault, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_fault.name()),
							new NameE("Relation PortType Operation Fault")
					));
			ascs.put(WSDLAssociation.relation_operation_message, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_message.name()), 
							new NameE("Relation Operation Message")
					));
			ascs.put(WSDLAssociation.relation_input_message, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_input_message.name()),
							new NameE("Relation Input Message")
					));
			ascs.put(WSDLAssociation.relation_output_message, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_output_message.name()),
							new NameE("Relation Output Message")
					));
			ascs.put(WSDLAssociation.relation_message_part, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_message_part.name()),
							new NameE("Relation Message Part", english)
					));
			ascs.put(WSDLAssociation.relation_part_types, 
					this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_part_types.name()),
							new NameE("Relation Part Types", english),
							new NameE("Abstrakte Typendefinition", deutsch)
					));
			return ascs;
		}

		private Topic createAssociation(Locator locator, NameE... names) {
			Topic ascType = this.createTopic(locator, IDs.SubjectIdentifier).getTopic();
			ascType.addType(this.createTopic(NS_WSDL2TM+ "sub_category_association_type", IDs.ItemIdentifier).getTopic());
			ascType.getTypes().iterator().next().addType(tm.createTopicByItemIdentifier(tm.createLocator("http://psi.topicmaps.org/tmcl/topic-type")));
			for (NameE name : names) {
				ascType.createName(name.getName(), name.getScopes());
			}
			return ascType;
		}

		private HashMap<WSDLTopic, Topic> createTopicTypes() {
			HashMap<WSDLTopic, Topic> topics = new HashMap<WSDLTopic, Topic>();
			Topic topicType = this.createTopic("http://psi.topicmaps.org/tmcl/topic-type", IDs.ItemIdentifier).getTopic();
			
			Topic service = this.createTopic(NS_WSDL+"Service", IDs.SubjectIdentifier).getTopic();
			service.addType(topicType);			
			Topic port = this.createTopic(NS_WSDL+"Port", IDs.SubjectIdentifier).getTopic();
			port.addType(topicType);
			Topic binding = this.createTopic(NS_WSDL+"Binding", IDs.SubjectIdentifier).getTopic();
			binding.addType(topicType);
			Topic bindingop = this.createTopic(NS_WSDL+"BindingOperation", IDs.SubjectIdentifier).getTopic();
			bindingop.addType(topicType);
			Topic porttype = this.createTopic(NS_WSDL+"PortType", IDs.SubjectIdentifier).getTopic();
			porttype.addType(topicType);
			Topic operation = this.createTopic(NS_WSDL+"Operation", IDs.SubjectIdentifier).getTopic();
			operation.addType(topicType);
			Topic bindinginput = this.createTopic(NS_WSDL+"BindingInput", IDs.SubjectIdentifier).getTopic();
			bindinginput.addType(topicType);
			Topic bindingoutput = this.createTopic(NS_WSDL+"BindingOutput", IDs.SubjectIdentifier).getTopic();
			bindingoutput.addType(topicType);
			Topic bindingfault = this.createTopic(NS_WSDL+"BindingFault", IDs.SubjectIdentifier).getTopic();
			bindingfault.addType(topicType);
			Topic input = this.createTopic(NS_WSDL+"Input", IDs.SubjectIdentifier).getTopic();
			input.addType(topicType);
			Topic output = this.createTopic(NS_WSDL+"Output", IDs.SubjectIdentifier).getTopic();
			output.addType(topicType);
			Topic fault = this.createTopic(NS_WSDL+"Fault", IDs.SubjectIdentifier).getTopic();
			fault.addType(topicType);
			Topic message = this.createTopic(NS_WSDL+"Message", IDs.SubjectIdentifier).getTopic();
			message.addType(topicType);
			Topic part = this.createTopic(NS_WSDL+"Part", IDs.SubjectIdentifier).getTopic();
			part.addType(topicType);
			Topic types = this.createTopic(NS_WSDL+"Types", IDs.SubjectIdentifier).getTopic();
			types.addType(topicType);	
			
			topics.put(WSDLTopic.service, service);
			topics.put(WSDLTopic.port, port);
			topics.put(WSDLTopic.binding, binding);
			topics.put(WSDLTopic.bindingop, bindingop);
			topics.put(WSDLTopic.porttype, porttype);
			topics.put(WSDLTopic.operation, operation);
			topics.put(WSDLTopic.bindingop_input, bindinginput);
			topics.put(WSDLTopic.bindingop_output, bindingoutput);
			topics.put(WSDLTopic.bindingop_fault, bindingfault);
 			topics.put(WSDLTopic.operation_input, input);
			topics.put(WSDLTopic.operation_output, output);
			topics.put(WSDLTopic.operation_fault, fault);
			topics.put(WSDLTopic.message, message);
			topics.put(WSDLTopic.part, part);
			topics.put(WSDLTopic.types, types);
			
			return topics;
		}
		
		private HashMap<WSDLRoles, Topic> createRoles() {
			HashMap<WSDLRoles, Topic> roles = new HashMap<WSDLRoles, Topic>();
			Topic roleType = tm.createTopicByItemIdentifier(tm.createLocator("http://psi.topicmaps.org/tmcl/role-type"));
			
			roles.put(WSDLRoles.service, this.createRole(NS_WSDL2TM+"Service-Role", roleType, 
					new NameE("Service",english),
					new NameE("Service", deutsch))
			);
			roles.put(WSDLRoles.port, this.createRole(NS_WSDL2TM+"ServicePort-Role", roleType, 
					new NameE("Service-Port",english),
					new NameE("Service-Port", deutsch))
			);
			roles.put(WSDLRoles.binding, this.createRole(NS_WSDL2TM+"Binding-Role", roleType, 
					new NameE("Binding",english),
					new NameE("Binding", deutsch))
			);
			roles.put(WSDLRoles.bindingop, this.createRole(NS_WSDL2TM+"BindingOperation-Role", roleType, 
					new NameE("Binding Operation",english),
					new NameE("Binding Funktion", deutsch))
			);
			roles.put(WSDLRoles.bindingop_input, this.createRole(NS_WSDL2TM+"BindingInput-Role", roleType, 
					new NameE("Input", english),
					new NameE("Eingabe", deutsch))
			);
			roles.put(WSDLRoles.bindingop_output, this.createRole(NS_WSDL2TM+"BindingOutput-Role", roleType, 
					new NameE("Output", english),
					new NameE("Ausgabe", deutsch))
			);
			roles.put(WSDLRoles.bindingop_fault, this.createRole(NS_WSDL2TM+"BindingFault-Role", roleType, 
					new NameE("Binding Operation Fault", english),
					new NameE("Binding Funktionsfehler", deutsch))
			);
			roles.put(WSDLRoles.porttype, this.createRole(NS_WSDL2TM+"PortType-Role", roleType, 
					new NameE("PortType",english),
					new NameE("PortType", deutsch))
			);
			roles.put(WSDLRoles.operation, this.createRole(NS_WSDL2TM+"Operation-Role", roleType, 
					new NameE("Operation",english),
					new NameE("Funktion", deutsch))
			);
			roles.put(WSDLRoles.operation_input, this.createRole(NS_WSDL2TM+"OperationInput-Role", roleType, 
					new NameE("Input", english),
					new NameE("Eingabe", deutsch))
			);
			roles.put(WSDLRoles.operation_output, this.createRole(NS_WSDL2TM+"OperationOutput-Role", roleType, 
					new NameE("Output", english),
					new NameE("Ausgabe", deutsch))
			);
			roles.put(WSDLRoles.operation_fault, this.createRole(NS_WSDL2TM+"OperationFault-Role", roleType, 
					new NameE("Operation Fault", english),
					new NameE("Funktionsfehler", deutsch))
			);
			roles.put(WSDLRoles.message, this.createRole(NS_WSDL2TM+"Message-Role", roleType, 
					new NameE("Message", english),
					new NameE("Nachricht", deutsch))
			);
			roles.put(WSDLRoles.part, this.createRole(NS_WSDL2TM+"MessagePart-Role", roleType, 
					new NameE("Message Part", english),
					new NameE("Nachrichtenparameter", deutsch))
			);
			roles.put(WSDLRoles.types, this.createRole(NS_WSDL2TM+"Types-Role", roleType, 
					new NameE("Type Definition", english),
					new NameE("Typendefinition", deutsch))
			);
			
			return roles;
		}
		
		private Topic createRole(String id, Topic type, NameE... names) {
			Topic topic = this.createTopic(id, IDs.SubjectIdentifier).getTopic();
			if (type != null) {
				topic.addType(type);
			}
			for (NameE name : names) {
				topic.createName(name.getName(), name.getScopes());
			}
			return topic;
		}
		
		/**
		 * <b>Method init</b> <br>
		 * <p>
		 * This method is the heart of class {@link WSDL2TM}. First a recursive
		 * function starts reading all import statements, which can be found as
		 * tags in the current WSDL. After the real transformation begins
		 * between both paradigms.
		 * <ul>
		 * <li><b>Types</b> - a container for data type definitions using some
		 * type system (such as XSD)</li>
		 * <li><b>Message</b> - an abstract, typed definition of the data being
		 * communicated</li>
		 * <li><b>PortType</b> (Interface) - an abstract set of operations
		 * supported by one or more endpoints</li>
		 * <li><b>Binding</b> - a concrete protocol and data format
		 * specification for a particular port type</li>
		 * <li><b>Service</b> - a collection of related endpoints</li>
		 * </ul>
		 * </p>
		 * 
		 * <p>
		 * Next to the five main elements of a WSDL exist two other sub
		 * elements. They describe e.g. Service and PortType in more detail by
		 * using the elements <i>Operation</i> and/or <i>Port</i>.
		 * <ul>
		 * <li><b>Operation</b> - an abstract description of an action supported
		 * by the service</li>
		 * <li><b>Port</b> - a single endpoint defined as a combination of a
		 * binding and a network address</li>
		 * </ul>
		 * </p>
		 * 
		 * @param wsdl
		 *            - Instance of class {@link Definition}. Definition means
		 *            the root element of a WSDL.
		 * @throws IOException
		 */
		// Unchecked because of Transformation between entrySet of all imports
		// and
		// String, List<Import> Map Entries.
		@SuppressWarnings("unchecked")
		private void init(Definition wsdl) throws InitializationException {

			
			/*
			 * Including all existing IMPORT statements to other wsdl
			 * definitions
			 */
			try {
				retrieveImports(wsdl);
			} catch (IOException e3) {
				throw new InitializationException("Critical error during retrieval of other web service descriptions through import statements.",e3);
			}
			
			/*
			 * Iterate through Service by calling function {@link #associateTopics(Object)}
			 */
			Iterator<Service> it_service = wsdl.getServices().values().iterator();
			while (it_service.hasNext()) {
				Service s = it_service.next();
				
				/*
				 * Simplified Topic Map structure due to reduce the complexity
				 * and create a better overview.
				 * Occurrences can be retrieved fast nower.
				 */
				this.initiateServiceToTopicMap(s, wsdl.getTypes());
//				this.associateTopics(s);
			}
		}
		
		
		@SuppressWarnings("unchecked")
		private void initiateServiceToTopicMap(Service s, Types t) {
			
			TopicE serviceE = this.createTopic(s.getQName(), IDs.ItemIdentifier);
			if (serviceE.exists()) {
				return;
			}
			
			Topic service = serviceE.getTopic();
			service.addType(topicTypes.get(WSDLTopic.service));
			
			Iterator<Port> it_ports = s.getPorts().values().iterator();
			while (it_ports.hasNext()) {
				Port port = it_ports.next();
				this.addOccurrences(service, port.getExtensibilityElements().iterator());
				this.addOccurrences(service, port.getBinding().getExtensibilityElements().iterator());
				this.addOccurrences(service, port.getBinding().getPortType().getExtensibilityElements().iterator());
				
				HashMap<String, BindingOperation> map_binop = new HashMap<String, BindingOperation>();
				Iterator<BindingOperation> it_binop = port.getBinding().getBindingOperations().iterator();
				while (it_binop.hasNext()) {
					BindingOperation binop = it_binop.next();
					map_binop.put(tns+binop.getName(), binop);
				}
				
				Iterator<Operation> it_op = port.getBinding().getPortType().getOperations().iterator();
				while (it_op.hasNext()) {
					Operation op = it_op.next();
					
					Topic opT = this.associateTopics(s, op,
							topicTypes.get(WSDLTopic.service), 
							topicTypes.get(WSDLTopic.operation),
							topicRoles.get(WSDLRoles.service),
							topicRoles.get(WSDLRoles.operation),
							WSDLAssociation.relation_service_operation);
					
					BindingOperation binop = map_binop.get(tns+ op.getName());
					this.addOccurrences(opT, binop.getExtensibilityElements().iterator());
					
					this.associateTopics(op, op.getInput().getMessage(), topicRoles.get(WSDLRoles.operation_input), binop.getBindingInput().getExtensibilityElements().iterator());
					this.associateTopics(op, op.getOutput().getMessage(), topicRoles.get(WSDLRoles.operation_output), binop.getBindingOutput().getExtensibilityElements().iterator());
					
					Iterator<Fault> it = op.getFaults().values().iterator();
					while (it.hasNext()) {
						Fault fault = it.next();
						BindingFault binFault = binop.getBindingFault(fault.getName());
						this.associateTopics(op, fault.getMessage(), topicRoles.get(WSDLRoles.operation_fault), binFault.getExtensibilityElements().iterator());
						
					}
					
					
				}
				
			}
		}
		
		@SuppressWarnings("unchecked")
		private Topic associateTopics(Operation op, Message msg, Topic type, Iterator<ExtensibilityElement> it) {
			Topic m = this.associateTopics(op, msg, 
					topicTypes.get(WSDLTopic.operation), 
					topicTypes.get(WSDLTopic.message), 
					topicRoles.get(WSDLRoles.operation), 
					type, 
					WSDLAssociation.relation_operation_message);
			
			this.addOccurrences(m, it);
			
			Iterator<Part> it_part = msg.getParts().values().iterator();
			while (it_part.hasNext()) {
				Part part = it_part.next();
				QName qname = new QName(tns, tns);
				if (part.getTypeName() != null) {
					// complex or simple type of xsd -> Occurrence
					 qname = part.getTypeName();
				} else {
					// element name of xsd -> Association to next Element (macht wenig Sinn, denn ComplexType kann ArrayOfFloat sein
					qname = part.getElementName();
				}
				
				TopicE dataE = this.createTopic(qname, IDs.SubjectIdentifier);
				Topic p = this.associateTopics(msg, part,
						topicTypes.get(WSDLTopic.message), 
						dataE.getTopic(),
						topicRoles.get(WSDLRoles.message),
						topicRoles.get(WSDLRoles.part),
						WSDLAssociation.relation_message_part);
				
				p.createOccurrence(dataType , unset, dataE.getTopic());
				
				
			}
			
			return m;
		}

		@SuppressWarnings("unchecked")
		private void associateTopics(Object a) {
			if (Service.class.isInstance(a)) {
				Iterator<Port> it = ((Service) a).getPorts().values().iterator();
				while (it.hasNext()) {
					Port port = (Port) it.next();
					this.associateTopics(a, port, 
							topicTypes.get(WSDLTopic.service), 
							topicTypes.get(WSDLTopic.port), 
							topicRoles.get(WSDLRoles.service), 
							topicRoles.get(WSDLRoles.port), 
							WSDLAssociation.relation_service_port);
					this.associateTopics(port);
				}
			} else if (Port.class.isInstance(a)) {
				Port port = (Port) a;
				this.associateTopics(port, port.getBinding(), 
						topicTypes.get(WSDLTopic.port), 
						topicTypes.get(WSDLTopic.binding),
						topicRoles.get(WSDLRoles.port),
						topicRoles.get(WSDLRoles.binding),
						WSDLAssociation.relation_port_binding);
				this.associateTopics(port.getBinding());
			} else if (Binding.class.isInstance(a)) {
				Binding bin = (Binding) a;
				Iterator<BindingOperation> it = bin.getBindingOperations().iterator();
				while (it.hasNext()) {
					BindingOperation op = it.next();
					this.associateTopics(bin, op, 
							topicTypes.get(WSDLTopic.binding), 
							topicTypes.get(WSDLTopic.operation),
							topicRoles.get(WSDLRoles.binding),
							topicRoles.get(WSDLRoles.operation),
							WSDLAssociation.relation_service_operation);
					this.associateTopics(op);
				}
				if (bin.getPortType() != null) {
				this.associateTopics(bin, bin.getPortType(), 
						topicTypes.get(WSDLTopic.binding), 
						topicTypes.get(WSDLTopic.porttype),
						topicRoles.get(WSDLRoles.binding),
						topicRoles.get(WSDLRoles.porttype),
						WSDLAssociation.relation_binding_porttype);
				this.associateTopics(bin.getPortType());
				}
			} else if (BindingOperation.class.isInstance(a)) {
				BindingOperation op = (BindingOperation) a;
				this.associateTopics(op, op.getBindingInput(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.bindingop_input), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.bindingop_input), 
						WSDLAssociation.relation_bindingoperation_input);
				this.associateTopics(op, op.getBindingOutput(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.bindingop_output), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.bindingop_output), 
						WSDLAssociation.relation_bindingoperation_output);
				Iterator<BindingFault> bin_fau = op.getBindingFaults().values().iterator();
				while (bin_fau.hasNext()) {
					this.associateTopics(op, bin_fau.next(),
							topicTypes.get(WSDLTopic.operation), 
							topicTypes.get(WSDLTopic.bindingop_fault), 
							topicRoles.get(WSDLRoles.operation), 
							topicRoles.get(WSDLRoles.bindingop_fault), 
							WSDLAssociation.relation_bindingoperation_fault);					
				}
				if (op.getBindingInput() != null) {
					this.associateTopics(op.getBindingInput());
				}
				if (op.getBindingOutput() != null) {
					this.associateTopics(op.getBindingOutput());
				}
			} else if (BindingInput.class.isInstance(a)) {
				// Nothing to do here...
			} else if (BindingOutput.class.isInstance(a)) {
				// Nothing to do here...
			} else if (BindingFault.class.isInstance(a)) {
				// Nothing to do here...
			} else if (PortType.class.isInstance(a)) {
				Iterator<Operation> it = ((PortType) a).getOperations().iterator();
				while (it.hasNext()) {
					Operation op = it.next();
					this.associateTopics(a, op, 
							topicTypes.get(WSDLTopic.porttype), 
							topicTypes.get(WSDLTopic.operation),
							topicRoles.get(WSDLRoles.porttype),
							topicRoles.get(WSDLRoles.operation),
							WSDLAssociation.relation_service_operation);
					this.associateTopics(op);
				}
			} else if (Operation.class.isInstance(a)) {
				Operation op = (Operation) a;
				this.associateTopics(op, op.getInput().getMessage(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.message), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.operation_input), 
						WSDLAssociation.relation_operation_message);
				this.associateTopics(op, op.getOutput().getMessage(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.message), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.operation_output), 
						WSDLAssociation.relation_operation_message);
				Iterator<Fault> it = op.getFaults().values().iterator();
				while (it.hasNext()) {
					Fault fault = it.next();
					this.associateTopics(op, fault.getMessage(),
							topicTypes.get(WSDLTopic.operation), 
							topicTypes.get(WSDLTopic.message), 
							topicRoles.get(WSDLRoles.operation), 
							topicRoles.get(WSDLRoles.operation_fault), 
							WSDLAssociation.relation_operation_message);					
					this.associateTopics(fault.getMessage());
				}
				if (op.getInput() != null) {
					this.associateTopics(op.getInput().getMessage());
				}
				if (op.getOutput() != null) {
					this.associateTopics(op.getOutput().getMessage());
				}
			} else if (Message.class.isInstance(a)) {
				Iterator<Part> it = ((Message) a).getParts().values().iterator();
				while (it.hasNext()) {
					Part part = it.next();
					TopicE dataE = null;
					if (part.getTypeName() != null) {
						 dataE = this.createTopic(part.getTypeName(), IDs.SubjectIdentifier);
					} else {
						dataE = this.createTopic(part.getElementName(), IDs.SubjectIdentifier);
					}
					this.associateTopics(a, part,
							topicTypes.get(WSDLTopic.message), 
							dataE.getTopic(),
							topicRoles.get(WSDLRoles.message),
							topicRoles.get(WSDLRoles.part),
							WSDLAssociation.relation_message_part);
					this.associateTopics(part);
				}
			} else if (Part.class.isInstance(a)) {
				
			}
		}
		
		/**
		 * @param a
		 * @param b
		 * @param typea
		 * @param typeb
		 * @param ra
		 * @param rb
		 * @param asc
		 * 
		 * Service-Port
		 * Service-Binding
		 * Port-Binding
		 * Binding-PortType
		 * Binding-BindingOperation
		 * BindingOperation-BindingInput
		 * BindingOperation-BindingOutput
		 * BindingOperation-BindingFault
		 * PortType-Operation
		 * Operation-Input
		 * Operation-Output
		 * Operation-Fault
		 * Input-Message
		 * Output-Message
		 * Message-Part
		 * Part-Types
		 * 
		 */
		@SuppressWarnings("unchecked")
		private Topic associateTopics(Object a, Object b, Topic typea, Topic typeb, Topic ra, Topic rb, WSDLAssociation choose) {
			
			
			TopicE t;
			Topic temp = null;
			Topic ta = null;
			Topic tb = null;
			
			Object[] objs = new Object[]{a,b};
			int i = 0;
			for (Object obj : objs) {
				if (Service.class.isInstance(obj)) { 
					Service ser = (Service) obj;
					t = this.createTopic(ser.getQName(), IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(ser.getQName().getLocalPart());
						this.addOccurrences(temp, ser.getExtensibilityElements().iterator());
					}
				} else if (Port.class.isInstance(obj)) { 
					Port por = (Port) obj;
					t = this.createTopic(tns+ por.getName());
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(por.getName());
						this.addOccurrences(temp, por.getExtensibilityElements().iterator());						
					}
				} else if (Binding.class.isInstance(obj)) { 
					Binding bin = (Binding) obj;
					t = this.createTopic(bin.getQName(), IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(bin.getQName().getLocalPart());
						this.addOccurrences(temp, bin.getExtensibilityElements().iterator());
					}
				} else if (BindingOperation.class.isInstance(obj)) {
					BindingOperation binop = (BindingOperation) obj;
					t = this.createTopic(tns+ binop.getName(), IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(binop.getName());
						this.addOccurrences(temp, binop.getExtensibilityElements().iterator());						
					}
				} else if (BindingInput.class.isInstance(obj)) {
					BindingInput binin = (BindingInput) obj;
					t = this.createTopic(NS_WSDL+"BindingInput", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Input");
						this.addOccurrences(temp, binin.getExtensibilityElements().iterator());
					}
				} else if (BindingOutput.class.isInstance(obj)) {
					BindingOutput binou = (BindingOutput) obj;
					t = this.createTopic(NS_WSDL+"BindingOutput", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Output");
						this.addOccurrences(temp, binou.getExtensibilityElements().iterator());
					}
				} else if (BindingFault.class.isInstance(obj)) { 
					BindingFault binfa = (BindingFault) obj;
					t = this.createTopic(NS_WSDL+"BindingFault", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Fault");
						this.addOccurrences(temp, binfa.getExtensibilityElements().iterator());
					}
				} else if (PortType.class.isInstance(obj)) {
					PortType pot = (PortType) obj;
					t = this.createTopic(pot.getQName(), IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(pot.getQName().getLocalPart());
					}
				} else if (Operation.class.isInstance(obj)) { 
					Operation ope = (Operation) obj;
					t = this.createTopic(tns+ope.getName(), IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(ope.getName());
						this.addOccurrences(temp, ope.getExtensibilityElements().iterator());
					}
				} else if (Input.class.isInstance(obj)) {
					t = this.createTopic(NS_WSDL+"Input", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Input");
					}
				} else if (Output.class.isInstance(obj)) {
					t = this.createTopic(NS_WSDL+"Output", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Output");
					}
				} else if (Fault.class.isInstance(obj)) {
					t = this.createTopic(NS_WSDL+"Fault", IDs.SubjectIdentifier);
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName("Fault");
					}
				} else if (Message.class.isInstance(obj)) {
					Message mes = (Message) obj;
					t = this.createTopic(mes.getQName());
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(mes.getQName().getLocalPart());
						this.addOccurrences(temp,mes.getExtensibilityElements().iterator());
					}
				} else if (Part.class.isInstance(obj)) {
					Part par = (Part) obj;
					t = this.createTopic(tns+par.getName());
					temp = t.getTopic();
					if (!t.exists()) {
						temp.createName(par.getName());
					}
				} else if (Types.class.isInstance(obj)) {
					// actually useless, because types will be recognized via PART-element (see part above)
					Types typ = (Types) obj;
				}
				
				if (i==0) {
					ta = temp;
					i++;
				} else {
					tb = temp;
				}
			}
			
			if (ta != null && tb != null) {

				ta.addType(typea);
				tb.addType(typeb);
				
				this.createAssociation(ascs.get(choose), ta, ra, tb, rb);
				
			}
			
			return tb;
	
		}

		private Association createAssociation(Topic ascType, Topic topic_a, Topic role_a, Topic topic_b, Topic role_b) {
			
			for (Association asc : tm.getAssociations()) {
				if (asc.getRoleTypes().contains(role_a) && asc.getRoleTypes().contains(role_b)) {
					boolean ba = false, bb = false;
					for (Role role : asc.getRoles()) {
						if (topic_a.equals(role.getPlayer())) {
							ba = true;
						} else if (topic_b.equals(role.getPlayer())) {
							bb = true;
						}
					}
					
					if (ba == true && bb == true) {
						if (log.isDebugEnabled()) {
							String nta = WebService2TopicMapFactory.getInfo(topic_a);
							String ntb = WebService2TopicMapFactory.getInfo(topic_b);
							String nra = WebService2TopicMapFactory.getInfo(role_a);
							String nrb = WebService2TopicMapFactory.getInfo(role_b);
							
							log.debug("Association exists already between topic "+nta+" ("+nra+") with topic "+ntb+" ("+nrb+") via assocation "+WebService2TopicMapFactory.getInfo(ascType));
						}	
						return asc;
					}
				}
			}
			
			Association asc = tm.createAssociation(ascType);
			asc.createRole(role_a, topic_a);
			asc.createRole(role_b, topic_b);	
			
			if (log.isDebugEnabled()) {
				String nta = WebService2TopicMapFactory.getInfo(topic_a);
				String ntb = WebService2TopicMapFactory.getInfo(topic_b);
				String nra = WebService2TopicMapFactory.getInfo(role_a);
				String nrb = WebService2TopicMapFactory.getInfo(role_b);
				
				log.debug("Associate topic "+nta+" ("+nra+") with topic "+ntb+" ("+nrb+") via assocation "+WebService2TopicMapFactory.getInfo(ascType));
			}	
			
			return asc;
		}

		private TopicE createTopic(QName qname) {
			String ns = qname.getNamespaceURI();
			if (!qname.getNamespaceURI().endsWith("/")) {
				ns = qname.getNamespaceURI()+"/";
			}
			return this.createTopic(ns+qname.getLocalPart());
		}
		
		private TopicE createTopic(String identifier) {
			return this.createTopic(identifier, IDs.SubjectIdentifier);
		}
		
		private TopicE createTopic(Locator loc, IDs id) {
			return this.createTopic(loc.getReference(), id);
		}
		
		private TopicE createTopic(QName qname, IDs id) {
			String ns = qname.getNamespaceURI();
			if (!qname.getNamespaceURI().endsWith("/")) {
				ns = qname.getNamespaceURI()+"/";
			}
			return this.createTopic(ns+qname.getLocalPart(), id);
		}
		
		private TopicE createTopic(String identifier, IDs id) {
			Topic topic = tm.createTopic();
			
			switch (id) {
				case ItemIdentifier: 
					for (Topic t : tm.getTopics()) {
						if (t.getItemIdentifiers().contains(tm.createLocator(identifier))) {
							if (log.isDebugEnabled()) {
								log.debug("Returning already existing topic with item identifier "+identifier);
							}
							return new TopicE(t,true);
						}
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with item identifier "+identifier);
					}
					topic = tm.createTopicByItemIdentifier(tm.createLocator(identifier));
					break;
				case SubjectIdentifier: 
					topic = tm.getTopicBySubjectIdentifier(tm.createLocator(identifier));
					if (topic != null) {
						if (log.isDebugEnabled()) {
							log.debug("Returning already existing topic with subject identifier "+identifier);
						}
						return new TopicE(topic, true);
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with subject identifier "+identifier);
					}
					topic = tm.createTopicBySubjectIdentifier(tm.createLocator(identifier));
					break;
				case SubjectLocator: 
					topic = tm.getTopicBySubjectLocator(tm.createLocator(identifier));
					if (topic != null) {
						if (log.isDebugEnabled()) {
							log.debug("Returning already existing topic with subject locator "+identifier);
						}
						return new TopicE(topic, true);
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with subject locator "+identifier);
					}
					topic = tm.createTopicBySubjectLocator(tm.createLocator(identifier));
					break;
			}
			
			return new TopicE(topic, false);
		}

		private void addOccurrences(Topic topic, Iterator<ExtensibilityElement> it) {
			String ns, lp;
			String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
			String NS_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
			while (it.hasNext()) {
				ExtensibilityElement e = it.next();
				ns = e.getElementType().getNamespaceURI();
				lp = e.getElementType().getLocalPart();
				if ((NS_SOAP+"address").equals(ns+lp)) {
					SOAPAddress soa = (SOAPAddress) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+ "LocationURI")), soa.getLocationURI());
					occ.getType().createName("Location of Web Service",english);
				} else if ((NS_SOAP12+"address").equals(ns+lp)) {
					e.getElementType();
					SOAP12Address soa = (SOAP12Address) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+ "LocationURI")), soa.getLocationURI());
					occ.getType().createName("Location of Web Service",english);					
				} else if ((NS_SOAP+"operation").equals(ns+lp)) {
					SOAPOperation soa = (SOAPOperation) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+"ActionURI")),soa.getSoapActionURI());
					occ.getType().createName("Action Address", english);
				} else if ((NS_SOAP+"binding").equals(ns+lp)) {
					SOAPBinding soa = (SOAPBinding) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+ "TransportProtocol")), soa.getTransportURI());
					occ.getType().createName("Transport Protocol", english);
				} else if ((NS_SOAP+"body").equals(ns+lp)) {
					SOAPBody soa = (SOAPBody) e;
					// Parts
					// Use
					Iterator<String> encodings = soa.getEncodingStyles().iterator();
					while (encodings.hasNext()) {
						//TODO test this
						System.out.println(encodings.next());
					}
					if (soa.getUse() != null) {
						Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+"Usage")),soa.getUse());
						occ.getType().createName("Encoding", english);
					}
					// encodingStyle
					// namespace
				}
				// soap header
				// soap header -> soap headerfault
				// soap fault
				


			}
		}

		@SuppressWarnings("unchecked")
		private void retrieveImports(Definition wsdl) throws IOException {
			try {
				Iterator<Import> it_import = wsdl.getImports().values()
						.iterator();
				while (it_import.hasNext()) {
					Import i = it_import.next();
					log.debug("Found import statement: will retrieve import of wsdl "+i.getLocationURI());
					if (i.getNamespaceURI() == null) {
						this.tm.mergeIn(new WSDL2TMImpl(i.getDefinition()).load());
					} else {
						this.tm.mergeIn(new WSDL2TMImpl(i.getDefinition(), i.getNamespaceURI()).load());
					}
				}
			} catch (Exception e) {
				throw new IOException("Unable to retrieve other web service description via import statements.",e);
			}
		}

		@Override
		public Set<Association> getAssociations() {
			if (this.tm.getAssociations() != null) {
				return this.tm.getAssociations();
			}
			return Collections.emptySet();
		}

		@Override
		public Set<Occurrence> getOccurrences() {
			Set<Occurrence> set = Collections.emptySet();

			for (Topic t : this.getTopics()) {
				set.addAll(t.getOccurrences());
			}

			return set;
		}

		@Override
		public Set<Topic> getTopics() {
			if (this.tm.getTopics() != null) {
				return this.tm.getTopics();
			}
			return Collections.emptySet();
		}

		@Override
		public TopicMap load() {
			return tm;
		}

		@Override
		public void save(TopicMap tm) {
			// NOTHING NEEDS TO BE DONE HERE. This function will not be
			// implemented.
		}
	}
}
