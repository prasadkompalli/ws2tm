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

import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.w3c.dom.Node;

import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.tm.util.MyTopicMapSystem;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;
import de.unileipzig.ws2tm.util.WebServiceConnector;
import de.unileipzig.ws2tm.ws.soap.Authentication;
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
		tTasc = this.tms.createTopic(WebService2TopicMapFactory.NS_SOAP2TM+"asc/type/has_subelement").getTopic(); 
		this.url = url;
	}

	/**
	 * @param request
	 * @return 
	 * @throws IOException
	 */
	public TopicMap request(RequestObject request) throws IOException {
		
		WebServiceConnector wsc = WebServiceConnector.newConnection(url);
		SOAPEngine s = SOAPEngine.newInstance(url);
		try {
			de.unileipzig.ws2tm.ws.soap.Message m = s.createMessage(request);
			this.analyze(s.createMessage(m.getRequest(), wsc.sendRequest(m.getRequest())));
			return this.load();
		} catch (SOAPException e) {
			throw new IOException(e);
		}
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
		SOAPMessage request = msg.getRequest();
		SOAPMessage response = msg.getResponse();
		
		Topic treq = this.transform(request);
		
		if (msg.getErrors().size() > 0) {
			for (SOAPFault f : msg.getErrors()) {
				Topic tfau = this.transform(f);
			}
		} else {
			Topic tres = this.transform(response);				
		}
		
		
		request.getMimeHeaders();
		request.getSOAPBody().getChildNodes().item(0);
		request.getSOAPHeader();
		
		/*
		 * Header and Envelope have namespaces existing
		 * 
		 * in SOAP-ENV:body existiert eine XML, wobei SOAP-ENV das root-element darstellt, jedes element in der XML kann einen Namespace definieren.
		 */
		
		Iterator<String> it = request.getSOAPPart().getEnvelope().getNamespacePrefixes();
		String prefix;
		while (it.hasNext()) {
			prefix = it.next();
			request.getSOAPPart().getEnvelope().getNamespaceURI(prefix);
		}
		
		request.getContentDescription();
		
		request.getAttachments();
		// couple request and response with eachother.
		
	}
	
	/**
	 * @param request
	 * @return
	 * @throws SOAPException 
	 */
	private Topic transform(SOAPMessage msg) throws SOAPException {
		
		Iterator<SOAPElement> it = msg.getSOAPBody().getChildElements();
		// the first element of a soap message should be the request operation or response operation
		SOAPElement top = it.next();
		
		Topic tMes = tms.createTopic(top.getElementQName()).getTopic();
		
		this.link(tMes, top, (Iterator<QName>) top.getAllAttributesAsQNames());
		this.link(tMes, top.getTextContent());
		this.link(tMes, (Iterator<Node>) top.getChildElements());
		return tMes;
	}

	/**
	 * @param tMes
	 * @param children
	 */
	private void link(Topic topic, Iterator<Node> children) {
		Association asc = tms.getTopicMap().createAssociation(tTasc);
	}

	/**
	 * Linking soap elements or their topics with a possible text value
	 * @param tMes
	 * @param textContent
	 */
	private void link(Topic topic, String content) {
		if (content != null) {
			topic.createOccurrence(tms.createTopic(WebService2TopicMapFactory.NS_WSDL2TM+"WS/datatype/value").getTopic(), content);
		}			
	}

	/**
	 * Linking of soap elements with their attributes and the values of each attribute
	 * 
	 * @param topic
	 * @param e
	 * @param allAttQName
	 */
	private void link(Topic topic, SOAPElement e, Iterator<QName> allAttQName) {
		if (allAttQName != null) {
			while (allAttQName.hasNext()) {
				QName q = allAttQName.next();
				topic.createOccurrence(tms.createTopic(q).getTopic(),e.getAttributeValue(q));
			}
		}
	}

	/**
	 * @param f
	 * @return
	 */
	private Topic transform(SOAPFault f) {
		return null;
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
