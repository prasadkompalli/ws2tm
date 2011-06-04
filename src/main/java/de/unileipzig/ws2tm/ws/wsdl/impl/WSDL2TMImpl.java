/**
 * 
 */
package de.unileipzig.ws2tm.ws.wsdl.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Role;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

import de.topicmapslab.majortom.model.core.ITopic;
import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.tm.util.MyTopicMapSystem;
import de.unileipzig.ws2tm.tm.util.NameE;
import de.unileipzig.ws2tm.tm.util.TopicE;
import de.unileipzig.ws2tm.tm.util.MyTopicMapSystem.IDs;
import de.unileipzig.ws2tm.ws.soap.Message;
import de.unileipzig.ws2tm.ws.xsd.SchemaParser;
import de.unileipzig.ws2tm.ws.xsd.Type;

public class WSDL2TMImpl implements TopicMapAccessObject {
	
	private static Logger log = Logger.getLogger(WSDL2TMImpl.class);
	
	private MyTopicMapSystem tms;
	private String tns;
	private ITopic deutsch;
	private ITopic english;
	
	private HashMap<String, URL> namespaces;
	
	private HashMap<WSDLAssociation, ITopic> ascs;
	private HashMap<WSDLTopic, ITopic> topicTypes;
	private HashMap<WSDLRoles, ITopic> topicRoles;
	private String notdefined = "Not defined";
	private ITopic dataType;
	
	private WSDL2TMImpl(Definition wsdl) throws InitializationException,MalformedIRIException, FactoryConfigurationException,IllegalArgumentException, IOException {
		this(wsdl, WebService2TopicMapFactory.NS_WSDL2TM+ new Random().nextLong());
	}

	private WSDL2TMImpl(Definition wsdl, String namespaceURI) throws InitializationException, MalformedIRIException,FactoryConfigurationException, IllegalArgumentException,IOException {
		this(wsdl, TopicMapEngine.newInstance().createNewTopicMapInstance(null, namespaceURI));
	}

	public WSDL2TMImpl(Definition wsdl, TopicMap tm) throws InitializationException {
		if (wsdl == null || tm == null) {
			throw new InitializationException("The parameters need to be initialized. Otherwise the web service description cannot be transformed to a topic map.");
		}
		
		log.debug("Created new instance of class "+WSDL2TMImpl.class.getCanonicalName());
		
		this.tms = new MyTopicMapSystem(tm);
		this.tns = wsdl.getTargetNamespace();
		if (!tns.endsWith("/")) {
			tns = tns+"/";
		}
		log.info("Using web service description "+wsdl.getDocumentBaseURI());
		
		ITopic language = tms.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/", IDs.ItemIdentifier).getTopic();
		deutsch = tms.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/deutsch", IDs.ItemIdentifier).getTopic();
		deutsch.createName("deutsch");
		deutsch.addType(language);
		
		english = tms.createTopic("http://code.topicmapslab.de/grigull-tm2speech/Language/english", IDs.ItemIdentifier).getTopic();
		english.createName("english");
		english.addType(language);
		
		dataType = tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"WS/Datatype", IDs.SubjectIdentifier).getTopic();
		dataType.createName("datatype");
		
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
		Map<String,String> map = wsdl.getNamespaces();
		for (Map.Entry<String, String> e : map.entrySet()) {
			if (e.getKey().startsWith("urn:") || e.getKey().startsWith("URN:")) {
				//BROKENWINDOW namespaces without an unprefixed element type name ("urn") are probably not recognized, and will cause a lot of ugly errors
			}
			this.addNameSpace(e.getKey().toLowerCase(), e.getValue());
		}
		this.addNameSpace("tns", this.tns);
		
