/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.log4j.Logger;


import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;
import de.unileipzig.ws2tm.util.WebServiceConnector;
import de.unileipzig.ws2tm.ws.soap.Connection;
import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.Parameter;
import de.unileipzig.ws2tm.ws.soap.RequestObject;
import de.unileipzig.ws2tm.ws.soap.Message;

//TODO Needs documentation, purpose and so on
/**
 * @author  Torsten Grigull
 * @version  0.1 (2011/01/14)
 */
public class SOAPEngine implements Factory {

	private static Logger log = Logger.getLogger(SOAPEngine.class);
	
	private static HashMap<URL,SOAPEngine> INSTANCES;
	
	private URL url;
	
	/**
	 * HashMap contains all request, which were created using this instance of class {@link SOAPEngine}.
	 * Key: {@link SOAPMessage} (Request)
	 * Value: {@link Message} (Request + Response, including possible Errors).
	 */
	private HashMap<SOAPMessage,Message> requests = null;

	private Operation currentOperation;
	
	private SOAPEngine(URL url) {
		if (requests == null) {
			requests = new HashMap<SOAPMessage, Message>();
		}
		this.url = url;
	}
	
	/**
	 * This method returns a new instance or already initialized instance of class {@link SOAPEngine}. This instance
	 * acts as a factory instance creating soap messages, and combining request and responses depending on the web service.
	 * 
	 * This class is therefore the main access point between the web service and the running web service 2 topic map application.
	 * 
	 * @param url - this valid instance of class {@link URL} defines the access point, to which soap messages should be sent and from which soap messages can be expected, depending on the web service structure and its purpose
	 * 
	 * @return the factory instance of class {@link SOAPEngine}
	 */
	public static SOAPEngine newInstance(URL url) {
		if (INSTANCES == null) {
			INSTANCES = new HashMap<URL,SOAPEngine>();
		}
		if (INSTANCES.containsKey(url)) {
			return INSTANCES.get(url);
		}
		SOAPEngine s = new SOAPEngine(url);
		INSTANCES.put(url, s);
		return s;
	}
	
