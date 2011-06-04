/**
 * me.master.thesis - de.unileipzig.ws2tm.tm.util
 *
 * === MyTopicMapSystem.java ====
 *
 */
package de.unileipzig.ws2tm.tm.util;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.tmapi.core.Locator;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;

import de.topicmapslab.majortom.model.core.ITopic;

/**
 * @author Torsten Grigull
 * @version 0.1 (24.05.2011)
 *
 */
public class MyTopicMapSystem {
	
	private static Logger log = Logger.getLogger(MyTopicMapSystem.class);
	
	private TopicMap tm = null;
	
	public MyTopicMapSystem(TopicMap tm) {
		this.tm = tm;
	}
	
	public TopicMap getTopicMap() {
		return this.tm;
	}
	
	public TopicE createTopic(QName qname) {
		String ns = qname.getNamespaceURI();
		if (!qname.getNamespaceURI().endsWith("/")) {
			ns = qname.getNamespaceURI()+"/";
		}
		return this.createTopic(ns+qname.getLocalPart());
	}
	
	public TopicE createTopic(String identifier) {
		return this.createTopic(identifier, IDs.SubjectIdentifier);
	}

	public TopicE createTopic(Locator loc, IDs id) {
		return this.createTopic(loc.getReference(), id);
	}
	
	public TopicE createTopic(QName qname, IDs id) {
		String ns = qname.getNamespaceURI();
		if (!qname.getNamespaceURI().endsWith("/")) {
			ns = qname.getNamespaceURI()+"/";
		}
		return this.createTopic(ns+qname.getLocalPart(), id);
	}
	
	public TopicE createTopic(String identifier, IDs id) {
		ITopic topic = (ITopic) tm.createTopic();
		
		if (identifier != null) {
			
			switch (id) {
				case ItemIdentifier: 
					for (Topic t : tm.getTopics()) {
						if (t.getItemIdentifiers().contains(tm.createLocator(identifier))) {
							if (log.isDebugEnabled()) {
								log.debug("Returning already existing topic with item identifier "+identifier);
							}
							return new TopicE((ITopic) t,true);
						}
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with item identifier "+identifier);
					}
					topic = (ITopic) tm.createTopicByItemIdentifier(tm.createLocator(identifier));
					break;
				case SubjectIdentifier: 
					topic = (ITopic) tm.getTopicBySubjectIdentifier(tm.createLocator(identifier));
					if (topic != null) {
						if (log.isDebugEnabled()) {
							log.debug("Returning already existing topic with subject identifier "+identifier);
						}
						return new TopicE(topic, true);
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with subject identifier "+identifier);
					}
					topic = (ITopic) tm.createTopicBySubjectIdentifier(tm.createLocator(identifier));
					break;
				case SubjectLocator: 
					topic = (ITopic) tm.getTopicBySubjectLocator(tm.createLocator(identifier));
					if (topic != null) {
						if (log.isDebugEnabled()) {
							log.debug("Returning already existing topic with subject locator "+identifier);
						}
						return new TopicE(topic, true);
					}
					if (log.isDebugEnabled()) {
						log.debug("Created new topic with subject locator "+identifier);
					}
					topic = (ITopic) tm.createTopicBySubjectLocator(tm.createLocator(identifier));
					break;
			}
		}
		return new TopicE(topic, false);
	}
	
	/**
	 * This method creates a role defined by an identifier, which should be a http link
	 * or something similar, which address one resource, which could be addressed as role,
	 * it topic type, and its names, which can be one or more instances of class {@link NameE}.
	 * This names are simple string names, and a linked scope, which does not have to be set.
	 * 
	 * @param id - string, which identifies the topic
	 * @param type - the type of the topic, which should be created
	 * @param names - the names (name) and the scopes
	 * @return the created role, which its role type and its linked names with its scopes.
	 * 
	 * @see #createRoles()
	 */
	public ITopic createRole(String id, Topic type, NameE... names) {
		ITopic topic = this.createTopic(id, IDs.SubjectIdentifier).getTopic();
		if (type != null) {
			topic.addType(type);
		}
		for (NameE name : names) {
			topic.createName(name.getName(), name.getScopes());
		}
		return topic;
	}

	public static String getInfo(Topic ta) {
		if (ta.getNames().size() > 0) {
			return ta.getNames().iterator().next().getValue();
		}
		if (ta.getSubjectIdentifiers().size() > 0) {
			return ta.getSubjectIdentifiers().iterator().next().getReference();
		}
		if (ta.getItemIdentifiers().size() > 0) {
			return ta.getItemIdentifiers().iterator().next().getReference();				
		}
		if (ta.getSubjectLocators().size() > 0) {
			return ta.getSubjectLocators().iterator().next().getReference();				
		}
		return ta.getId();
	}		
	
	public enum IDs {
		ItemIdentifier,
		SubjectIdentifier,
		SubjectLocator
	}
}

