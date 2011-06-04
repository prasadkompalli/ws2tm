/**
 * 
 */
package de.unileipzig.ws2tm.ws.xsd;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.ws.xsd.Schema;
import de.unileipzig.ws2tm.ws.xsd.impl.SchemaImpl;

/**
 * XML Schema Document Parser
 * <p>
 * Parses and organizes XML Schema documents depending on the url they are from.
 * </p>
 * 
 * What do I try to do:
 * Elements and Types, recognize them (their names, and types)
 * Elements are actually simple: only attributes
 *  name
 *  type
 *  minOccurs
 *  maxOccurs
 *  
 * Types are very complex... simple or complex 
 *  name or innerTypeDefinition
 *  any number of children or restriction
 *  any number of attributes or references to attributegroups
 *  any number of sequences, choices and these can have any number of sequences and choices
 *  any number of minOccurs and maxOccurs defining the elements, sequences, choices, all
 * @author Torsten Grigull
 * @version 0.1 (2011/02/10)
 *
 */
public class SchemaParser implements Factory {

	private static SchemaParser FACTORY = null;
	private static int counter = 0;
	

	/**
	 * HashMap for all retrieved schemas. These are required for accessing all temporary saved instances of class {@link Type} and {@link Element}
	 * Key of Map: Namespace of Schema
	 * Value of Map: Schema with namespace addressed by the key of the map
	 */
	private HashMap<String, Schema> schemas;
	
	/**
	 * Collection of all integrated schemas and their types. The data types (complex and simple) are divided from their schema, and should be used without them
	 */
	private HashMap<QName, Type> types;
	/**
	 * Collection of all integrated schemas and their elements. The element definitions are divided from their schema, and should be used without them
	 */
	private HashMap<QName, Element> elements;	
	
	/**
	 * Logging Instance (Log4j Apache Foundations)
	 */
	private static Logger log = Logger.getLogger(SchemaParser.class);
	
	
	/**
	 * Private FACTORY Constructor
	 */
	private SchemaParser() {
		
		types = new HashMap<QName, Type>();
		elements = new HashMap<QName, Element>();
		
		//BROKENWINDOW possible chance that bugs may occur, if this declaration remains. However, I think the links to schema instances should not be cut...
		schemas = new HashMap<String, Schema>();
		log.info("Factory class "+SchemaParser.class.getCanonicalName()+" successfully initialzed.");
	}
	
	public static SchemaParser getFactory() {
		if (FACTORY == null) {
			FACTORY = new SchemaParser();
		}
		return FACTORY;
	}
	
	/**
	 * @param qname
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static Type getType(QName qname) throws IllegalArgumentException, IOException {
		SchemaParser.getFactory();
		if (SchemaParser.getFactory().types.containsKey(qname)) {
			return SchemaParser.getFactory().types.get(qname);
		}
		SchemaParser.getFactory().addSchema(qname.getNamespaceURI());
		return SchemaParser.getFactory().types.get(qname);
	}
	
	/**
	 * @param qname
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static Element getElement(QName qname) throws IllegalArgumentException, IOException {
		SchemaParser.getFactory();
		if (SchemaParser.getFactory().elements.containsKey(qname)) {
			return SchemaParser.getFactory().elements.get(qname);
		}
		SchemaParser.getFactory().addSchema(qname.getNamespaceURI());
		return SchemaParser.getFactory().elements.get(qname);		
	}
	
	/**
	 * @param nameSpace
	 * @return
	 */
	public Schema getSchema(String nameSpace) {
		if (schemas.containsKey(nameSpace)) {
			return schemas.get(nameSpace);
		}
		return null;
	}
	
	/**
	 * This method can be accessed only from the factory instance.
	 * It returns the Schema defined by the url. This method
	 * returns only already parsed schemas. Therefore no new schema
	 * will be returned and added via this function.
	 * 
	 * Please consult function {@link #addSchema(URL)} for this kind of task.
	 * 
	 * @param urlToSchema - url pointing to the schema or defining the schema as namespace
	 * @return instance of class {@link Schema}, which was saved using the
	 * the parameter
	 */
	public Schema getSchema(URL urlToSchema) {
		return getSchema(urlToSchema.toString());
	}
	
