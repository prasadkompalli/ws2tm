/**
 * me.master.thesis - de.unileipzig.ws2tm.tm.util
 *
 * === TopicE.java ====
 *
 */
package de.unileipzig.ws2tm.tm.util;

import org.apache.log4j.Logger;

import de.topicmapslab.majortom.model.core.ITopic;

/**
 * @author Torsten Grigull
 * @version 0.1 (24.05.2011)
 *
 */
public class TopicE {
	
	private static Logger log = Logger.getLogger(TopicE.class);
	
		private boolean exists;
		private ITopic topic;

		public TopicE(ITopic topic, boolean exists) {
			if (log.isDebugEnabled()) {
				log.debug("Created new instance of class "+TopicE.class.getCanonicalName()+": "+MyTopicMapSystem.getInfo(topic));
			}			
			this.topic = topic;
			this.exists = exists;
		}
		
		public boolean exists() {
			return exists;
		}
		
		public ITopic getTopic() {
			return topic;
		}
}
