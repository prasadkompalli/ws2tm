package de.unileipzig.ws2tm.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;

public class WebServiceConnectorTest {

	@Test
	public void connectToJamCA() throws IOException {
		WebServiceConnector wsc = WebServiceConnector.newConnection(new URL("http://www.jam.ca"));
	}
	
	@Test
	public void connectToAmazonWS() throws IOException {
		WebServiceConnector wsc = WebServiceConnector.newConnection(new URL("https://ecs.amazonaws.com/onca/soap?Service=AWSECommerceService"));
	}
	
	
}
