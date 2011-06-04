/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.topicmapslab.majortom.model.core.ITopic;
import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.tm.util.MyTopicMapSystem;
import de.unileipzig.ws2tm.tm.util.TopicE;
import de.unileipzig.ws2tm.tm.util.MyTopicMapSystem.IDs;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;
import de.unileipzig.ws2tm.util.WebServiceConnector;
import de.unileipzig.ws2tm.ws.soap.Authentication;
import de.unileipzig.ws2tm.ws.soap.Connection;
import de.unileipzig.ws2tm.ws.soap.Message;
import de.unileipzig.ws2tm.ws.soap.RequestObject;
import de.unileipzig.ws2tm.ws.soap.factory.SOAPEngine;

/**
 * Implementation for transforming SOAP messages to a topic map. It implements interface {@link TopicMapAccessObject}
 * for loading the retrieved data.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2010/12/15)
 * @version 0.2 (2011/01/30)
 * @version 0.3 (2011/02/12)
 *
 */
public class SOAP2TMImpl implements TopicMapAccessObject {

	private static Logger log = Logger.getLogger(SOAP2TMImpl.class);
	
	private static String NS = WebService2TopicMapFactory.NS_SOAP2TM;
	
	private MyTopicMapSystem tms = null;
	
	private URL url;
	
	private Topic tTasc = null;
	
	/**
	 * Constructor of class
	 *
	 * @param url
	 * @throws MalformedIRIException
	 * @throws FactoryConfigurationException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public SOAP2TMImpl(URL url) throws MalformedIRIException, FactoryConfigurationException, IllegalArgumentException {
		this.tms = new MyTopicMapSystem(TopicMapEngine.newInstance().createNewTopicMapInstance(new File(WebServiceConfigurator.getFileSOAP2TM()), WebService2TopicMapFactory.NS_SOAP2TM));
		TopicE tE = this.tms.createTopic(NS+"tm/asc/type/has_subelement");
		tTasc = tE.getTopic(); 
		if (!tE.exists()) {
			tTasc.createName("has-subelement");
		}
		this.url = url;
	}

	/**
	 * @param request
	 * @return 
	 * @throws IOException
	 */
	public TopicMap request(RequestObject request) throws IOException {
		if (url == null) {
			throw new IOException("URL is not initialized, and cannot be used. A connection has to established for a soap request.");
		}
		
		WebServiceConnector wsc = WebServiceConnector.newConnection(url);
		SOAPEngine s = SOAPEngine.newInstance(url);
		de.unileipzig.ws2tm.ws.soap.Message m = null;
		try {
			m = s.createMessage(request);
			log.info("Transforming Web Service Request to Topic Map");
			TopicE tE = tms.createTopic(WebService2TopicMapFactory.NS_SOAP+"request", IDs.SubjectIdentifier);
			ITopic tTreq = tE.getTopic();
			if (!tE.exists()) {
				tTreq.createName("Request");
			}

			this.transform(tTreq, m.getRequest());
			/*
			log.info("Sending request to web service");			
			Connection response = wsc.sendRequest(m.getRequest());
			log.info("Creating new message out of existing request and newly received response");			
			m = s.createMessage(m.getRequest(), response);
			*/
		} catch (SOAPException e) {
			throw new IOException("Unable to create soap response. Probably unsuccesful connection to web service, or the response of the web service follows not the soap format standard.",e);
		}
		/*
		if (m != null) {
			try {
				log.info("Start analyzation of request and response.");
				this.analyze(m);
			} catch (SOAPException e) {
				throw new IOException("SOAPException while analyzing and transforming soap request and response to a topic struture.",e);
			}
		}
		*/
		return this.load();
	}

	/**
	 * @param msg
	 * @throws SOAPException 
	 */
	/*
	 * A possible soap request would be something like the following lines
	 * of xml code:
	 * 
	 * <?xml version="1.0" encoding="utf-8" ?> 
	 * <soap:Envelope
	 *  xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
	 *  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 *  xmlns:xsd="http://www.w3.org/2001/XMLSchema"> 
	 *  <soap:Body>
	 * 		<GetCustomerInfo xmlns="http://tempUri.org/">
	 * 			<CustomerID>1</CustomerID> 
	 * 			<OutputParam /> 
	 * 		</GetCustomerInfo>
	 * 	</soap:Body> 
	 * </soap:Envelope>
	 * 
	 * A possible soap response would be something like that: 
	 * <?xml version="1.0" encoding="utf-8" ?> 
	 * <SOAP-ENV:Envelope
	 * 	xmlns:xsd="http://www.w3.org/2001/XMLSchema" ...> 
	 * 	<SOAP-ENV:Body>
	 * 		<method:MethodNameResponse> 
	 * 			<method:MethodNameResult xmlns="" xsi:type="sqlresultstream:SqlResultStream"> 
	 * 				<!-- the results are returned here --> 
	 * 			</method:MethodNameResult>
	 * 			<method:OutputParam>Value</method:OutputParam>
	 * 		</method:MethodNameResponse> 
	 * 	</SOAP-ENV:Body>
	 * </SOAP-ENV:Envelope>
	 */
	private void analyze(Message msg) throws SOAPException {
		
		TopicE tE = tms.createTopic(WebService2TopicMapFactory.NS_SOAP+"request", IDs.SubjectIdentifier);
		ITopic tTreq = tE.getTopic();
		if (!tE.exists()) {
			tTreq.createName("Request");
		}
		tE = tms.createTopic(WebService2TopicMapFactory.NS_SOAP+"response", IDs.SubjectIdentifier);
		ITopic tTres = tE.getTopic();
		if (!tE.exists()) {
			tTres.createName("Response");
		}
		tE = tms.createTopic(WebService2TopicMapFactory.NS_SOAP+"fault", IDs.SubjectIdentifier);
		ITopic tTfau = tE.getTopic();
		if (!tE.exists()) {
			tTfau.createName("Fault");
		}
		tE = tms.createTopic(NS+"tm/asc/type/request-response", IDs.SubjectIdentifier);
		ITopic tTRR = tE.getTopic();
		if (!tE.exists()) {
			tTRR.createName("linkage request response");
		}
		tE = tms.createTopic(NS+"tm/asc/type/request-fault", IDs.SubjectIdentifier);
		ITopic tTRF = tE.getTopic();
		if (!tE.exists()) {
			tTRF.createName("linkage request fault");
		}
		
		ITopic tReq = this.transform(tTreq, msg.getRequest());
		
		if (msg.getErrors().size() > 0) {
			for (SOAPFault f : msg.getErrors()) {
				log.info("Link soap request and soap fault with each other.");
				Association aRF = tms.getTopicMap().createAssociation(tTRF);
				aRF.createRole(tTreq, tReq);
				aRF.createRole(tTfau, this.transform(tTfau, f));
			}
		} else {
			log.info("Link soap request and response with each other.");
			Association aRR = tms.getTopicMap().createAssociation(tTRR);
			aRR.createRole(tTreq, tReq);
			aRR.createRole(tTres, this.transform(tTres, msg.getResponse()));				
		}
		
	}
	
