package de.unileipzig.ws2tm.ws.xsd;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import de.unileipzig.ws2tm.ws.xsd.Type.ListElement;


/**
 * @author Torsten Grigull
 * @version 0.1 (2011/02/23)
 */
//TODO documentation needed for the methods and the class itself
public class Element implements Comparable<Element>, ListElement {
	
	private static Logger log = Logger.getLogger(Element.class);
	
	QName name;
	QName type;
	
	int minOccurs = 0;
	int maxOccurs = 1;
	
	public Element() {
		log.debug("Created new element without any attributes yet.");
	}
	
	public Element(QName name, QName type) {
		this.name = name;
		try {
			if (SchemaParser.getType(type) != null) {
				this.type = type;
			}
		} catch (IllegalArgumentException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		this.type = type;
		log.debug("Created Element "+name.toString()+" with datatype "+type.toString());
	}

	public String getName() {
		return this.name.getLocalPart();
	}

	public QName getQName() {
		return this.name;
	}
	

	public Type getType() throws IOException {
		Type impl = SchemaParser.getType(type);
		if (impl != null) {
			return impl;
		}
		return null;
	}

	public void setQName(QName name) {
		this.name = name;
	}

	public void setType(QName type) {
		this.type = type;
	}

	public Type setType(Type type) {
		this.type = type.getQName();
		return type;
	}

	public boolean canOccurrMoreThanOnce() {
		if (maxOccurs > 1) {
			return true;
		}
		return false;
	}

	public boolean isRequired() {
		if (minOccurs >= 1) {
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(Element e) {
		if (this.getQName() == e.getQName()) {
			return 0;
		}
		int i = this.getQName().getNamespaceURI().compareTo(e.getQName().getNamespaceURI());
		if (i == 0) {
			return this.getQName().getLocalPart().compareTo(e.getQName().getLocalPart());
		}
		return i;
	}
	
	@Override
	public Element getObject() {
		return this;
	}
	
	@Override
	public int getListElementType() {
		return Type.ELEMENT;
	}
	
}
