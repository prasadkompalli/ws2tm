/**
 * 
 */
package de.unileipzig.ws2tm.persistence;

import java.util.Collection;
import java.util.List;

import de.unileipzig.ws2tm.persistence.jaxb.Entry;

/**
 * This class abstracts the configuration which acts as the main 
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 *
 */
public interface Configuration {

	public String getConfigurationDescription();
	
	public String getConfigurationComment();
	
	public List<String> getKeys();
	
	public String getValue(String key);
	
	public Collection<Entry> getConfigurationParameters();
	
	public Entry addEntry(String key, String value);
	
	public Entry addEntry(String key, String value, String refersTo);
	
	public Entry removeEntry(String key);
	
}
