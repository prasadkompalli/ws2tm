/**
 * 
 */
package de.unileipzig.ws2tm.persistence.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.persistence.Configuration;
import de.unileipzig.ws2tm.persistence.DataAccessObject;
import de.unileipzig.ws2tm.persistence.jaxb.ConfigurationJaxbImpl;

/**
 * Class to import data from configuration files following a concrete schema.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 *
 */
public class ConfigurationDAO implements DataAccessObject, Factory {

	private static Logger log = Logger.getLogger(ConfigurationDAO.class);
	
	private static ConfigurationDAO FACTORY = null;
	
	private HashMap<Configuration, File> map = null;
	
	/**
	 * Standard Constructor creating a new empty list.
	 */
	private ConfigurationDAO() {
		this.map = new HashMap<Configuration, File>();
	}
	
	/**
	 * Constructor for assigning an already existing list of instances of class
	 * {@link TranslationList}.
	 * 
	 * @param cl - list of instance of class {@link Configuration}
	 */
	private ConfigurationDAO(Collection<Configuration> cl) {
		this.map = new HashMap<Configuration, File>();
		
		for (Configuration c : cl) {
			this.map.put(c, null);
		}
		
	}
	
	public static ConfigurationDAO newInstance() {
		if (FACTORY == null) {
			FACTORY = new ConfigurationDAO();
		}
		return FACTORY;
	}

	public static ConfigurationDAO newInstance(Collection<Configuration> cl) {
		if (FACTORY == null && cl.size() > 0 && FACTORY.map == null || (FACTORY.map != null && FACTORY.map.size() == 0)) {
			FACTORY = new ConfigurationDAO(cl);
		} else if (FACTORY == null) {
			FACTORY = new ConfigurationDAO();
		}
		return FACTORY;
	}
	
	@Override
	public Configuration create() {
		ConfigurationJaxbImpl impl = new ConfigurationJaxbImpl();
		this.map.put(impl, null);
		return impl;
	}

	@Override
	public Configuration create(Object obj) {
		if (obj == null || !Configuration.class.isInstance(obj)) {
			throw new IllegalArgumentException("The assigned parameter points to null or is not an instance of class "+Configuration.class.getName());
		}
		ConfigurationJaxbImpl impl = new ConfigurationJaxbImpl((Configuration) obj);
		this.map.put(impl, null);
		return impl;
	}
	
	@Override
	public void delete(String key) {
		for (Map.Entry<Configuration, File> e : this.map.entrySet()) {
			try {
				if (e.getValue().getName() == key || e.getValue().getCanonicalPath() == key) {
					this.map.remove(e.getKey());
				}
			} catch (IOException ex) {
				log.error("Could not remove the assigned key from the index of configurations.", ex);
			}
		}

	}

	@Override
	public Configuration load(String key) throws IOException, FileNotFoundException {
		if (key == null) {
			throw new IllegalArgumentException("The assigned parameter points to null.");
		}

		Configuration c = null;

		File file = new File(key);

		if (file.exists()) {
			Unmarshaller unmarshaller;
			try {
				unmarshaller = JAXBContext.newInstance(ConfigurationJaxbImpl.class).createUnmarshaller();
				c = (Configuration) unmarshaller.unmarshal(file);
			} catch (JAXBException e) {
				e.printStackTrace();
				throw new IOException("Unable to unmarshall file " + key+ ". Check the correctness of the file.", e);
			}
		} else {
			throw new FileNotFoundException("Unable to find specified file: "+ key);
		}
		
		this.map.put(c, file);

		return c;
	}
	
	@Override
	public void save() throws IOException {
		for (Map.Entry<Configuration, File> e : this.map.entrySet()) {
			this.save(e.getKey(), e.getValue().getCanonicalPath());
		}
	}

	@Override
	public void save(String key) throws IOException, FileNotFoundException {
		if (key == null) {
			throw new IllegalArgumentException("The assigned parameter points to null.");			
		}
		
		for (Map.Entry<Configuration, File> e : this.map.entrySet()) {
			try {
				if (e.getValue().getName() == key || e.getValue().getCanonicalPath() == key) {
					this.save(e.getKey(), e.getValue().getAbsolutePath());
				}
			} catch (IOException ex) {
				log.error("Could not save the configuration assigned by key "+key+".", ex);
			}
		}	
		throw new FileNotFoundException("Assigned file could not be associated with a configuration file. Use #save(Object o, String filePath) instead.");
	}

	@Override
	public void save(Object obj, String key) throws IOException {
		if (key == null || obj == null) {
			throw new IllegalArgumentException("The assigned parameter points to null.");			
		} else if (!Configuration.class.isInstance(obj)) {
			throw new IllegalArgumentException("The assigned parameter needs to be an instance of class "+Configuration.class.getCanonicalName());
		}
		
		Configuration c = (Configuration) obj;
		
		File file = new File(key);
		
		try {
			Marshaller m = JAXBContext.newInstance(ConfigurationJaxbImpl.class).createMarshaller();
			m.marshal(c, file);
		} catch (JAXBException e) {
			throw new IOException("Unable to marshall file " + key + ". Configuration could not be saved. Check the correctness of the file.", e);
		}
		
		this.map.put(c, file);
		
	}


}
