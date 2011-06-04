/**
 * 
 */
package de.unileipzig.ws2tm.ws.soap;

import java.util.ArrayList;
import java.util.List;

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

	private List<Parameter> children = null;
	
	private String name;
	private String prefix;
	private String nameSpace;
	private String value;

	private QName datatype;
	

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
	 * Constructor of class
	 *
	 * @param name
	 * @param prefix
	 * @param nameSpace
	 * @param value
	 * @param datatype
	 */
	public Parameter(String name, String prefix, String nameSpace, String value, QName datatype) {
		this.setName(name);
		this.setPrefix(prefix);
		this.setNameSpace(nameSpace);
		this.setValue(value);
		this.setDatatype(datatype);
	}
	
	
	/**
	 * @return <code>true</code> if child elements exists, which are inserted in this parameter
	 */
	public boolean hasParameter() {
		if (this.children != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return <code>true</code> if no parameters are contained inside this parameter
	 */
	public boolean hasValue() {
		if (this.children == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return <code>true</code> if a datatype was defined
	 */
	public boolean hasDatatype() {
		if (this.datatype != null) {
			return true;
		}
		return false;
	}
	
	public QName setDatatype(QName dt) {
		this.datatype = dt;
		return dt;
	}
	
	public QName getDatatype() {
		return this.datatype;
	}
	
	/**
	 * @return the possible list of child elements, which are contained in this parameter
	 */
	public List<Parameter> getParameters() {
		return this.children;
	}
	
	/**
	 * Add a parameter, which acts as a child element of the current parameter.
	 * 
	 * @param p
	 * @return
	 */
	public Parameter addParameter(Parameter p) {
		if (children == null) {
			children = new ArrayList<Parameter>();
		}
		this.children.add(p);
		return p;
	}
	
	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public Parameter addParameter(QName name, String value) {
		return this.addParameter(new Parameter(name, value));
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
