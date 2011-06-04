/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.IOException;

import org.tmapi.core.TopicMap;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.ws.soap.Authentication;
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

	public TopicMapAccessObject newWebService(String wsdlPath) throws IOException, InitializationException;

	public TopicMapAccessObject newWebServiceRequest(RequestObject request) throws IOException, InitializationException;
	
	public TopicMap mergeTopicMaps();
	
	public boolean authenticationRequired();
	
	public Authentication setAuthenticationParameter(String user, String pw);
	
	public Authentication getAuthentication();	
}
