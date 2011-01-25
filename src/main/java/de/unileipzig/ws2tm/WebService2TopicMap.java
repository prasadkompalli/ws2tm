/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.IOException;

import org.tmapi.core.TopicMap;

import de.unileipzig.ws2tm.ws.soap.RequestObject;

/**
 * <b>Interface WebService2TopicMap</b> describes the main access point. Instances are
 * created via {@link WebService2TopicMapFactory} and its function {@link WebService2TopicMapFactory#}
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/01/17)
 *
 */
public interface WebService2TopicMap {

	public TopicMap newWebService(String wsdlPath) throws IOException, InitializationException;

	public TopicMap newWebServiceRequest(RequestObject request) throws IOException;
	
	public TopicMap mergeTopicMaps();
	
}
