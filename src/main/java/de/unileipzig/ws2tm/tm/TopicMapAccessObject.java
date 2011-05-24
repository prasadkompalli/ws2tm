/**
 * 
 */
package de.unileipzig.ws2tm.tm;

import java.util.Set;

import org.tmapi.core.Association;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

/**
 * @author Torsten Grigull
 * @version 0.1 (2010/12/01)
 *
 */
public interface TopicMapAccessObject {
	
	public TopicMap load();
	
	public void save(TopicMap tm);
	
	public Set<Topic> getTopics();
	
	public Set<Occurrence> getOccurrences();
	
	public Set<Association> getAssociations();
	
}
