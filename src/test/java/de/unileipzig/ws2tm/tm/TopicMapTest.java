/**
 * 
 */
package de.unileipzig.ws2tm.tm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmapi.core.Construct;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.IdentityConstraintException;
import org.tmapi.core.MalformedIRIException;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapExistsException;

import de.topicmapslab.tmclvalidator.TMCLValidator;
import de.topicmapslab.tmclvalidator.TMCLValidatorException;
import de.topicmapslab.tmclvalidator.ValidationResult;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/01/06)
 *
 */
public class TopicMapTest {

	private static TopicMap tm;

	/**
	 * 
	 */
	public TopicMapTest() {
		// TODO Auto-generated constructor stub
	}

	
	@BeforeClass
	public static void createTopicMap() throws MalformedIRIException, FactoryConfigurationException, IllegalArgumentException, TopicMapExistsException, IOException {
		tm = TopicMapEngine.newInstance().createNewTopicMapInstance(new File("./tmp/topicmap.xtm"), "http://newexamples.org/tmp/");
		TopicMapEngine.OVERWRITE = true;
	}
	
	@Before
	public void createTopics() throws IdentityConstraintException, ModelConstraintException, FactoryConfigurationException {
		Topic topicType = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://psi.topicmaps.org/tmcl/topic-type"));
		Topic occType = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://psi.topicmaps.org/tmcl/occurrence-type"));
		
		Topic msg = tm.createTopicBySubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://schemas.xmlsoap.org/wsdl#message"));
		msg.addType(topicType);
		
		Topic operation = tm.createTopicBySubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://schemas.xmlsoap.org/wsdl#operation"));
		operation.addType(topicType);
		
		Topic input = tm.createTopicBySubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://schemas.xmlsoap.org/wsdl#input"));
		input.addType(topicType);
		
		Topic output = tm.createTopicBySubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://schemas.xmlsoap.org/wsdl#output"));
		output.addType(topicType);
		
		Topic part = tm.createTopicBySubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://schemas.xmlsoap.org/wsdl#part"));
		part.addType(topicType);
		
		
		// GetTradePriceInput
		Topic gtpi = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#GetTradePricesInput"));
		gtpi.addType(msg);
		
		Topic tickerSymbol = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#tickerSymbol"));
		tickerSymbol.addSubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://www.w3.org/2000/10/XMLSchema#string"));
		tickerSymbol.addType(part);
		
		
		Topic timePeriod = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#timePeriod"));
		timePeriod.addType(part);
		
		gtpi.createOccurrence(occType,tickerSymbol.getSubjectIdentifiers().iterator().next().getReference(), msg, part);
		gtpi.createOccurrence(occType,timePeriod.getSubjectIdentifiers().iterator().next().getReference(), msg, part);
		
		// GetTradePriceOutput
		Topic gtpo = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#GetTradePricesOutput"));
		gtpo.addType(msg);
		
		Topic frequence = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#frequence"));
		tickerSymbol.addSubjectLocator(TopicMapEngine.newInstance().createBaseLocator("http://www.w3.org/2000/10/XMLSchema#float"));
		tickerSymbol.addType(part);
		
		Topic result = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#result"));
		timePeriod.addType(part);
		
		gtpi.createOccurrence(occType,result.getSubjectIdentifiers().iterator().next().getReference(), msg, part);
		gtpi.createOccurrence(occType,frequence.getSubjectIdentifiers().iterator().next().getReference(), msg, part);
		
		// GetLastTradePrice (Operation)
		
		Topic gltp = tm.createTopicBySubjectIdentifier(TopicMapEngine.newInstance().createBaseLocator("http://example.com/stockquote.wsdl#GetLastTradePrice"));
		gltp.addType(operation);
		
		gltp.createOccurrence(occType, gtpi.getSubjectIdentifiers().iterator().next().getReference(), operation, input);
		gltp.createOccurrence(occType, gtpo.getSubjectIdentifiers().iterator().next().getReference(), operation, output);
		
		try {
			TopicMapEngine.newInstance().write(tm);
		} catch (IOException e) {
			System.out.println("Could not write the current topic map. See stack trace for more information.");
			e.printStackTrace();
		}
		
	}

	@Test
	public void validateTopicMap() throws TMCLValidatorException {
		TMCLValidator validator = new TMCLValidator();
        Map<Construct, Set<ValidationResult>> invalidConstructs = validator.validate(tm);

        // plot result

        if (invalidConstructs.entrySet().size() > 0) {
	        for(Map.Entry<Construct, Set<ValidationResult>> invalidConstruct:invalidConstructs.entrySet()){
	
	                System.out.println("Invalid construct " + invalidConstruct.getKey() + ":");
	                for(ValidationResult result:invalidConstruct.getValue()){
	                        System.out.println("Constraint " + result.getConstraintId() + " violated:");
	                        System.out.println(result.getMessage());
	                }
	        }
        } else {
        	System.out.println("Topic map is valid. Tested with TopicMap Constraint Language Validator 1.3.0-SNAPSHOT.");
        }
	}
	
}