	/**
	 * @param ro - an instance of class {@link RequestObject}. This class can be also extended to add functionality or to support higher request languages. Currently
	 * the class abstracts only the most required operations to access descriptions of the operations and its parameters. See the class description for further details.
	 * @return an instance of class {@link Message} with an initialized soap request object, which can be send to the described web service
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * 
	 * @see #sendRequest(SOAPMessage)
	 */
	public Message createMessage(RequestObject ro) throws SOAPException {
		MessageImpl msg = new MessageImpl();
		
		SOAPMessage request = msg.getRequest();
		
		for (Operation op : ro.getOperations()) {
			Iterator<Parameter> it = op.getParameters().iterator();
			SOAPBodyElement soa = null;
			if (it.hasNext()) {
				soa = request.getSOAPBody().addBodyElement(op.getQName());				
			}
			
			this.currentOperation = op;
			
			for (Parameter p : op.getParameters()) {
				this.add(soa,p);						
			}
		}
		
		if (requests.containsKey(request)) {
			return requests.get(request);
		}
		if (log.isDebugEnabled()) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				msg.getRequest().writeTo(bout);
				log.debug(bout.toString());
			} catch (IOException e) {
				log.error("Unable to write the created soap message to the log output stream.",e);
			}
		}
		
		requests.put(request, msg);
		return msg;		
	}
	
	/**
	 * @param soa
	 * @param p
	 * @throws SOAPException 
	 */
	private void add(SOAPElement e, Parameter p) throws SOAPException {
		if (p.getNameSpace() == null || p.getNameSpace().length() == 0) {
			p.setNameSpace(this.currentOperation.getNameSpace());
			if (this.currentOperation.getPrefix() != null) {
				p.setPrefix(this.currentOperation.getPrefix());
			}
		}
		
		log.debug("Creating new SOAP element of parameter "+p.getQName());
	
		SOAPElement newElement = e.addChildElement(p.getQName());
		if (p.hasValue()) {
			newElement.addTextNode(p.getValue());					
		} else {
			for (Parameter child : p.getParameters()) {
				this.add(newElement,child);						
			}
		}
		if (p.hasDatatype()) {
			QName dt = p.getDatatype();
			String ns, lp = null;
			if (dt.getNamespaceURI().endsWith("/")) {
				ns = dt.getNamespaceURI();
			} else {
				ns = dt.getNamespaceURI()+"#";
			}
			lp = dt.getLocalPart();
			newElement.addAttribute(new QName(WebService2TopicMapFactory.NS_XSI,"type","xsi"), ns+lp);
		}
	}

	public Message createMessage(SOAPMessage request, SOAPMessage response) {
		MessageImpl msg = new MessageImpl(request, response);

		return this.requests.put(request, msg);
	}
	
	public Message createMessage(SOAPMessage request, Connection conn) throws IOException, SOAPException {
		
		if (conn == null || conn.getInputStream() == null) {
			throw new SOAPException("Unable to create a soap message without a valid inputstream. Please check the assigned parameters and their contained values.");
		}
		
		SOAPMessage response = MessageFactory.newInstance().createMessage(conn.getMimeHeaders(), conn.getInputStream());
		
		if (response.getSOAPPart().getEnvelope() == null) {
			SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd_HH.mm.ss.S" );
			File file = new File(WebServiceConfigurator.getLoggingDirectory()+System.getProperty("file.seperator")+df.format(new Date())+System.getProperty("file.seperator")+response.hashCode()+".txt");
			file.mkdirs();
			FileOutputStream out = new FileOutputStream(file);
			response.writeTo(out);
			out.close();
			log.fatal("Could not generate a valid soap message.");
			throw new SOAPException("Could not generate a valid soap message. The content of the soap message can be found at file: "+file.getCanonicalPath());
		}
		
		return this.createMessage(request, response);
	}
	
	/**
	 * @param ro - instance of class {@link RequestObject}, which can be extended or encapsulated (container for operations, their parameters etc.)
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 * 
	 * @see #sendRequest(SOAPMessage)
	 */
	public Message sendMessage(RequestObject ro) throws SOAPException, IOException {
		return this.sendMessage(this.createMessage(ro));
	}
	
	/**
	 * @param msg - instance of class {@link Message}, which encapsulates a request and response of a web service. It helps a lot of tracking does parts during a busy communication with a web service
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 * 
	 * @see #sendRequest(SOAPMessage)
	 */
	public Message sendMessage(Message msg) throws SOAPException, IOException {
		SOAPMessage response = this.sendMessage(msg.getRequest());
		
		MessageImpl newMsg = new MessageImpl(msg.getRequest(),response);
		if (response.getSOAPBody().getFault() != null) {
			newMsg.addError(response.getSOAPBody().getFault());
		}
		return newMsg;
	}
	
	/**
	 * @param request - instance of class {@link SOAPMessage}, which represents a request to the web service
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 * 
	 * @see #sendMessage(Message)
	 */
	private SOAPMessage sendMessage(SOAPMessage request) throws SOAPException, IOException {
		request.writeTo(System.out);
		
		WebServiceConnector conn = WebServiceConnector.newConnection(this.url);
		InputStream is = null;
		SOAPMessage response = null;
		try {
			Connection c = conn.sendRequest(request);
			is = c.getInputStream();
			response = MessageFactory.newInstance().createMessage(c.getMimeHeaders(), is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		
		return response;
	}
	
	/**
	 * <b>Implementation of Interface {@link SOAPMessage}</b>.
	 * This implementation is hidden because mainly only the functions for
	 * retrieving received information will be important.
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/01/15)
	 *
	 */
	private class MessageImpl implements Message {

		private SOAPMessage request;
		private SOAPMessage response;
		
		private Set<SOAPFault> errors;
		
		public MessageImpl() throws SOAPException {
			this.request = MessageFactory.newInstance().createMessage();
		}
		
		/**
		 * Constructor of class
		 * 
		 * @param request 
		 * @param response 
		 */
		public MessageImpl(SOAPMessage request, SOAPMessage response) {
			this.request = request;
			this.response = response;
		}

		/**
		 * @param error
		 */
		private void addError(SOAPFault error) {
			if (error == null) {
				throw new IllegalArgumentException("The assigned variable has to be initialized as an instance of class javax.xml.soap.SOAPFault");
			}
			
			this.errors.add(error);
		}
		
		
		@Override
		public boolean errorOccurred() {
			if (this.errors.size() > 0) {
				return true;
			}
			return false;
		}

		@Override
		public Set<SOAPFault> getErrors() {
			return errors;
		}

		@Override
		public SOAPMessage getRequest() {
			return this.request;
		}

		@Override
		public SOAPMessage getResponse() {
			if (response == null) {
				throw new NullPointerException("A response has to be received yet. Currently there is no response existing.");
			}
			return this.response;
		}


	}
}
