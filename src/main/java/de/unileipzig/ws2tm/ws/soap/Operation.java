/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * <b>Class Operation</b>
 * 
 * @author Torsten Grigull
 *
 */
public class Operation {

	
	
	private String name;
	private String prefix;
	private String nameSpace;
	
	private Collection<Parameter> params;
	private QName datatype;
	private String value;

	public Operation(String nameSpace, String name , String prefix) {
		this.setName(name);
		this.setPrefix(prefix);
		this.setNameSpace(nameSpace);		
		
	}
	
	public Operation(QName name) {
		this(name.getNamespaceURI(),name.getLocalPart(), name.getPrefix());
	}
	
	public Parameter addParameter(String ns, String localName, String prefix, String value) {
		return this.addParameter(new QName(ns, localName, prefix), value);
	}
	
	public Parameter addParameter(String ns, String localName, String value) {
		return this.addParameter(new QName(ns, localName), value);		
	}
	
	public Parameter addParameter(String name, String value) {
		return this.addParameter(new QName(null, name), value);		
	}
	
	public Parameter addParameter(QName name, String value) {
		if (name.getNamespaceURI() == null || name.getNamespaceURI().length() == 0) {
			if (this.getPrefix() != null) {
				name = new QName(this.getNameSpace(), name.getLocalPart(), this.getPrefix());
			} else {
				name = new QName(this.getNameSpace(), name.getLocalPart());				
			}
		}
		return this.addParameter(new Parameter(name, value));
	}
	
	/**
	 * @param param
	 * @return
	 */
	public Parameter addParameter(Parameter param) {
		if (this.params == null) {
			this.params = new ArrayList<Parameter>();			
		}
		this.params.add(param);
		return param;
	}
	
	/**
	 * @return
	 */
	public boolean hasParameters() {
		if (this.params != null) {
			return true;
		}
		return false;
	}
	
	public boolean hasValue() {
		if (this.params == null) {
			return true;
		}
		return false;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean hasDatatype() {
		if (this.datatype != null) {
			return true;
		}
		return false;
	}
	
	public QName getDatatype() {
		return this.datatype;
	}
	
	public QName setDatatype(QName dt) {
		this.datatype = dt;
		return dt;
	}
	
	
	public Parameter getParameter(int index) {
		if (this.params.size() <= index || index < 0) {
			throw new IllegalArgumentException("Assigned index is out of bounds of the intern used array.");
		}
		
		Iterator<Parameter> it = this.params.iterator();
		int i = 0;
		
		while (it.hasNext()) {
			if (i == index) {
				return it.next();
			}
			i++;
		}
		return null;
	}

	public Collection<Parameter> getParameters() {
		return this.params;
	}
	
	public QName getQName() {
		if (this.getPrefix() != null) {
			return new QName(this.getNameSpace(), this.getName(), this.getPrefix());		
		}
		return new QName(this.getNameSpace(), this.getName());
	}
	
	/**
	 * @param nameSpace the nameSpace to set
	 */
	public void setNameSpace(String nameSpace) {
		if (nameSpace == null || nameSpace.length() == 0) {
			throw new IllegalArgumentException("The parameter 'nameSpace' needs to be initialized and a value has to be assigned to it.");
		}
		this.nameSpace = nameSpace;
	}

	/**
	 * @return the nameSpace
	 */
	public String getNameSpace() {
		return nameSpace;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		if (prefix != null && prefix.length() > 0) {
			this.prefix = prefix;
		}
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("The parameter 'name' needs to be initialized and a value has to be assigned to it.");
		}
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	
}
