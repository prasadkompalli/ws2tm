/**
 * me.master.thesis - de.unileipzig.ws2tm.persistence.tmql
 *
 * === WebsiteTMQLImpl.java ====
 *
 */
package de.unileipzig.ws2tm.persistence.tmql;

import java.io.File;
import java.util.Iterator;

import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.TopicMap;

import de.topicmapslab.majortom.model.core.ITopic;
import de.topicmapslab.tmql4j.common.core.exception.TMQLRuntimeException;
import de.topicmapslab.tmql4j.common.core.runtime.TMQLRuntimeFactory;
import de.topicmapslab.tmql4j.common.model.query.IQuery;
import de.topicmapslab.tmql4j.common.model.runtime.ITMQLRuntime;
import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.persistence.Website;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;

/**
 * @author Torsten Grigull
 * @version 0.1 (26.05.2011)
 *
 */
public class WebsiteTMQLImpl implements Website {

	/**
	 * 	Get all operations of an instance of w:Service 
	 */
	private static String tmqlQuery = "%prefix w "+WebService2TopicMapFactory.NS_WSDL+
	"w:Service << types >> traverse // w:Operation";
	
	/**
	 * Constructor of class
	 * @throws TMQLRuntimeException 
	 * @throws FactoryConfigurationException 
	 *
	 */
	public WebsiteTMQLImpl() throws TMQLRuntimeException, FactoryConfigurationException {
		this(TopicMapEngine.newInstance().getTopicMap(new File(WebServiceConfigurator.getFileWSDL2TM())));
	}

	public WebsiteTMQLImpl(TopicMap tm) throws TMQLRuntimeException {
		ITMQLRuntime runtime = TMQLRuntimeFactory.newFactory().newRuntime(tm);
		IQuery q = runtime.run(tmqlQuery);
		Iterator<ITopic> it = (Iterator<ITopic>) q.getResults().iterator();
	}
	
	/* (non-Javadoc)
	 * @see de.unileipzig.ws2tm.persistence.Website#getId()
	 */
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unileipzig.ws2tm.persistence.Website#reload()
	 */
	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}
	
	

}