	/**
	 * @param request
	 * @return
	 * @throws SOAPException 
	 */
	private ITopic transform(Topic type, SOAPMessage msg) throws SOAPException {
		
		ITopic topic = tms.createTopic((String) null, IDs.ItemIdentifier).getTopic();
		topic.addSupertype(type);
		
		Iterator<SOAPElement> it = msg.getSOAPBody().getChildElements();
		
		while (it.hasNext()) {
			topic.addType(this.link(topic, it.next()));
		}
		
		return topic;
		
	}

	/**
	 * @param f
	 * @return
	 */
	//TODO add content to transform soap faults (error code, error message, error sender) to topics and associate them somehow
	private ITopic transform(Topic topic, SOAPFault f) {
		return null;
	}

	/**
	 * Linking of soap elements with their attributes and the values of each attribute
	 * 
	 * @param topic
	 * @param e
	 * @param allAttQName
	 */
	//TODO this function should add the content to a topic, and should recall it for every existing child element
	private Topic link(ITopic parent, Node e) {
		
		if (e.getNodeType() != Node.ELEMENT_NODE) {
			return null;
		}
		TopicE tE = tms.createTopic(this.getQName(e), IDs.SubjectIdentifier);
		ITopic type = tE.getTopic();
		if (!tE.exists()) {
			type.createName(e.getLocalName());
		}
		
		tE = tms.createTopic((String) null, IDs.ItemIdentifier);
		ITopic topic = tE.getTopic();
		topic.addType(type);
		
		// Attributes are linked as occurrence for a topic
		NamedNodeMap atts = e.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			// Filtering the attributes. Every attribute allowed except "xmlns" for declaring name spaces.
			if (!att.getLocalName().startsWith("xmlns")) {
				topic.createOccurrence(tms.createTopic(this.getQName(att)).getTopic(),att.getNodeValue());			
			}
		}
		
		if (e.getLocalName() != null) {
			topic.createName(e.getLocalName());
		}
		
		if (e.getTextContent() != null) {
			topic.createOccurrence(tms.createTopic(NS+"occ/type/value").getTopic(), e.getTextContent(),tms.createTopic(new QName(WebService2TopicMapFactory.NS_XSD,"string")).getTopic());
		}
		
		// Subelements are linked as association with a sub-element connection
		// roles: soap2tm:parent and soap2tm:child
		TopicE rPE = tms.createTopic(NS+"asc/role/parent");
		ITopic rP = rPE.getTopic();
		if (!rPE.exists()) {
			rP.createName("parent");
		}
		TopicE rCE = tms.createTopic(NS+"asc/role/child");
		ITopic rC = rCE.getTopic();
		if (!rCE.exists()) {
			rC.createName("child");
		}
		
		Association asc = tms.getTopicMap().createAssociation(tTasc);
		asc.createRole(rP, parent);
		asc.createRole(rC, topic);
		
		if (e.hasChildNodes()) {
			NodeList children = e.getChildNodes();
			for (int i=0; i< children.getLength(); i++) {
				this.link(topic, children.item(i));
			}
		}
		return type;
	}
	
	/**
	 * Function required for creating qualified names with namespace, local name and used prefix in this context.
	 * 
	 * @param n - an instance of class {@link Node}
	 * @return a qualified name, with 
	 */
	private QName getQName(Node n) {
		String prefix = n.getPrefix();
		String ns_t = n.lookupPrefix(prefix);
		String ns = n.getNamespaceURI();
		if (ns_t != null) {
			ns = ns_t;
		} else if (prefix == null) {
			prefix = n.lookupNamespaceURI(ns);			
		}
		
		if (prefix == null) {
			return new QName(ns, n.getLocalName());
		}
		return new QName(ns, n.getLocalName(), prefix);			
	}

	@Override
	public Set<Association> getAssociations() {
		return this.tms.getTopicMap().getAssociations();
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
		return this.tms.getTopicMap().getTopics();
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
}
