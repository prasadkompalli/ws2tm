/**
 * me.master.thesis - de.unileipzig.ws2tm.persistence.dao
 *
 * === WebsiteHTMLDAO.java ====
 *
 */
package de.unileipzig.ws2tm.persistence.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmapi.core.Association;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Role;
import org.tmapi.core.TopicMap;

import de.topicmapslab.majortom.model.core.ITopic;
import de.topicmapslab.tmql4j.common.core.exception.TMQLRuntimeException;
import de.topicmapslab.tmql4j.common.core.runtime.TMQLRuntimeFactory;
import de.topicmapslab.tmql4j.common.model.query.IQuery;
import de.topicmapslab.tmql4j.common.model.runtime.ITMQLRuntime;

import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.persistence.DataAccessObject;
import de.unileipzig.ws2tm.persistence.Website;
import de.unileipzig.ws2tm.persistence.tmql.WebsiteTMQLImpl;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.util.WebServiceConfigurator;

/**
 * @author Torsten Grigull
 * @version 0.1 (26.05.2011)
 *
 */
public class WebsiteDAO implements DataAccessObject, Factory {

	private static final String tmql_operations = "wsdl:operation >> instances";
	
	private static WebsiteDAO INSTANCE = null;
	
	private Logger log = Logger.getLogger(WebsiteDAO.class);
	
	private HashMap<File,Website> websites = null;
	
	private WebsiteDAO() {
		websites = new HashMap<File,Website>();
	}
	
	public static WebsiteDAO newInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WebsiteDAO();
		}
		return INSTANCE;
	}
	
	@Override
	public Website create() {
		try {
			return this.create(TopicMapEngine.newInstance().getTopicMap(new File(WebServiceConfigurator.getFileWSDL2TM())));
		} catch (FactoryConfigurationException e) {
			log.fatal("TopicMapEngine or the used topic map system could not be initialized. Unable to create a topic map or access a topic map without the topic map engine.",e);
		}
		return null;
	}

	@Override
	public Website create(Object obj) {
		Website w = null;
		if (TopicMap.class.isInstance(obj)) {
			try {
				w = new WebsiteTMQLImpl((TopicMap) obj);
				websites.put(TopicMapEngine.newInstance().getFile((TopicMap) obj), w);
			} catch (TMQLRuntimeException e) {
				log.fatal("Unable to execute queries against a topic map. This means a serious problem, which has to be solved before any further actions will be taken.",e);
			} catch (FactoryConfigurationException e) {
				log.fatal("Topic Map engine could not be initialized. This means, the TMQLRuntimeFactory cannot create a new runtime with an invalid topic map.",e);
			}
		} else {
			log.error("An instance of class org.tmapi.core.Topic Map is required.");
		}
		return w;
	}

	@Override
	public void delete(String key) {
		for (Map.Entry<File, Website> e : websites.entrySet()) {
			try {
				if (e.getKey().getCanonicalPath().endsWith(key)) {
					log.info("Deleting website and configuration files associated with topic map: "+e.getKey().getCanonicalPath());
				}
			} catch (IOException e1) {
				// can be ignored
			}
		}
	}

	@Override
	public Website load(String key) throws IOException, FileNotFoundException {
		throw new RuntimeException("This method is not supported with this instance of a data access object.");
	}

	@Override
	public void save() throws IOException {
		throw new RuntimeException("This method is not supported with this instance of a data access object.");
	}

	@Override
	public void save(String key) throws IOException, FileNotFoundException {
		throw new RuntimeException("This method is not supported with this instance of a data access object.");
	}

	@Override
	public void save(Object obj, String key) throws IOException {
		throw new RuntimeException("This method is not supported with this instance of a data access object.");
	}

}
