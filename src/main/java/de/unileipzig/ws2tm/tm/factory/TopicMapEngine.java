package de.unileipzig.ws2tm.tm.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapExistsException;
import org.tmapi.core.TopicMapSystem;
import org.tmapi.core.TopicMapSystemFactory;
import org.tmapix.io.CTMTopicMapWriter;
import org.tmapix.io.XTM10TopicMapWriter;
import org.tmapix.io.XTM2TopicMapWriter;
import org.tmapix.io.XTMVersion;

/**
 * <b>TopicMapEngine</b> <p> <i>TopicMapEngine</i> is intended, to create topic map instances of interface {@link de.unileipzig.asv.tm2speech.TopicMap} . </p>
 * @author  Torsten Grigull
 * @version  0.1 (2010/08/01)
 * 
 */
public class TopicMapEngine {

	/**
	 * Factory INSTANCE, which will be return via   {@link #newInstance()}  .
	 */
	private static TopicMapEngine INSTANCE = null;

	private static HashMap<TopicMap, File> topicmaps = null;
	
	public static final int XTM_1_0 = 1;
	public static final int XTM_2_0 = 2;
	public static final int XTM_2_1 = 3;
	
	private static final int LTM_1_0 = 4;
	public static final int CTM_1_0 = 5;
	
	public static boolean OVERWRITE = false;
	
	private int sFormat = 0;

	private TopicMapSystem TMSystem = null;
	
	/**
	 * Logging Instance
	 * 
	 * @see LogManager
	 * @see LogManager#getLogger(Class)
	 */
	private static Logger log = Logger.getLogger(TopicMapEngine.class.getClass().getSimpleName()); // LogManager.getLogger(TopicMapEngine.class.getClass());

	/**
	 * Private standard constructor of this factory instance
	 * 
	 * @throws FactoryConfigurationException
	 *             If the TopicMapEngine and the connected TopicMapSystem could
	 *             not be created
	 */
	private TopicMapEngine() throws FactoryConfigurationException {

		TMSystem = this.getTopicMapSystem();
		topicmaps = new HashMap<TopicMap, File>();
		
		this.setWritingModus(TopicMapEngine.XTM_2_1);
		
	}

	/**
	 * Create new instance of class {@link TopicMapEngine}.
	 * 
	 * @return new instance of class {@link TopicMapEngine}
	 * 
	 * @throws FactoryConfigurationException
	 *             If the TopicMapEngine and the connected TopicMapSystem could
	 *             not be created
	 * @see TopicMapEngine
	 * @see #TopicMapEngine()
	 */
	public static TopicMapEngine newInstance() throws FactoryConfigurationException {
		if (INSTANCE == null) {
			INSTANCE = new TopicMapEngine();
		}
		return INSTANCE;
	}
	
	/**
	 * Create a new instance of topic map. This instance will be registered for
	 * a later use. However, you are able to create an empty or a filled topic
	 * map. These topic maps are able to be merged with each other. A merging
	 * would be important to extend existing topic maps with the knowledge of
	 * other topic maps without changing the content of the topic maps.
	 * 
	 * @param xtmFile
	 * 			  - instance of class {@link File}, which will be filled with XTM 2.0 content if the returned instance of class {@link TopicMap} gets content.
	 * @param baseLocator
	 *            - Base Locator of a TopicMap as String
	 * @return An instance of class {@link de.unileipzig.asv.tm2speech.TopicMap}
	 * 
	 * @throws FactoryConfigurationException
	 *             If the TopicMapEngine and the connected TopicMapSystem could
	 *             not be created
	 * @throws NullPointerException
	 *             If specified parameters haven a value pointing to null
	 * @throws MalformedIRIException
	 *             If the specified path does not follow an IRI specification.
	 * @throws IOException
	 *             If the specified file cannot be written or read because an IO
	 *             error occurred.
	 * @throws TopicMapExistsException 
	 * @throws IllegalArgumentException 
	 * 
	 * @see #createNewTopicMapInstance(File,Locator)
	 */
	public TopicMap createNewTopicMapInstance(File xtmFile, String baseLocator) throws FactoryConfigurationException, MalformedIRIException, IllegalArgumentException {
		return this.createTopicMapInstance(xtmFile, this.getTopicMapSystem().createLocator(baseLocator));
	}