		Iterator<ExtensibilityElement> it = wsdl.getTypes().getExtensibilityElements().iterator();
		while (it.hasNext()) {
			ExtensibilityElement e = it.next();
			QName name = e.getElementType();
			// if you find schemas inside the web service description...
			if (name.getNamespaceURI().equalsIgnoreCase("http://www.w3.org/2001/XMLSchema") && name.getLocalPart().equalsIgnoreCase("schema")) {
				Schema s = (Schema) e;
				SchemaParser.getFactory().addSchema(s, s.getElement().getNamespaceURI());
			}
		}
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
	private HashMap<WSDLAssociation, ITopic> createAssociations() {
		HashMap<WSDLAssociation, ITopic> ascs = new HashMap<WSDLAssociation, ITopic>();
		ascs.put(WSDLAssociation.relation_service_port, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_service_port.name()),
						new NameE("Relation Service Port"),
						new NameE("Serviceschnittstellen", deutsch)
				));
		ascs.put(WSDLAssociation.relation_port_binding, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_port_binding),
						new NameE("Relation Port Binding"),
						new NameE("Schnittstellenbindungen", deutsch)
				));
		ascs.put(WSDLAssociation.relation_binding_porttype, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_binding_porttype),
						new NameE("Relation Binding PortType"),
						new NameE("PortType-Bindungen", deutsch)
				));
		ascs.put(WSDLAssociation.relation_service_operation,
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_service_operation), 
						new NameE("Service Operations"),
						new NameE("Service-Funktionen", deutsch)
				));
		ascs.put(WSDLAssociation.relation_binding_operation, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_binding_operation.name()),
						new NameE("Relation Binding BindingOperation"),
						new NameE("Service Operationen",deutsch)
				));
		ascs.put(WSDLAssociation.relation_bindingoperation_input, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_input), 
						new NameE("Relation BindingOperation Input")
				));
		ascs.put(WSDLAssociation.relation_bindingoperation_output, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_output), 
						new NameE("Relation BindingOperation Output")
				));
		ascs.put(WSDLAssociation.relation_bindingoperation_fault, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_fault), 
						new NameE("Relation BindingOperation Fault")
				));
		ascs.put(WSDLAssociation.relation_bindingoperation_message, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_bindingoperation_message.name()), 
						new NameE("Relation BindingOperation Message")
				));
		ascs.put(WSDLAssociation.relation_porttype_operation, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_porttype_operation),
						new NameE("Relation PortType Operation")
				));
		ascs.put(WSDLAssociation.relation_operation_input, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_operation_input.name()),
						new NameE("Relation PortType Operation Input")
				));
		ascs.put(WSDLAssociation.relation_operation_output, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_operation_output.name()),
						new NameE("Relation PortType Operation Output")
				));
		ascs.put(WSDLAssociation.relation_operation_fault, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_operation_fault.name()),
						new NameE("Relation PortType Operation Fault")
				));
		ascs.put(WSDLAssociation.relation_operation_message, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_operation_message.name()), 
						new NameE("Relation Operation Message")
				));
		ascs.put(WSDLAssociation.relation_input_message, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_input_message.name()),
						new NameE("Relation Input Message")
				));
		ascs.put(WSDLAssociation.relation_output_message, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_output_message.name()),
						new NameE("Relation Output Message")
				));
		ascs.put(WSDLAssociation.relation_message_part, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_message_part.name()),
						new NameE("Relation Message Part", english)
				));
		ascs.put(WSDLAssociation.relation_part_types, 
				this.createAssociation(tms.getTopicMap().createLocator(WebService2TopicMapFactory.NS_WSDL2TM+WSDLAssociation.relation_part_types.name()),
						new NameE("Relation Part Types", english),
						new NameE("Abstrakte Typendefinition", deutsch)
				));
		return ascs;
	}

	/**
	 * This method creates association types or returns an already existing topic, if the locator
	 * already exists. The association types are grouped as {@link IDs#SubjectIdentifier}. The 
	 * association topics will get the same item identifier {@link IDs#ItemIdentifier} linked to
	 * {@link WebService2TopicMapFactory#NS_WSDL2TM}/sub_category_association_type, and those
	 * are typed via http://psi.topicmaps.org/tmcl/topic-type.
	 * 
	 * @param locator - the address identifying the association
	 * @param names - one or more instances of class {@link NameE}, which links names (strings) and
	 * the scopes where the name should be used (which are topics).
	 * @return the created association as instance of class {@link Topic}
	 */
	private ITopic createAssociation(Locator locator, NameE... names) {
		ITopic ascType = tms.createTopic(locator, IDs.SubjectIdentifier).getTopic();
		ascType.addType(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+ "sub_category_association_type", IDs.ItemIdentifier).getTopic());
		ascType.getTypes().iterator().next().addType(tms.createTopic("http://psi.topicmaps.org/tmcl/topic-type",IDs.ItemIdentifier).getTopic());
		for (NameE name : names) {
			ascType.createName(name.getName(), name.getScopes());
		}
		return ascType;
	}

	/**
	 * This method creates the topic types abstracting the WSDL main elements, their sub elements
	 * and other attributes, which are required for mapping information to these types.
	 * 
	 * @return a map where the keys point to {@link WSDLTopic}, and the value is the topic type linked to the key
	 */
	private HashMap<WSDLTopic, ITopic> createTopicTypes() {
		HashMap<WSDLTopic, ITopic> topics = new HashMap<WSDLTopic, ITopic>();
		ITopic topicType = tms.createTopic("http://psi.topicmaps.org/tmcl/topic-type", IDs.ItemIdentifier).getTopic();
		
		ITopic service = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Service", IDs.SubjectIdentifier).getTopic();
		service.addType(topicType);
		service.createName("Topic type service");
		ITopic port = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Port", IDs.SubjectIdentifier).getTopic();
		port.addType(topicType);
		service.createName("Topic type service port");
		ITopic binding = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Binding", IDs.SubjectIdentifier).getTopic();
		binding.addType(topicType);
		service.createName("Topic type binding");
		ITopic bindingop = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingOperation", IDs.SubjectIdentifier).getTopic();
		bindingop.addType(topicType);
		service.createName("Topic type binding operation");
		ITopic porttype = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"PortType", IDs.SubjectIdentifier).getTopic();
		porttype.addType(topicType);
		service.createName("Topic type porttype");
		ITopic operation = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Operation", IDs.SubjectIdentifier).getTopic();
		operation.addType(topicType);
		service.createName("Topic type operation");
		ITopic bindinginput = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingInput", IDs.SubjectIdentifier).getTopic();
		bindinginput.addType(topicType);
		service.createName("Topic type binding input");
		ITopic bindingoutput = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingOutput", IDs.SubjectIdentifier).getTopic();
		bindingoutput.addType(topicType);
		service.createName("Topic type binding output");
		ITopic bindingfault = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingFault", IDs.SubjectIdentifier).getTopic();
		bindingfault.addType(topicType);
		service.createName("Topic type binding fault");
		ITopic input = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Input", IDs.SubjectIdentifier).getTopic();
		input.addType(topicType);
		service.createName("Topic type operation input");
		ITopic output = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Output", IDs.SubjectIdentifier).getTopic();
		output.addType(topicType);
		service.createName("Topic type operation output");
		ITopic fault = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Fault", IDs.SubjectIdentifier).getTopic();
		fault.addType(topicType);
		service.createName("Topic type operation fault");
		ITopic message = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Message", IDs.SubjectIdentifier).getTopic();
		message.addType(topicType);
		service.createName("Topic type message");
		ITopic part = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Part", IDs.SubjectIdentifier).getTopic();
		part.addType(topicType);
		service.createName("Topic type part");
		ITopic types = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Types", IDs.SubjectIdentifier).getTopic();
		types.addType(topicType);	
		service.createName("Topic type types");
		
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
	
	/**
	 * This method creates a map with a number of role types. These roles types are
	 * an optional and still somehow required part of linking topics via associations
	 * or using occurrences and their values in a defined scope.
	 * 
	 * @return a map with the keys of class {@link WSDLRoles}, and the role types, defined
	 * in this method
	 */
	private HashMap<WSDLRoles, ITopic> createRoles() {
		HashMap<WSDLRoles, ITopic> roles = new HashMap<WSDLRoles, ITopic>();
		Topic roleType = tms.createTopic("http://psi.topicmaps.org/tmcl/role-type", IDs.ItemIdentifier).getTopic();
		
		roles.put(WSDLRoles.service, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"Service-Role", roleType, 
				new NameE("Service",english),
				new NameE("Service", deutsch))
		);
		roles.put(WSDLRoles.port, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"ServicePort-Role", roleType, 
				new NameE("Service-Port",english),
				new NameE("Service-Port", deutsch))
		);
		roles.put(WSDLRoles.binding, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"Binding-Role", roleType, 
				new NameE("Binding",english),
				new NameE("Binding", deutsch))
		);
		roles.put(WSDLRoles.bindingop, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"BindingOperation-Role", roleType, 
				new NameE("Binding Operation",english),
				new NameE("Binding Funktion", deutsch))
		);
		roles.put(WSDLRoles.bindingop_input, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"BindingInput-Role", roleType, 
				new NameE("Input", english),
				new NameE("Eingabe", deutsch))
		);
		roles.put(WSDLRoles.bindingop_output, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"BindingOutput-Role", roleType, 
				new NameE("Output", english),
				new NameE("Ausgabe", deutsch))
		);
		roles.put(WSDLRoles.bindingop_fault, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"BindingFault-Role", roleType, 
				new NameE("Binding Operation Fault", english),
				new NameE("Binding Funktionsfehler", deutsch))
		);
		roles.put(WSDLRoles.porttype, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"PortType-Role", roleType, 
				new NameE("PortType",english),
				new NameE("PortType", deutsch))
		);
		roles.put(WSDLRoles.operation, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"Operation-Role", roleType, 
				new NameE("Operation",english),
				new NameE("Funktion", deutsch))
		);
		roles.put(WSDLRoles.operation_input, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"OperationInput-Role", roleType, 
				new NameE("Input", english),
				new NameE("Eingabe", deutsch))
		);
		roles.put(WSDLRoles.operation_output, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"OperationOutput-Role", roleType, 
				new NameE("Output", english),
				new NameE("Ausgabe", deutsch))
		);
		roles.put(WSDLRoles.operation_fault, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"OperationFault-Role", roleType, 
				new NameE("Operation Fault", english),
				new NameE("Funktionsfehler", deutsch))
		);
		roles.put(WSDLRoles.message, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"Message-Role", roleType, 
				new NameE("Message", english),
				new NameE("Nachricht", deutsch))
		);
		roles.put(WSDLRoles.part, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"MessagePart-Role", roleType, 
				new NameE("Message Part", english),
				new NameE("Nachrichtenparameter", deutsch))
		);
		roles.put(WSDLRoles.types, tms.createRole(WebService2TopicMapFactory.NS_WSDL2TM+"Types-Role", roleType, 
				new NameE("Type Definition", english),
				new NameE("Typendefinition", deutsch))
		);
		
		return roles;
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
			 * Occurrences can be retrieved fast now.
			 */
			this.initiateServiceToTopicMap(s, wsdl.getTypes());
