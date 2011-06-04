/**
 * 
 */
package de.unileipzig.ws2tm;

import java.io.File;
import java.io.IOException;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmapi.core.FactoryConfigurationException;
import org.tmapi.core.TopicMap;
import org.w3c.dom.DOMException;

import de.unileipzig.ws2tm.exception.InitializationException;
import de.unileipzig.ws2tm.request.TMQLRequest;
import de.unileipzig.ws2tm.tm.TopicMapAccessObject;
import de.unileipzig.ws2tm.tm.factory.TopicMapEngine;
import de.unileipzig.ws2tm.ws.soap.Operation;
import de.unileipzig.ws2tm.ws.soap.Parameter;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/01/17)
 *
 */
public class WS2TMTest {

	private static WebService2TopicMap ws2tm;
	
	@BeforeClass
	public static void createWebService() throws DOMException, IOException, InitializationException, FactoryConfigurationException {
		
		ws2tm = WebService2TopicMapFactory.createWebService();
		TopicMapEngine.OVERWRITE = true;
		TopicMapAccessObject wsdl2tm = ws2tm.newWebService("wsdl/weathergov.wsdl");
		TopicMapEngine.newInstance().write(new File("tmp/ws2tm-wsdl2tm.xtm"), wsdl2tm.load(), TopicMapEngine.XTM_2_0);
	}
	
	@Before
	public void requestWebService() throws IOException, InitializationException, FactoryConfigurationException {
		TMQLRequest request = new TMQLRequest();
				
		String NS = "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl";
		String XS = "http://www.w3.org/2001/XMLSchema";
		Operation op = request.addOperation(new QName(NS,"NDFDgenRequest","w"));
		op.addParameter(new Parameter("latitude","w",NS,"40.00",new QName(XS,"decimal")));
		op.addParameter(new Parameter("longitude","w",NS,"40.00",new QName(XS,"decimal")));
		op.addParameter(new Parameter("startTime","w",NS,"2011-06-01T00:00:00",new QName(XS,"dateTime")));
		op.addParameter(new Parameter("endTime","w",NS,"2011-06-02T00:00:00",new QName(XS,"dateTime")));
		op.addParameter(new Parameter("product","w",NS,"glance",new QName(NS,"productType")));
		Parameter p = op.addParameter(new Parameter("weatherParameters","w",NS, null, new QName(NS,"weatherParametersType")));
		p.addParameter(new Parameter("maxt","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("mint","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("temp","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("dew","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("pop12","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("qpf","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("sky","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("snow","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("wspd","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("wdir","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("wx","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("waveh","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("icons","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("rh","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("appt","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("incw34","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("incw50","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("incw64","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("cum34","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("cum50","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("cum64","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("conhazo","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("ptornado","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("phail","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("ptstmwinds","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("pxtornado","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("pxhail","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("pxtstmwinds","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("ptotsvrtstm","w",NS,"True",new QName(XS,"boolean")));
		p.addParameter(new Parameter("pxtotsvrtstm","w",NS,"True",new QName(XS,"boolean")));
		
		TopicMapAccessObject soap2tm = ws2tm.newWebServiceRequest(request);		
		TopicMapEngine.newInstance().write(new File("tmp/ws2tm-soap2tm.xtm"), soap2tm.load(), TopicMapEngine.XTM_2_0);
	}
	
	@Test
	public void mergeTopicMaps() throws FactoryConfigurationException, IOException {
		TopicMap tm = ws2tm.mergeTopicMaps();
		TopicMapEngine.newInstance().write(new File("tmp/ws2tm-merged.xtm"), tm, TopicMapEngine.XTM_2_0);
	}
	
}