	/**
	 * Public function to load an existing topic map or create a new one. First,
	 * an attempt will be started to create a new topic map. If this fails
	 * because an {@link TopicMapExistsException} will be thrown, than the
	 * already existing TopicMap will be loaded and returned. However, both
	 * options can fail because of an {@link FactoryConfigurationException},
	 * which has to be handeld.
	 * 
	 * @param xtmFile
	 *            - XTM 2.0 {@link TopicMap} File, or file, which should be written.
	 * @param loc
	 *            - Base Locator of a {@link TopicMap}
	 * @return an instance of a {@link TopicMap} (new instance of already existing
	 *         instance)
	 * @throws FactoryConfigurationException
	 *             If the TopicMapEngine and the connected TopicMapSystem could
	 *             not be created
	 * @throws IllegalArgumentException
	 *             If one or both parameters contain a null value.
	 * @throws TopicMapExistsException 
	 */
	public TopicMap createTopicMapInstance(File xtmFile, Locator loc) throws FactoryConfigurationException, IllegalArgumentException {

		// Test assigned parameters
		if (loc == null) {
			throw new IllegalArgumentException("Specified base locator points to null.");
		}

		// Create a new instance of org.tmapi.core.TopicMap
		TopicMap tm = null;
		try {
			tm = this.getTopicMapSystem().createTopicMap(loc);
		} catch (TopicMapExistsException e) {
			tm = this.getTopicMapSystem().getTopicMap(loc);
		}
		
		if (tm == null) {
			throw new FactoryConfigurationException("The Topic Map System could not load or create a topic map with the specified locator "+loc.getReference());
		}
		
		topicmaps.put(tm, xtmFile);
		
		// Return the a new instance of de.unileipzig.asv.tm2speech.TopicMap, which extends org.tmapi.core.TopicMap.
		return tm;
	}

	/**
	 * @param locator
	 * @return
	 * @throws MalformedIRIException
	 * @throws FactoryConfigurationException
	 */
	public Locator createBaseLocator(String locator) throws MalformedIRIException, FactoryConfigurationException {
		return this.getTopicMapSystem().createLocator(locator);
	}
	
	public void setWritingModus(int modus) {
		if (modus < XTM_1_0 || modus > CTM_1_0) {
			throw new IllegalArgumentException("Currently this topic map engine supports only XTM writer. XTM 1.0, XTM 2.0 and XTM 2.1 are supported.");
		}
		
		sFormat = modus;
	}
	
	/**
	 * @param tm - the topic map which is associated via this factory with an instance of class {@link File}, which will be filled with the information of the topic map in the XTM 2.0 standard
	 * @throws IOException if the instance of class {@link FileOutputStream} could not be created, or the writing process was interrupted by the operation system.
	 * 
	 * @see XTM20TopicMapWriter
	 * @see XTM20TopicMapWriter#write
	 */
	public void write(TopicMap tm) throws IOException {
		this.write(tm, sFormat);
	}
	
	public void write(TopicMap tm, int WRITE_MODUS) throws IOException {
		if (!topicmaps.containsKey(tm)) {
			throw new IllegalArgumentException("The assigned topic map instance does not exist");
		}
		
		File file = topicmaps.get(tm);
		if (file.exists() && OVERWRITE == false) {
			throw new IOException("The file already exists. The current configuration does not allow the writing process to overwrite the already existing file. Please change the configuration or move the already existing file to another path.");
		}
		
		OutputStream out = new FileOutputStream(file);
		
		// TODO add more writer for topic maps
		switch (WRITE_MODUS) {
			case XTM_1_0: new XTM10TopicMapWriter(out, tm.getLocator().getReference()).write(tm); break;
			case XTM_2_0: new XTM2TopicMapWriter(out, tm.getLocator().getReference(), XTMVersion.XTM_2_0).write(tm); break;
			case XTM_2_1: new XTM2TopicMapWriter(out, tm.getLocator().getReference(), XTMVersion.XTM_2_1).write(tm); break;
			case CTM_1_0: new CTMTopicMapWriter(out, tm.getLocator().getReference()).write(tm); break;
			case LTM_1_0: throw new IllegalArgumentException("The LTM Topic Map Writer is currently not supported. Please choose another, e.g. XTM Writer Format");
			default: throw new IllegalArgumentException("Could not choose a writing system, because an illegal number was set. Please arrange a valid writing modus.");
		}
	}
	
