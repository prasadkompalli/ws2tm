/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.Name;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Role;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapExistsException;

import com.semagia.mio.helpers.HamsterHandler.IRole;

import de.topicmapslab.majortom.model.core.IAssociation;
import de.topicmapslab.majortom.model.core.IConstruct;
import de.topicmapslab.majortom.model.core.IName;
import de.topicmapslab.majortom.model.core.ITopic;
import de.topicmapslab.majortom.model.core.ITopicMap;
import de.topicmapslab.majortom.model.core.ITopicMapSystem;
import de.unileipzig.ws2tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.ws.soap.RequestObject;

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
	
	public final static String NS_WebService = "http://ws2tm.org/";

	public final static String NS_SOAP2TM = WebService2TopicMapFactory.NS_WebService + "/SOAP2TM/";

	public final static String NS_WSDL2TM = WebService2TopicMapFactory.NS_WebService + "/WSDL2TM/";

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

		@Override
		public TopicMap mergeTopicMaps() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TopicMap newWebService(String wsdlPath) throws IOException,
				InitializationException {
			TopicMap tm = null;
			TopicMapAccessObject wsdl2tm = null;
			try {
				tm = TopicMapEngine.newInstance().createNewTopicMapInstance(new File("tmp/wsdl2tm.xtm"), NS_WSDL2TM);
				Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlPath);
				wsdl2tm = new WSDL2TMImpl(wsdl, tm);
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
				throw new IOException("WSDL could not be retrieved from path "
						+ wsdlPath, e);
			}

			return wsdl2tm.load();
		}

		@Override
		public TopicMap newWebServiceRequest(RequestObject request)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private class SOAP2TMImpl implements TopicMapAccessObject {

		private TopicMap tm;

		private SOAP2TMImpl(RequestObject request) {
			this.request(request);
		}

		private void request(RequestObject request) {
			SOAPMessage msg = null;

			this.init(msg);
		}

		private void init(SOAPMessage msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public Set<Association> getAssociations() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<Occurrence> getOccurrences() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<Topic> getTopics() {
			// TODO Auto-generated method stub
			return null;
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
		relation_port_binding,
		relation_binding_operation,
		relation_bindingoperation_input,
		relation_bindingoperation_output,
		relation_bindingoperation_fault,
		relation_binding_porttype,
		relation_porttype_operation,
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

		
		private int count = 0;
		
		private TopicMap tm;
		private String tns;
		private Topic deutsch;
		private Topic english;
		private HashMap<WSDLAssociation, Topic> ascs;
		private HashMap<WSDLTopic, Topic> topicTypes;
		private HashMap<WSDLRoles, Topic> topicRoles;
		
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
			
			Topic language = tm.createTopicByItemIdentifier(tm.createLocator("http://code.topicmapslab.de/grigull-tm2speech/Language/"));
			deutsch = tm.createTopicByItemIdentifier(tm.createLocator("http://code.topicmapslab.de/grigull-tm2speech/Language/deutsch"));
			deutsch.createName("deutsch");
			deutsch.addType(language);
			english = tm.createTopicByItemIdentifier(tm.createLocator("http://code.topicmapslab.de/grigull-tm2speech/Language/english"));
			english.createName("english");
			english.addType(language);
			
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
			
			this.init(wsdl);
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
			ascs.put(WSDLAssociation.relation_service_port, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_service_port.name()),"Relation Service Port"));
			ascs.put(WSDLAssociation.relation_port_binding, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_port_binding),"Relation Port Binding"));
			ascs.put(WSDLAssociation.relation_binding_porttype, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_binding_porttype),"Relation Binding PortType"));
			ascs.put(WSDLAssociation.relation_binding_operation, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_binding_operation.name()),"Relation Binding BindingOperation"));
			ascs.put(WSDLAssociation.relation_bindingoperation_input, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_input), "Relation BindingOperation Input"));
			ascs.put(WSDLAssociation.relation_bindingoperation_output, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_output), "Relation BindingOperation Output"));
			ascs.put(WSDLAssociation.relation_bindingoperation_fault, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_fault), "Relation BindingOperation Fault"));
			ascs.put(WSDLAssociation.relation_porttype_operation, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_porttype_operation),"Relation PortType Operation"));
			ascs.put(WSDLAssociation.relation_operation_input, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_input.name()),"Relation PortType Operation Input"));
			ascs.put(WSDLAssociation.relation_operation_output, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_output.name()),"Relation PortType Operation Output"));
			ascs.put(WSDLAssociation.relation_operation_fault, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_operation_fault.name()),"Relation PortType Operation Fault"));
			ascs.put(WSDLAssociation.relation_input_message, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_input_message.name()),"Relation Input Message"));
			ascs.put(WSDLAssociation.relation_output_message, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_output_message.name()),"Relation Output Message"));
			ascs.put(WSDLAssociation.relation_message_part, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_message_part.name()),"Relation Message Part"));
			ascs.put(WSDLAssociation.relation_part_types, this.createAssociation(tm.createLocator(NS_WSDL2TM+WSDLAssociation.relation_part_types.name()),"Relation Part Types"));
			return ascs;
		}

		private Topic createAssociation(Locator locator, String name) {
			Topic ascType = this.createTopic(locator, IDs.SubjectIdentifier).getTopic();
			ascType.addType(this.createTopic(NS_WSDL2TM+ "sub_category_association_type", IDs.ItemIdentifier).getTopic());
			ascType.getTypes().iterator().next().addType(tm.createTopicByItemIdentifier(tm.createLocator("http://psi.topicmaps.org/tmcl/topic-type")));
			ascType.createName(name);
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
					new NameE("Role Service",english),
					new NameE("Element Service", deutsch))
			);
			roles.put(WSDLRoles.port, this.createRole(NS_WSDL2TM+"ServicePort-Role", roleType, 
					new NameE("Role Service Port",english),
					new NameE("Element Service Port", deutsch))
			);
			roles.put(WSDLRoles.binding, this.createRole(NS_WSDL2TM+"Binding-Role", roleType, 
					new NameE("Role Binding",english),
					new NameE("Element Binding", deutsch))
			);
			roles.put(WSDLRoles.bindingop, this.createRole(NS_WSDL2TM+"BindingOperation-Role", roleType, 
					new NameE("Role Binding Operation",english),
					new NameE("Element Binding Operation", deutsch))
			);
			roles.put(WSDLRoles.bindingop_input, this.createRole(NS_WSDL2TM+"BindingInput-Role", roleType, 
					new NameE("Role Binding Operation Input", english),
					new NameE("Element Binding Operation Input", deutsch))
			);
			roles.put(WSDLRoles.bindingop_output, this.createRole(NS_WSDL2TM+"BindingOutput-Role", roleType, 
					new NameE("Role Binding Operation Output", english),
					new NameE("Element Binding Operation Output", deutsch))
			);
			roles.put(WSDLRoles.bindingop_fault, this.createRole(NS_WSDL2TM+"BindingFault-Role", roleType, 
					new NameE("Role Binding Operation Fault", english),
					new NameE("Element Binding Operation Fault", deutsch))
			);
			roles.put(WSDLRoles.porttype, this.createRole(NS_WSDL2TM+"PortType-Role", roleType, 
					new NameE("Role PortType",english),
					new NameE("Element PortType", deutsch))
			);
			roles.put(WSDLRoles.operation, this.createRole(NS_WSDL2TM+"Operation-Role", roleType, 
					new NameE("Role Operation",english),
					new NameE("Element Operation", deutsch))
			);
			roles.put(WSDLRoles.operation_input, this.createRole(NS_WSDL2TM+"OperationInput-Role", roleType, 
					new NameE("Role Operation Input", english),
					new NameE("Element Operation Input", deutsch))
			);
			roles.put(WSDLRoles.operation_output, this.createRole(NS_WSDL2TM+"OperationOutput-Role", roleType, 
					new NameE("Role Operation Output", english),
					new NameE("Element Operation Output", deutsch))
			);
			roles.put(WSDLRoles.operation_fault, this.createRole(NS_WSDL2TM+"OperationFault-Role", roleType, 
					new NameE("Role Operation Fault", english),
					new NameE("Element Operation Fault", deutsch))
			);
			roles.put(WSDLRoles.message, this.createRole(NS_WSDL2TM+"Message-Role", roleType, 
					new NameE("Role Message", english),
					new NameE("Element Message", deutsch))
			);
			roles.put(WSDLRoles.part, this.createRole(NS_WSDL2TM+"MessagePart-Role", roleType, 
					new NameE("Role Message Part", english),
					new NameE("Element Message Part", deutsch))
			);
			roles.put(WSDLRoles.types, this.createRole(NS_WSDL2TM+"Types-Role", roleType, 
					new NameE("Role Types", english),
					new NameE("Element Types", deutsch))
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
				this.associateTopics(s);
			}
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
							topicRoles.get(WSDLRoles.bindingop),
							WSDLAssociation.relation_binding_operation);
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
						topicRoles.get(WSDLRoles.bindingop), 
						topicRoles.get(WSDLRoles.bindingop_input), 
						WSDLAssociation.relation_bindingoperation_input);
				this.associateTopics(op, op.getBindingOutput(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.bindingop_output), 
						topicRoles.get(WSDLRoles.bindingop), 
						topicRoles.get(WSDLRoles.bindingop_output), 
						WSDLAssociation.relation_bindingoperation_output);
				Iterator<BindingFault> bin_fau = op.getBindingFaults().values().iterator();
				while (bin_fau.hasNext()) {
					this.associateTopics(op, bin_fau.next(),
							topicTypes.get(WSDLTopic.operation), 
							topicTypes.get(WSDLTopic.bindingop_fault), 
							topicRoles.get(WSDLRoles.bindingop), 
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
							WSDLAssociation.relation_porttype_operation);
					this.associateTopics(op);
				}
			} else if (Operation.class.isInstance(a)) {
				Operation op = (Operation) a;
				this.associateTopics(op, op.getInput(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.operation_input), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.operation_input), 
						WSDLAssociation.relation_operation_input);
				this.associateTopics(op, op.getOutput(),							
						topicTypes.get(WSDLTopic.operation), 
						topicTypes.get(WSDLTopic.operation_output), 
						topicRoles.get(WSDLRoles.operation), 
						topicRoles.get(WSDLRoles.operation_output), 
						WSDLAssociation.relation_operation_output);
				Iterator<Fault> it = op.getFaults().values().iterator();
				while (it.hasNext()) {
					this.associateTopics(op, it.next(),
							topicTypes.get(WSDLTopic.operation), 
							topicTypes.get(WSDLTopic.operation_fault), 
							topicRoles.get(WSDLRoles.operation), 
							topicRoles.get(WSDLRoles.operation_fault), 
							WSDLAssociation.relation_operation_fault);					
				}
				if (op.getInput() != null) {
					this.associateTopics(op.getInput());
				}
				if (op.getOutput() != null) {
					this.associateTopics(op.getOutput());
				}
			} else if (Input.class.isInstance(a)) {
				Input i = (Input) a;
				if (i.getMessage() != null) {
					this.associateTopics(i, i.getMessage(),
							topicTypes.get(WSDLTopic.operation_input), 
							topicTypes.get(WSDLTopic.message),
							topicRoles.get(WSDLRoles.operation_input),
							topicRoles.get(WSDLRoles.message),
							WSDLAssociation.relation_input_message);
					this.associateTopics(i.getMessage());					
				}
			} else if (Output.class.isInstance(a)) {
				Output o = (Output) a;
				if (o.getMessage() != null) {
					this.associateTopics(o, o.getMessage(),
							topicTypes.get(WSDLTopic.operation_output), 
							topicTypes.get(WSDLTopic.message),
							topicRoles.get(WSDLRoles.operation_output),
							topicRoles.get(WSDLRoles.message),
							WSDLAssociation.relation_output_message);
					this.associateTopics(o.getMessage());					
				}
			} else if (Fault.class.isInstance(a)) {
				Fault f = (Fault) a;
				if (f.getMessage() != null) {
					this.associateTopics(f, f.getMessage(),
							topicTypes.get(WSDLTopic.operation_fault), 
							topicTypes.get(WSDLTopic.message),
							topicRoles.get(WSDLRoles.operation_fault),
							topicRoles.get(WSDLRoles.message),
							WSDLAssociation.relation_fault_message);
					this.associateTopics(f.getMessage());					
				}
				
			} else if (Message.class.isInstance(a)) {
				Iterator<Part> it = ((Message) a).getParts().values().iterator();
				while (it.hasNext()) {
					Part part = it.next();
					this.associateTopics(a, part,
							topicTypes.get(WSDLTopic.message), 
							topicTypes.get(WSDLTopic.part),
							topicRoles.get(WSDLRoles.message),
							topicRoles.get(WSDLRoles.part),
							WSDLAssociation.relation_message_part);
					this.associateTopics(part);
				}
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
		private void associateTopics(Object a, Object b, Topic typea, Topic typeb, Topic ra, Topic rb, WSDLAssociation choose) {
			
			
			TopicE t;
			Topic temp = tm.createTopic();
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
						if (par.getElementName() != null) {
							// simple type (xsd or tns goal)
						} else if (par.getTypeName() != null) {
							// complex type (xsd or tns goal)
						}
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
				
				this.createAssociation(ascs.get(choose), (ITopic) ta, ra, (ITopic) tb, rb);
				
			}
	
		}

		private Association createAssociation(Topic ascType, ITopic topic_a, Topic role_a, ITopic topic_b, Topic role_b) {
			
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
			while (it.hasNext()) {
				ExtensibilityElement e = it.next();
				ns = e.getElementType().getNamespaceURI();
				lp = e.getElementType().getLocalPart();
				if ("http://schemas.xmlsoap.org/wsdl/soap/address".equals(ns+lp)) {
					SOAPAddress soa = (SOAPAddress) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+ "LocationURI")), soa.getLocationURI());
					occ.getType().createName("Location of Web Service",english);
				} else if ("http://schemas.xmlsoap.org/wsdl/soap/operation".equals(ns+lp)) {
					SOAPOperation soa = (SOAPOperation) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+"ActionURI")),soa.getSoapActionURI());
					occ.getType().createName("Action Address", english);
				} else if ("http://schemas.xmlsoap.org/wsdl/soap/binding".equals(ns+lp)) {
					SOAPBinding soa = (SOAPBinding) e;
					Occurrence occ = topic.createOccurrence(tm.createTopicBySubjectIdentifier(tm.createLocator(NS_WSDL2TM+ "TransportProtocol")), soa.getTransportURI());
					occ.getType().createName("Transport Protocol", english);
				} else if ("http://schemas.xmlsoap.org/wsdl/soap/body".equals(ns+lp)) {
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
