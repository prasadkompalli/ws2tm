/**
 * 
 */
package de.unileipzig.ws2tm.persistence.jaxb;

import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 * 
 */
@XmlType(name="Entry")
public class Entry {

	@XmlID
	@XmlAttribute(required = true)
	private String key;
	
	@XmlAttribute(required = false)
	private String refersTo;
	
	@XmlValue
	private String value;

	/**
	 * 
	 */
	public Entry() {
		this.key = new Random().nextLong()+"";
		this.value = "";
		this.refersTo = null;
	}

	/**
	 * @param e
	 */
	public Entry(Entry e) {
		this.key = e.getKey();
		this.refersTo = e.getRefersTo();
		this.value = e.getValue();
	}
	
	/**
	 * @param key
	 * @param value
	 * @param refersTo
	 */
	public Entry(String key, String value, String refersTo) {
		this.key = key;
		this.value = value;
		this.refersTo = refersTo;
	}

	/**
	 * @return
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @return
	 */
	public String getRefersTo() {
		return refersTo;
	}
}
