/**
 * 
 */
package de.unileipzig.ws2tm.util;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import de.unileipzig.ws2tm.ws.xsd.Schema;
import de.unileipzig.ws2tm.ws.xsd.SchemaParser;

/**
 * @author Torsten Grigull
 * @version 0.1 
 */
public class XSDParserTest {
	
	@Test
	public void SchemaParser() throws IOException {
			URL url = new URL("file:///Users/torsten/Documents/eclipse-workspace/me.master.thesis/xsd/DWML.xsd");
			Schema s = SchemaParser.getFactory().addSchema(url);
/*			Iterator<Element> it = s.getElements();
			while (it.hasNext()) {
				Element e = it.next();
				System.out.println("Element "+e.getName());
				QName q = e.getType().getType();
				System.out.println("\t"+q.getNamespaceURI());
				System.out.println("\t"+q.getLocalPart());
				System.out.println("\t"+q.getPrefix());
				System.out.println();
			}
			Iterator<Type> t = s.getTypes();
			while (t.hasNext()) {
				Type type = t.next();
				QName q = type.getType();
				System.out.println("Type "+q.getLocalPart());
				System.out.println("\t"+q.getNamespaceURI());
				System.out.println("\t"+q.getPrefix());
				System.out.println();
			}
*/
	}
	
}