//			this.associateTopics(s);
		}
	}
	
	
	/**
	 * This method 
	 * @param s
	 * @param t
	 */
	@SuppressWarnings("unchecked")
	private void initiateServiceToTopicMap(Service s, Types t) {
		
		TopicE serviceE = tms.createTopic(s.getQName(), IDs.ItemIdentifier);
		if (serviceE.exists()) {
			return;
		}
		
		// First: get every service
		ITopic service = serviceE.getTopic();
		service.addType(topicTypes.get(WSDLTopic.service));
		
		// Second: Iterate through all found ports of a service element
		Iterator<Port> it_ports = s.getPorts().values().iterator();
		while (it_ports.hasNext()) {
			Port port = it_ports.next();
			// Third: get elements, which extend the current port element, and add them to the current service
			this.addOccurrences(service, port.getExtensibilityElements().iterator());
			// Third: add the elements, which extend the binding linked to the current port with the current service
			this.addOccurrences(service, port.getBinding().getExtensibilityElements().iterator());
			// Third: add the elements, which extend the porttype, which is linked the to binding of the current port, and add those information to the current service
			this.addOccurrences(service, port.getBinding().getPortType().getExtensibilityElements().iterator());
			
			HashMap<String, BindingOperation> map_binop = new HashMap<String, BindingOperation>();
			Iterator<BindingOperation> it_binop = port.getBinding().getBindingOperations().iterator();
			// Fourth: a temporary hash map is created and filled with information of all existing binding operations
			while (it_binop.hasNext()) {
				BindingOperation binop = it_binop.next();
				map_binop.put(tns+binop.getName(), binop);
			}
			
			Iterator<Operation> it_op = port.getBinding().getPortType().getOperations().iterator();
			// Fourth: After, every element operation of the current port, their binding, and their porttype, which has all operations linked
			while (it_op.hasNext()) {
				Operation op = it_op.next();
				// those operations will be associated with the current service through a pre-defined connection
				ITopic opT = this.associateTopics(s, op,
						topicTypes.get(WSDLTopic.service), 
						topicTypes.get(WSDLTopic.operation),
						topicRoles.get(WSDLRoles.service),
						topicRoles.get(WSDLRoles.operation),
						WSDLAssociation.relation_service_operation);
				// the binding operations have information of great importance like the kind of technologie for the internet connection and so on
				BindingOperation binop = map_binop.get(tns+ op.getName());
				// these information are added to the current operation, which is associated with the current service. Therefore both information of binding operation and normal operation are linked in one topic
				this.addOccurrences(opT, binop.getExtensibilityElements().iterator());
				
				/*
				 * Sixth: in the following three steps the input, output and error messages will be associated with the current operation
				 * However, another function is used than the previous calls to #associateTopics(Object, Object, Topic, Topic, Topic, Topic, WSDLAssociation)
				 * The here used associate Topics concentrates on the message and the operation, which is associated with it.
				 */
				this.associateTopics(op, op.getInput().getMessage(), topicRoles.get(WSDLRoles.operation_input), binop.getBindingInput().getExtensibilityElements().iterator());
				this.associateTopics(op, op.getOutput().getMessage(), topicRoles.get(WSDLRoles.operation_output), binop.getBindingOutput().getExtensibilityElements().iterator());
				
				Iterator<Fault> it = op.getFaults().values().iterator();
				while (it.hasNext()) {
					Fault fault = it.next();
					this.associateTopics(op, fault.getMessage(), topicRoles.get(WSDLRoles.operation_fault), binop.getBindingFault(fault.getName()).getExtensibilityElements().iterator());
				}
				
				
			}
			
		}
	}
	
	/**
	 * This functions differs from function {@link #associateTopics(Object, Object, Topic, Topic, Topic, Topic, WSDLAssociation)}. This function
	 * uses concrete objects, which should be instances of classes {@link Operation} and {@link Message} of the WSDL norm. While
	 * the topic types are already defined, the role types can change depending if the message concentrates on the {@link Input}, {@link Output}
	 * or an error ({@link Fault}).
	 * 
	 * @param op - instance of class {@link Operation}
	 * @param msg - instance of class {@link Message}
	 * @param type - the role type of the instance of class {@link Message}. E.g. {@link WSDLRoles#operation_input},{@link WSDLRoles#operation_output} or {@link WSDLRoles#operation_fault}
	 * @param it - An iterator for elements, which extend the current message
	 * @return
	 * @see #associateTopics(Object, Object, Topic, Topic, Topic, Topic, WSDLAssociation)
	 */
	@SuppressWarnings("unchecked")
	private Topic associateTopics(Operation op, javax.wsdl.Message msg, ITopic type, Iterator<ExtensibilityElement> it) {
		ITopic m = this.associateTopics(op, msg, 
				topicTypes.get(WSDLTopic.operation), 
				topicTypes.get(WSDLTopic.message), 
				topicRoles.get(WSDLRoles.operation), 
				type, 
				WSDLAssociation.relation_operation_message);
		
		this.addOccurrences(m, it);
		
		Iterator<Part> it_part = msg.getParts().values().iterator();
		while (it_part.hasNext()) {
			Part part = it_part.next();
			QName qname = new QName(tns, part.getName());
			QName Qele = null, Qtyp = null;
			String ele = null;
			Type xsType = null;
			if (part.getTypeName() != null) {
				//TODO add here the xsd schema topic transformation
				/*
				 * SchemaParser.getType(part.getTypeName())
				 * if Type: part.getName();
				 */
				// complex or simple type of xsd -> Occurrence
				ele = part.getName();
				Qele = new QName(tns,ele);
				Qtyp = part.getTypeName();
				try {
					xsType = SchemaParser.getType(Qtyp);
				} catch (IllegalArgumentException e) {
					log.warn("While accessing instance of type "+Qtyp+" with part name "+ele+" an IllegalArgumentException was catched.",e);
				} catch (IOException e) {
					log.warn("While accessing instance of type "+Qtyp+" with part name "+ele+" an IOException was catched.",e);
				}
			} else {
				// element name of xsd -> Association to next Element (macht wenig Sinn, denn ComplexType kann ArrayOfFloat sein
				/*
				 * SchemaParser.getElement(part.getElementName())
				 * if Element: element.getQName();, element.getType();
				 */
				Qele = part.getElementName();
				ele = Qele.getLocalPart();
				try {
					xsType = SchemaParser.getElement(Qele).getType();
					Qtyp = xsType.getQName();
				} catch (IllegalArgumentException e) {
					log.warn("While accessing instance of type of element "+Qele.toString()+" an IllegalArgumentException was catched.",e);
				} catch (IOException e) {
					log.warn("While accessing instance of type of element "+Qele.toString()+" an IOException was catched.",e);
				}
			}
			
			
			TopicMap tm = tms.getTopicMap();
			Association asc = tm.createAssociation(ascs.get(WSDLAssociation.relation_message_part));
			
			ITopic para = tms.createTopic(Qele, IDs.SubjectIdentifier).getTopic();
			para.createName(ele);
			para.addType(this.topicTypes.get(WSDLTopic.part));
			
			Topic tdt = tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"ws/datatype", IDs.SubjectIdentifier).getTopic();
			
			log.debug("Adding element name "+ele);
			
			if (xsType != null) {
				ITopic Ttyp = this.transformXSTyp2Topic(xsType);
				para.createOccurrence(tdt,ele, Ttyp);
			} else {
				para.createOccurrence(tdt,ele);				
			}
			asc.createRole(topicRoles.get(WSDLRoles.message), m);
			asc.createRole(topicRoles.get(WSDLRoles.part), para);			
		}
		
		return m;
	}

	/**
	 * @param qtyp
	 * @return
	 */
	private ITopic transformXSTyp2Topic(Type type) {
		ITopic t = tms.createTopic((String) null, IDs.ItemIdentifier).getTopic();
		t.addSupertype(dataType);
		t.addType(tms.createTopic(type.getQName(), IDs.ItemIdentifier).getTopic());
		
		
		ITopic aSetting = tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"tm/asc/datatype/has-setting", IDs.SubjectIdentifier).getTopic();
		ITopic Tsetting = tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"ws/datatype/setting", IDs.SubjectIdentifier).getTopic();
		ITopic setting = tms.createTopic((String) null, IDs.ItemIdentifier).getTopic();		
		setting.addType(Tsetting);
		
		//TODO the created topic about a concret topic type needs to be analyzed and transformed further to a topic structure. 
		/*
		ITopic tboolean = tms.createTopic(new QName(WebService2TopicMapFactory.NS_XSD,"boolean"), IDs.SubjectIdentifier).getTopic();
		
		
		Association a = tms.getTopicMap().createAssociation(aSetting);
		a.createRole(dataType, t);
		a.createRole(Tsetting, setting);
		
		if (type.hasChildren()) {
			for (ListElement e : type.getListElements()) {
				switch (e.getListElementType()) {
				case Type.ALL:
				case Type.CHOICE:
				case Type.SEQUENCE:
				case Type.ELEMENT: 
				}
			}
		}
		
		*/
		
		return t;
		
	}

	private void associateTopics(Object a) {
		if (Service.class.isInstance(a)) {
			Iterator<Port> it = ((Service) a).getPorts().values().iterator();
			while (it.hasNext()) {
				Port port = it.next();
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
		} else if (javax.wsdl.Message.class.isInstance(a)) {
			Iterator<Part> it = ((javax.wsdl.Message) a).getParts().values().iterator();
			while (it.hasNext()) {
				Part part = it.next();
				TopicE dataE = null;
				if (part.getTypeName() != null) {
					 dataE = tms.createTopic(part.getTypeName(), IDs.SubjectIdentifier);
				} else {
					dataE = tms.createTopic(part.getElementName(), IDs.SubjectIdentifier);
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
	private ITopic associateTopics(Object a, Object b, ITopic typea, ITopic typeb, ITopic ra, ITopic rb, WSDLAssociation choose) {
		
		TopicE t;
		ITopic temp = null;
		ITopic ta = null;
		ITopic tb = null;
		
		Object[] objs = new Object[]{a,b};
		int i = 0;
		for (Object obj : objs) {
			if (Service.class.isInstance(obj)) { 
				Service ser = (Service) obj;
				t = tms.createTopic(ser.getQName(), IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(ser.getQName().getLocalPart());
					this.addOccurrences(temp, ser.getExtensibilityElements().iterator());
				}
			} else if (Port.class.isInstance(obj)) { 
				Port por = (Port) obj;
				t = tms.createTopic(tns+ por.getName());
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(por.getName());
					this.addOccurrences(temp, por.getExtensibilityElements().iterator());						
				}
			} else if (Binding.class.isInstance(obj)) { 
				Binding bin = (Binding) obj;
				t = tms.createTopic(bin.getQName(), IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(bin.getQName().getLocalPart());
					this.addOccurrences(temp, bin.getExtensibilityElements().iterator());
				}
			} else if (BindingOperation.class.isInstance(obj)) {
				BindingOperation binop = (BindingOperation) obj;
				t = tms.createTopic(tns+ binop.getName(), IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(binop.getName());
					this.addOccurrences(temp, binop.getExtensibilityElements().iterator());						
				}
			} else if (BindingInput.class.isInstance(obj)) {
				BindingInput binin = (BindingInput) obj;
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingInput", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Input");
					this.addOccurrences(temp, binin.getExtensibilityElements().iterator());
				}
			} else if (BindingOutput.class.isInstance(obj)) {
				BindingOutput binou = (BindingOutput) obj;
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingOutput", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Output");
					this.addOccurrences(temp, binou.getExtensibilityElements().iterator());
				}
			} else if (BindingFault.class.isInstance(obj)) { 
				BindingFault binfa = (BindingFault) obj;
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"BindingFault", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Fault");
					this.addOccurrences(temp, binfa.getExtensibilityElements().iterator());
				}
			} else if (PortType.class.isInstance(obj)) {
				PortType pot = (PortType) obj;
				t = tms.createTopic(pot.getQName(), IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(pot.getQName().getLocalPart());
				}
			} else if (Operation.class.isInstance(obj)) { 
				Operation ope = (Operation) obj;
				t = tms.createTopic(tns+ope.getName(), IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(ope.getName());
					this.addOccurrences(temp, ope.getExtensibilityElements().iterator());
				}
			} else if (Input.class.isInstance(obj)) {
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Input", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Input");
				}
			} else if (Output.class.isInstance(obj)) {
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Output", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Output");
				}
			} else if (Fault.class.isInstance(obj)) {
				t = tms.createTopic(WebService2TopicMapFactory.NS_WSDL+"Fault", IDs.SubjectIdentifier);
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName("Fault");
				}
			} else if (javax.wsdl.Message.class.isInstance(obj)) {
				javax.wsdl.Message mes = (javax.wsdl.Message) obj;
				t = tms.createTopic(mes.getQName());
				temp = t.getTopic();
				if (!t.exists()) {
					temp.createName(mes.getQName().getLocalPart());
					this.addOccurrences(temp,mes.getExtensibilityElements().iterator());
				}
			} else if (Part.class.isInstance(obj)) {
				Part par = (Part) obj;
				t = tms.createTopic(tns+par.getName(), IDs.ItemIdentifier);
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

	private Association createAssociation(ITopic ascType, ITopic topic_a, ITopic role_a, ITopic topic_b, ITopic role_b) {
		
		for (Association asc : this.getAssociations()) {
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
						String nta = MyTopicMapSystem.getInfo(topic_a);
						String ntb = MyTopicMapSystem.getInfo(topic_b);
						String nra = MyTopicMapSystem.getInfo(role_a);
						String nrb = MyTopicMapSystem.getInfo(role_b);
						
						log.debug("Association exists already between topic "+nta+" ("+nra+") with topic "+ntb+" ("+nrb+") via assocation "+MyTopicMapSystem.getInfo(ascType));
					}	
					return asc;
				}
			}
		}
		
		Association asc = tms.getTopicMap().createAssociation(ascType);
		asc.createRole(role_a, topic_a);
		asc.createRole(role_b, topic_b);	
		
		if (log.isDebugEnabled()) {
			String nta = MyTopicMapSystem.getInfo(topic_a);
			String ntb = MyTopicMapSystem.getInfo(topic_b);
			String nra = MyTopicMapSystem.getInfo(role_a);
			String nrb = MyTopicMapSystem.getInfo(role_b);
			
			log.debug("Associate topic "+nta+" ("+nra+") with topic "+ntb+" ("+nrb+") via assocation "+MyTopicMapSystem.getInfo(ascType));
		}	
		
		return asc;
	}

	private void addOccurrences(ITopic topic, Iterator<ExtensibilityElement> it) {
		String ns, lp;
		String NS_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
		String NS_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
		while (it.hasNext()) {
			ExtensibilityElement e = it.next();
			ns = e.getElementType().getNamespaceURI();
			lp = e.getElementType().getLocalPart();
			if ((NS_SOAP+"address").equals(ns+lp)) {
				SOAPAddress soa = (SOAPAddress) e;
				Occurrence occ = topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+ "LocationURI",IDs.SubjectIdentifier).getTopic(), soa.getLocationURI());
				occ.getType().createName("Location of Web Service",english);
			} else if ((NS_SOAP12+"address").equals(ns+lp)) {
				e.getElementType();
				SOAP12Address soa = (SOAP12Address) e;
				Occurrence occ = topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+ "LocationURI", IDs.SubjectIdentifier).getTopic(), soa.getLocationURI());
				occ.getType().createName("Location of Web Service",english);
			} else if ((NS_SOAP+"operation").equals(ns+lp)) {
				SOAPOperation soa = (SOAPOperation) e;
				Occurrence occ = topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"ActionURI", IDs.SubjectIdentifier).getTopic(), soa.getSoapActionURI());
				occ.getType().createName("Action Address", english);
			} else if ((NS_SOAP+"binding").equals(ns+lp)) {
				SOAPBinding soa = (SOAPBinding) e;
				Occurrence occ = topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+ "TransportProtocol",IDs.SubjectIdentifier).getTopic(), soa.getTransportURI());
				occ.getType().createName("Transport Protocol", english);
			} else if ((NS_SOAP+"body").equals(ns+lp)) {
				SOAPBody soa = (SOAPBody) e;
				
				if (soa.getEncodingStyles() != null) {
					// Parts
					// Use
					Iterator<String> encodings = soa.getEncodingStyles().iterator();
					while (encodings.hasNext()) {
						
						//BROKENWINDOW test this
						System.out.println(soa.getElementType() + ": "+ encodings.next());
					}
					if (soa.getUse() != null) {
						Occurrence occ = topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"Usage", IDs.SubjectIdentifier).getTopic(),soa.getUse());
						occ.getType().createName("Encoding", english);
					}
					// encodingStyle
					// namespace
				}
			}
			// soap header
			// soap header -> soap headerfault
			// soap fault
			


		}
	}

	@SuppressWarnings("unchecked")
	private void retrieveImports(Definition wsdl) throws IOException {
		try {
			Iterator<Import> it_import = wsdl.getImports().values().iterator();
			while (it_import.hasNext()) {
				Import i = it_import.next();
				log.debug("Found import statement: will retrieve import of wsdl "+i.getLocationURI());
				if (i.getNamespaceURI() == null) {
					this.tms.getTopicMap().mergeIn(new WSDL2TMImpl(i.getDefinition()).load());
				} else {
					this.tms.getTopicMap().mergeIn(new WSDL2TMImpl(i.getDefinition(), i.getNamespaceURI()).load());
				}
			}
		} catch (Exception e) {
			throw new IOException("Unable to retrieve other web service description via import statements.",e);
		}
	}

	@Override
	public Set<Association> getAssociations() {
		if (this.tms.getTopicMap().getAssociations() != null) {
			return this.tms.getTopicMap().getAssociations();
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
		if (this.tms.getTopicMap().getTopics() != null) {
			return this.tms.getTopicMap().getTopics();
		}
		return Collections.emptySet();
	}

	@Override
	public TopicMap load() {
		return this.tms.getTopicMap();
	}

	@Override
	public void save(TopicMap tm) {
		// NOTHING NEEDS TO BE DONE HERE. This function will not be
		// implemented.
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
	
	
}