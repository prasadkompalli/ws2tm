/**
 * 
 */
package de.unileipzig.ws2tm.persistence.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.unileipzig.ws2tm.persistence.Configuration;

/**
 * Implementation of class {@link Configuration}
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="configuration")
@XmlType(name = "ConfigurationJaxbImpl")
public class ConfigurationJaxbImpl implements Configuration {

	@XmlElement(required=false)
	private String description;
	
	@XmlElement(required=false)
	private String comment;
	
	@XmlElementWrapper
	@XmlElement(type=Entry.class, name="item")
	private Collection<Entry> items;
	
	/**
	 * 
	 */
	public ConfigurationJaxbImpl() {
		this.setConfigurationDescription("Configuration");
		this.setConfigurationComment("");
		this.items = new ArrayList<Entry>();
	}
	
	/**
	 * @param c
	 */
	public ConfigurationJaxbImpl(Configuration c) {
		this.comment = c.getConfigurationComment();
		this.description = c.getConfigurationDescription();
		this.items = c.getConfigurationParameters();
		
	}

	@Override
	public Collection<Entry> getConfigurationParameters() {
		return this.items;
	}

	@Override
	public List<String> getKeys() {
		List<String> keys = new ArrayList<String>();
		for (Entry e : this.items) {
			keys.add(e.getKey());
		}
		return keys;
	}

	@Override
	public String getValue(String key) {
		for (Entry e : this.items) {
			if (key.equalsIgnoreCase(e.getKey())) {
				return e.getValue();
			}
		}
		return null;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setConfigurationComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public String getConfigurationComment() {
		return comment;
	}


	/**
	 * @param description the description to set
	 */
	public void setConfigurationDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getConfigurationDescription() {
		return this.description;
	}

	@Override
	public Entry addEntry(String key, String value) {
		return this.addEntry(key, value, null);
	}

	@Override
	public Entry addEntry(String key, String value, String refersTo) {
		Entry e = new Entry(key, value, refersTo);
		this.items.add(e);
		return e;
	}

	@Override
	public Entry removeEntry(String key) {
		for (Entry e : this.items) {
			if (key.equals(e.getKey())) {
				if (this.items.remove(e)) {
					return e;
				}
			}
		}		
		return null;
	}
	
}
