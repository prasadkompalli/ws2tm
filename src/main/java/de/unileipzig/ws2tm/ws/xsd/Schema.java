/**
 * 
 */
package de.unileipzig.ws2tm.ws.xsd;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/02/17)
 *
 */
public interface Schema {

	/**
	 * @param qname
	 * @return
	 */
	public Element getElement(QName qname);
	
	/**
	 * @return
	 */
	public String getTargetNameSpace();
	
	/**
	 * @param qname
	 * @return
	 */
	public Type getType(QName qname);
	
	/**
	 * @return
	 */
	public Collection<Element> getElements();
	
	/**
	 * @return
	 */
	public Collection<Type> getTypes();
	
	/**
	 * @param qname
	 * @return
	 */
	public Type addType(QName qname);
	
	/**
	 * @param qname
	 * @param type
	 * @return
	 */
	public Element addElement(QName qname, Type type);
	
}
