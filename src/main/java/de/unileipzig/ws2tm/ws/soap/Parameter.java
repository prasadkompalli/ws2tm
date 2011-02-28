/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

import javax.xml.namespace.QName;

/**
 * <b>Class Parameter</b>
 * 
 * Simple class to extend class {@link Operation}. A parameter abstracts a part of an operation. It concentrates the name, prefix,
 * namespace and foremost the value of the parameter.
 * 
 * 
 * @author Torsten Grigull
 * 
 * @version 0.1 (2010/01/30)
 *
 */
public class Parameter {

	
	private String name;
	private String prefix;
	private String nameSpace;
	private String value;
	

	/**
	 * @param name
	 * @param value
	 */
	public Parameter(QName name, String value) {
		this(name.getLocalPart(), name.getPrefix(), name.getNamespaceURI(), value);
	}

	/**
	 * @param name
	 * @param prefix
	 * @param nameSpace
	 * @param value
	 */
	public Parameter(String name, String prefix, String nameSpace, String value) {
		this.setName(name);
		this.setPrefix(prefix);
		this.setNameSpace(nameSpace);
		this.setValue(value);
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public QName getQName() {
		if (this.getPrefix() != null) {
			return new QName(this.getNameSpace(), this.getName(), this.getPrefix());
		}
		return new QName(this.getNameSpace(), this.getName());
	}
	
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
