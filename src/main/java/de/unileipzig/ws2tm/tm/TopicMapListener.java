/**
 * 
 */
package de.unileipzig.ws2tm.tm;

import org.tmapi.core.Construct;

import de.topicmapslab.majortom.model.event.ITopicMapListener;
import de.topicmapslab.majortom.model.event.TopicMapEventType;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/01/14)
 *
 */
public class TopicMapListener implements ITopicMapListener {

	/**
	 * 
	 */
	public TopicMapListener() {
		// Initialize new Topic Map Listener
	}

	@Override
	public void topicMapChanged(String code, TopicMapEventType event, Construct newValue, Object newObj, Object oldObj) {
		
	}

}