	/**
	 * This method links the assigned file with the assigned topic map.
	 * This may overwrite the saved settings, therefore the topic map will be written to the new assigned file.
	 * After, this function calls method {@link #write(TopicMap)}.
	 * @param file - instance of class {@link File}, which will contain the content of the assigned instance of class {@link TopicMap}
	 * @param tm - instance of class {@link TopicMap}. Its content will be written to an external file in XTM 2.0
	 * @throws IOException  - 
	 * 
	 * @see #write(TopicMap)
	 */
	public void write(File file, TopicMap tm) throws IOException {
		this.add(tm, file);
		this.write(tm);
	}
	
	/**
	 * This method links the assigned file with the assigned topic map.
	 * This may overwrite the saved settings, therefore the topic map will be written to the new assigned file.
	 * After, this function calls method {@link #write(TopicMap)}.
	 * @param file - instance of class {@link File}, which will contain the content of the assigned instance of class {@link TopicMap}
	 * @param tm - instance of class {@link TopicMap}. Its content will be written to an external file in XTM 2.0
	 * @param WRITEMODUS 
	 * @throws IOException  - 
	 * 
	 * @see #write(TopicMap)
	 * @see #write(TopicMap, int)
	 * @see #setWritingModus(int)
	 */
	public void write(File file, TopicMap tm, int WRITEMODUS) throws IOException {
		this.setWritingModus(WRITEMODUS);
		this.add(tm, file);
		this.write(tm);
	}
	
	/**
	 * Setter method 
	 * @param tm
	 * @param file
	 */
	public void add(TopicMap tm, File file) {
		topicmaps.put(tm, file);
	}
	
	/**
	 * Check if an instance of class {@link TopicMap} is associated with this factory class, and if it could be used (written, read etc.)
	 * @param tm - instance of class {@link TopicMap}
	 * @return <code>true</code>: if the instance of class {@link TopicMap} could be found (linked with a file map via this factory class), otherwise <code>false</code>
	 */
	public boolean contains(TopicMap tm) {
		if (this.getTopicMaps().contains(tm)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if a file is already in use with thie factory class. Important, if you do not want to overwrite a topic map
	 * @param file - check if the parameter file does exist (linked with a topic map)
	 * @return <code>true</code>: if the file could be found (linked with a topic map via this factory class), otherwise <code>false</code>
	 */
	public boolean contains(File file) {
		for (TopicMap tm : this.getTopicMaps()) {
			if (this.getFile(tm) == file) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Getter method for returning all existing topic maps, which are associated with this factory class (in use, written down, read currently)
	 * @return instances of class {@link TopicMap} which are currently in use with this factory class
	 * @see #getFile(TopicMap)
	 */
	public Collection<TopicMap> getTopicMaps() {
		return topicmaps.keySet();
	}
	
	/**
	 * Getter method for getting the file associated with the assigned topic map
	 * 
	 * @param tm - an instance of class {@link TopicMap}, which is associated with the searched file
	 * @return an instance of class {@link File} if the assigned topic map is associated and if it exists. Otherwise <code>null</code> will be returned.
	 */
	public File getFile(TopicMap tm) {
		return topicmaps.get(tm);
	}
	
	/**
	 * Getter method, to get the topic map associated with the file acting as key.
	 * 
	 * @param file - file which is associated with a topic map
	 * @return the topic map instance which is associated with the file, otherwise return <code>null</code>.
	 */
	public TopicMap getTopicMap(File file) {
		for (Map.Entry<TopicMap, File> e : topicmaps.entrySet()) {
			if (e.getValue() == file) {
				return e.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Private method, which returns an instance of class {@link TopicMapSystem}
	 * . This instance is required to create new topic maps or link already
	 * existing topic maps. These topicmaps will extended by class
	 * {@link TopicMap}.
	 * 
	 * @throws FactoryConfigurationException
	 *             If the TopicMapEngine and the connected TopicMapSystem could
	 *             not be created
	 * 
	 * @return an instance of class {@link TopicMapSystem}
	 */
	private TopicMapSystem getTopicMapSystem() throws FactoryConfigurationException {
		/*
		 * Setting up required objects for a TopicMap creation.
		 */
		if (TMSystem == null) {
			try {
				TMSystem = TopicMapSystemFactory.newInstance().newTopicMapSystem();
			} catch (TMAPIException e) {
				log.error("Unable to create an instance of class "
						+ TopicMapEngine.class.getSimpleName() + ". "
						+ TopicMapSystem.class.getSimpleName()
						+ " instance could not be created: " + e.getMessage());
				throw new FactoryConfigurationException(e);
			}
		}

		return TMSystem;
	}

}
