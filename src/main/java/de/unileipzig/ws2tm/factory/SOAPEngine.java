/**
 * 
 */
package de.unileipzig.ws2tm.factory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;


import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.Parameter;
import de.unileipzig.ws2tm.ws.soap.RequestObject;
import de.unileipzig.ws2tm.ws.soap.Message;

/**
 * @author  Torsten Grigull
 * @version  0.1 (2011/01/14)
 */
public class SOAPEngine implements Factory {

	/**
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
	private static SOAPEngine INSTANCE;
	private static Definition wsdl;
	
	private static HashMap<SOAPMessage,Message> requests = null;
	
	private SOAPEngine() {
		if (requests == null) {
			requests = new HashMap<SOAPMessage, Message>();
		}
	}
	
	/**
	 * This method returns a new instance or already initialized instance of class {@link SOAPEngine}. This instance
	 * acts as a factory instance creating soap messages, and combining request and responses depending on the web service.
	 * 
	 * This class is therefore the main access point between the web service and the running web service 2 topic map application.
	 * 
	 * @return the factory instance of class {@link SOAPEngine}
	 */
	public static SOAPEngine newInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SOAPEngine();
		}
		return INSTANCE;
	}
	
	/**
	 * This method creates instances of class {@link javax.wsdl.Operation}. However, may be these are not required any more. Because
	 * request and response exist out of different attributes.
	 * 
	 * @return an instance of class {@link javax.wsdl.Operation}
	 * @throws WSDLException this exception is thrown if a wsdl definition cannot be created. An instance is required for creating one or more instances of class {@link javax.wsdl.Operation}
	 */
	public static javax.wsdl.Operation newOperation() throws WSDLException {
		if (wsdl == null) {
			wsdl = WSDLFactory.newInstance().newDefinition();
		}
		return wsdl.createOperation();
	}
	
	/**
	 * @param request - instance of class {@link RequestObject}. This class can be extended to add functionality or to support other
	 * request types like TMQL requests and so on. 
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 */
	public Message createMessage(RequestObject request) throws SOAPException {
		if (INSTANCE == null) {
			INSTANCE = new SOAPEngine();
		}
		return INSTANCE.newMessage(request);
	}
	
	/**
	 * @param msg
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 * 
	 * @see #sendRequest(SOAPMessage)
	 */
	public Message sendMessage(Message msg) throws SOAPException, IOException {
		return this.sendRequest(msg.getRequest());
	}
	
	/**
	 * @param request
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 * 
	 * @see #sendMessage(Message)
	 */
	public Message sendRequest(SOAPMessage request) throws SOAPException, IOException {
		request.writeTo(System.out);
		
		HttpURLConnection conn = (HttpURLConnection) new URL("").openConnection();
		
		return requests.get(request);
	}
	
	/**
	 * This operations combines the creation of a request message and sends the created message right away to the web service.
	 * The response will be therefore be added to the instance of class {@link Message}, which is also the returned object.
	 * 
	 * @see #createMessage(RequestObject)
	 * @see #sendRequest(SOAPMessage)
	 * 
	 * @param request - instance of class {@link RequestObject}. This class can be extended to add functionality or to support other
	 * request types like TMQL requests and so on. 
	 * @return instance of class {@link Message}, which combines the request and response for the web service in one object
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * @throws IOException this exception is thrown if an exception occurred during the connection to the web service or during the reception of the response from the web service
	 */
	public Message requestWebService(RequestObject request) throws SOAPException, IOException {
		Message msg = this.createMessage(request);
		return this.sendMessage(msg);
	}
	
	/**
	 * @param request - an instance of class {@link RequestObject}. This class can be also extended to add functionality or to support higher request languages. Currently
	 * the class abstracts only the most required operations to access descriptions of the operations and its parameters. See the class description for further details.
	 * @return an instance of class {@link Message} with an initialized soap request object, which can be send to the described web service
	 * @throws SOAPException this exception is thrown if the request or response could not be generated
	 * 
	 * @see #sendRequest(SOAPMessage)
	 */
	private Message newMessage(RequestObject request) throws SOAPException {
		MessageImpl impl = new MessageImpl(request);
		
		SOAPMessage r = impl.getRequest();
		
		for (Operation op : request.getOperations()) {
			Iterator<Parameter> it = op.getParameters().iterator();
			SOAPBodyElement soa = null;
			if (it.hasNext()) {
				soa = r.getSOAPBody().addBodyElement(op.getQName());				
			}
			Parameter par;
			while (it.hasNext() && soa != null) {
				par = it.next();
				
				if (par.getNameSpace() == null || par.getNameSpace().length() == 0) {
					par.setNameSpace(op.getNameSpace());
					if (op.getPrefix() != null) {
						par.setPrefix(op.getPrefix());
					}
				}
				
				soa.addChildElement(par.getQName()).addTextNode(par.getValue());
			}
		}
		
		requests.put(impl.request, impl);
				
		
		return impl;
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
		private boolean errorOccurred;
		
		private Set<SOAPFault> errors;
		
		public MessageImpl(RequestObject request) throws SOAPException {
			this.request = MessageFactory.newInstance().createMessage();
		}
		
		private void addError(SOAPFault error) {
			if (error == null) {
				throw new IllegalArgumentException("The assigned variable has to be initialized as an instance of class javax.xml.soap.SOAPFault");
			}
			
			if (errorOccurred == false) {
				errorOccurred = true;
			}
			
			this.errors.add(error);
		}
		
		
		@Override
		public boolean errorOccurred() {
			return errorOccurred;
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
