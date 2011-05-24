/**
 * me.master.thesis - de.unileipzig.ws2tm.tm.util
 *
 * === NameE.java ====
 *
 */
package de.unileipzig.ws2tm.tm.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.tmapi.core.Topic;

/**
 * @author Torsten Grigull
 * @version 0.1 (24.05.2011)
 *
 */
public class NameE {
	
	private static Logger log = Logger.getLogger(NameE.class);
	
	private String name;
	private Set<Topic> scopes;
	
	public NameE(String name, Topic...scopes){
		log.debug("Created new instance of class "+NameE.class.getCanonicalName()+": "+name);
		this.name = name;
		
		this.scopes = new HashSet<Topic>();
		for (Topic topic : scopes) {
			this.scopes.add(topic);
		}
		
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<Topic> getScopes() {
		return this.scopes;
	}
	
}