	/**
	 * It is pretty hard to get an already parsed XML Schema using the url used to access
	 * the schema or its name space, because they may change during the parsing of the schema.
	 * Therefore two methods exist to get an already existing instance of class {@link Schema}
	 * more easily and secure.
	 * 
	 * This method returns an instance of class {@link Schema} if the assigned instance of class
	 * {@link Element} can be associated with the instance of class {@link Schema}.
	 * 
	 * @param e - instance of class {@link Element}, which should be associated with an already existing XML Schema
	 * @return the instance of class {@link Schema}, which contains the assigned instance of class {@link Element}
	 */
	public Schema getSchema(Element e) {
		for (Schema s : this.getSchemas()) {
			if (s.getElements().contains(e)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * This method returns an instance of class {@link Schema} if the assigned instance of class
	 * {@link Type} can be associated with the instance of class {@link Schema}.
	 * 
	 * @param e - instance of class {@link Type}, which should be associated with an already existing XML Schema
	 * @return the instance of class {@link Schema}, which contains the assigned instance of class {@link Type}
	 */
	public Schema getSchema(Type e) {
		for (Schema s : this.getSchemas()) {
			if (s.getTypes().contains(e)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public Collection<Schema> getSchemas() {
		return this.schemas.values();
	}

	/**
	 * @param ns
	 * @return
	 * @throws IOException
	 */
	public Schema addSchema(String ns) throws IOException {
		if (ns == null || ns.length() == 0) {
			throw new IllegalArgumentException("The assigned parameter needs at least the namespace uri and its local part.");
		}
				
		try {
			//BROKENWINDOW
			/*
			if (ns.startsWith("http://www.w3.org/2001/XMLSchema")) {
				//url = new URL(ns.replaceFirst("/$", "")+".xsd");
			}
			*/
			return this.addSchema(new URL(ns));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("The assigned namespace uri seems to contain illegal signs for a valid URL.",e);
		}
	}

	public Schema addSchema(URL url) throws IOException {

		if (schemas.containsKey(url.toString())) {
			return schemas.get(url.toString());
		}

		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		log.warn("This function was called "+counter+++" times.");
		
		try {
			
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			DocumentBuilder dom = factory.newDocumentBuilder();
			ErrorHandler myErrorHandler = new MyErrorHandler(url.toString());
			dom.setErrorHandler(myErrorHandler);
			log.info("Opening Inputstream to url "+url.toString());
			return this.addSchema(dom.parse(url.openStream()), url.toString());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Class "+DocumentBuilderFactory.class.getCanonicalName()+" could not create a new instance of class "+DocumentBuilder.class.getCanonicalName(),e);
		} catch (SAXException e) {
			throw new IOException("Schema from url "+url.toString()+" could not be parsed.",e);
		} catch (IOException e) {
			throw new IOException("Schema form url "+url.toString()+" could not be parsed.",e);
		}
	}

	public Schema addSchema(Document dom, String tns) {
		
		if (schemas.containsKey(tns)) {
			return schemas.get(tns);
		}
		
		Schema s = new SchemaImpl(dom, tns);
		
		for (Element e : s.getElements()) {
			this.elements.put(e.getQName(), e);
		}
		
		for (Type t: s.getTypes()) {
			this.types.put(t.getQName(), t);
		}
		
		schemas.put(s.getTargetNameSpace(), s);
		
		return s;
	}
	
	/**
	 * @param s - instance of class {@link javax.wsdl.extensions.schema.Schema} 
	 * @param tns - the target name space of the
	 * @return
	 */
	public Schema addSchema(javax.wsdl.extensions.schema.Schema s, String tns) {
		return this.addSchema(s.getElement().getOwnerDocument(), tns);
	}

	/**
	 * Error Handler class, it is static, because all the functionality will be the same no matter how many
	 * instances of class {@link SchemaParser} may exist (in reality, their exist only one instance: the factory class: {@link SchemaParser#getFactory()})
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/02/15)
	 */
	private static class MyErrorHandler implements ErrorHandler {

		private String ns;

		public MyErrorHandler(String ns) {
			super();
			this.ns = ns;
		}
		
		@Override
		public void error(SAXParseException e) throws SAXException {
			log.error("Error during parsing of document "+ns+": "+e.getMessage());
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			log.error("Critical Error during parsing of document "+ns+": "+e.getMessage());
		}

		@Override
		public void warning(SAXParseException e) throws SAXException {
			log.error("Problem during parsing of document "+ns+": "+e.getMessage());
		}
		
	}
	
}
