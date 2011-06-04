package de.unileipzig.ws2tm.ws.xsd;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Restriction {

	private static Logger log = LogManager.getLogger(Restriction.class);
	
	private QName base;
	private Collection<String> enumeration = new HashSet<String>();

	private String pattern = new String();
	
	public Restriction() {
		log.debug("Created new Instance of class "+Restriction.class.getCanonicalName());
	}
	
	public Restriction(QName base) {
		this.base = base;
		log.debug("Created new Instance of class "+Restriction.class.getCanonicalName()+" with base type "+base.toString());
	}
	
	/**
	 * @param name the name to set
	 */
	public void setBase(QName name) {
		this.base = name;
	}

	/**
	 * @return the name
	 */
	public QName getBase() {
		return base;
	}
	
	/**
	 * @return
	 */
	public Collection<String> getEnumerations() {
		return this.enumeration;
	}
	
	/**
	 * @param enumeration
	 * @return
	 */
	public String addEnumeration(String enumeration) {
		this.enumeration.add(enumeration);
		return enumeration;
	}
	
	public boolean hasEnumeration() {
		if (this.enumeration.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param enums
	 * @return
	 */
	public Collection<String> addEnumerations(Collection<String> enums) {
		this.enumeration.addAll(enums);
		return enums;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public String getPattern() {
		return this.pattern;
	}
	
	public boolean hasPattern() {
		if (this.pattern.length() > 0) {
			return true;
		}
		return false;
	}
	
}
