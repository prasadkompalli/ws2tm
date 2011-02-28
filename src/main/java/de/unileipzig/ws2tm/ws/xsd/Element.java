package de.unileipzig.ws2tm.ws.xsd;

import java.io.IOException;

import javax.xml.namespace.QName;

import de.unileipzig.ws2tm.ws.xsd.Type.ListElement;


/**
 * @author Torsten Grigull
 * @version 0.1 (2011/02/23)
 */
public class Element implements Comparable<Element>, ListElement {
	QName name;
	QName type;
	
	int minOccurs = 0;
	int maxOccurs = 1;
	
	public Element() {
		// TODO Auto-generated constructor stub
	}

	public Element(QName name, QName type) {
		this.name = name;
		try {
			if (SchemaParser.getType(type) != null) {
				this.type = type;
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		}
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
