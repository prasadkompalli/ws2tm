/**
 * 
 */
package de.unileipzig.ws2tm.persistence;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <b>Interface DataAccessObject</b>
 * <p>
 * This interface keeps track of every implementation or extension of an complex
 * data access object. These DAOs describe an interface between the data, which
 * should be accessed and other objects, which wants to use the data. A typical
 * DAO would be class {@link ContextSpecification}. It controls the access
 * between a configuration file, which describes a language specification. This
 * specifications controls the behaviour of the underlaying program.
 * </p>
 * <p>
 * Every DAO should implement or extend this interface for a controlled usage in
 * this package.
 * </p>
 * 
 * @author Torsten Grigull
 * @version 0.1 2010/08/12
 * 
 * @see ContextSpecification
 */
public interface DataAccessObject {

	/**
	 * <b>create:</b> create a new instance of class {@link DataAccessObject}.
	 * This method returns only a new object. It does not write it to file.
	 * 
	 * @return an instance of class {@link DataAccessObject}. Depending on the class which implements this interface.
	 */
	public Object create();

	/**
	 * This method is required for a JAXB context.
	 * 
	 * @param obj - interface or implementation of JAXB implementation, which will be
	 *            transformed to an instance of the JAXB implementation class, which can be accessed through
	 *            the JAXB module.
	 * @return an instance of class {@link DataAccessObject}. Depending on the class which implements this interface.
	 * @throws IllegalArgumentException if the assigned parameter points to null
	 */
	public Object create(Object obj);
	
	/**
	 * <b>delete:</b> delete a data access object. This method deletes a DAO
	 * depending on the specified key, which identifies the DAO.
	 * 
	 * @param key - key, which specifies the data access object
	 */
	public void delete(String key);

	/**
	 * <b>load:</b> This method is a standard method for loading data out of an
	 * {@link DataAccessObject}.
	 * 
	 * @param key - File or Identifier of the DAO, which should be loaded.
	 * @return an instance of class {@link DataAccessObject}, which has access to the contained data.
	 * @throws IOException
	 *             if the specified file could not be loaded
	 * @throws FileNotFoundException
	 *             if the file or source, which is the sum of a directory and
	 *             specified key, could not be found because it does not exist.
	 */
	public Object load(String key) throws IOException, FileNotFoundException;

	/**
	 * <b>save:</b> This method is a standard method for saving data to a
	 * {@link DataAccessObject}. The source or target is specified by evaluating all
	 * existing objects and their associated file paths or file objects.
	 * 
	 * Therefore only objects will be saved which are associated with a file or a file path.
	 * Objects with are not connected through any setting with a file, can be saved using
	 * the methods {@link #save(Object, String)}.
	 * @throws IOException
	 *             if an error occurrs while saving an existing object with its associated file.
	 * 
	 * @see #save(String)
	 */
	public void save() throws IOException;
	
	/**
	 * <b>save:</b> This method is a standard method for saving data to a
	 * {@link DataAccessObject}. The source or target is specified by assigning
	 * the parameter <code>key</code>.
	 * 
	 * @param key
	 *            - key, which describes an instance of class
	 *            {@link DataAccessObject}, e.g. DataAccessObject.class. For
	 *            files e.g. <code>key</code> should be a directory. Files will
	 *            be written in the implementation itself. Otherwise DAO will be
	 *            written over and over in the same file (overwriting theyself),
	 *            because only one file was specified.
	 * @throws IllegalArgumentException
	 *             if the specified key points to null
	 * @throws FileNotFoundException 
	 * 				if the assigned file could not be found at did not point to any saved object.
	 * 				This means that no object could be saved or written to the assigned value.
	 * @throws IOException
	 *             if the specified file could not be written because of a wrong
	 *             format or other I/O errors.
	 */
	public void save(String key) throws IOException, FileNotFoundException;

	/**
	 * <b>save:</b>: This method is a standard method for saving data out of an
	 * {@link DataAccessObject} to the linked data source. It depends on the
	 * implementation, however, this method should be intended for writing a
	 * specified object to the file or source, which is assigned by the
	 * parameter <code>key</code>.
	 * 
	 * @param obj
	 *            - data access object, which should be saved to the specified
	 *            file, which is the other assigned parameter
	 * @param key
	 *            - key, which describes the target or source of a
	 *            {@link DataAccessObject}, e.g. a configuration file.
	 *            Other than method {@link #save(String)}. This parameter
	 *            should describe a file, if you want to write a file or
	 *            an sql connection address with table information.
	 * @throws IllegalArgumentException
	 *             if the specified object or key points to null
	 * @throws IOException
	 *             if the specified file could not be saved
	 */
	public void save(Object obj, String key) throws	IOException;
}
