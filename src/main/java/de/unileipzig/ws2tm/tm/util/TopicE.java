/**
 * me.master.thesis - de.unileipzig.ws2tm.tm.util
 *
 * === TopicE.java ====
 *
 */
package de.unileipzig.ws2tm.tm.util;

import org.tmapi.core.Topic;

import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.WebService2TopicMapFactory.TopicE;

/**
 * @author Torsten Grigull
 * @version 0.1 (24.05.2011)
 *
 */
public class TopicE {
		private boolean exists;
		private Topic topic;

		public TopicE(Topic topic, boolean exists) {
			if (log.isDebugEnabled()) {
				log.debug("Created new instance of class "+TopicE.class.getCanonicalName()+": "+WebService2TopicMapFactory.getInfo(topic));
			}			
			this.topic = topic;
			this.exists = exists;
		}
		
		public boolean exists() {
			return exists;
		}
		
		public Topic getTopic() {
			return topic;
		}
}
