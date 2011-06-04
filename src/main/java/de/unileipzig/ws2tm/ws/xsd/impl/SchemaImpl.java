package de.unileipzig.ws2tm.ws.xsd.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.unileipzig.ws2tm.WebService2TopicMapFactory;
import de.unileipzig.ws2tm.exception.NotImplementedException;
import de.unileipzig.ws2tm.ws.xsd.Element;
import de.unileipzig.ws2tm.ws.xsd.Restriction;
import de.unileipzig.ws2tm.ws.xsd.Schema;
import de.unileipzig.ws2tm.ws.xsd.SchemaParser;
import de.unileipzig.ws2tm.ws.xsd.Type;
import de.unileipzig.ws2tm.ws.xsd.Type.All;
import de.unileipzig.ws2tm.ws.xsd.Type.Choice;
import de.unileipzig.ws2tm.ws.xsd.Type.Sequence;

public class SchemaImpl implements Schema {

	private static Logger log = LogManager.getLogger(SchemaImpl.class);
	
	private static final String NS_XS = WebService2TopicMapFactory.NS_XSD;
	
	private static final QName currentElement = new QName(NS_XS,"Element");
	private static final QName currentType = new QName(NS_XS,"Type");
	
	private final Document document;
	
	private final HashMap<String, String> namespaces;
	
	private final String tns;
	
	/**
	 * This 
	 */
	private final String ns;
	
	/**
	 * 
	 */
	private HashMap<QName, Element> elements = new HashMap<QName, Element>();
	/**
	 * 
	 */
	private HashMap<QName, Type> types = new HashMap<QName, Type>();
			
	public SchemaImpl(Document dom, String targetNameSpace) {
		document = dom;
		this.namespaces = new HashMap<String,String>();
		int i = 0;
		org.w3c.dom.Element node = dom.getDocumentElement();
		System.out.println(node.getNodeName());
		if (node.hasAttributes()) {
			for (Node att = node.getAttributes().item(i++); att != null; att = node.getAttributes().item(i++)) {
				QName q = this.createQName(att.getNodeName(), att.getNodeValue());
				this.namespaces.put(q.getPrefix(), q.getNamespaceURI());
			}
		}
		String tns = this.namespaces.get("tns");
		if (tns == null) {
			this.tns = targetNameSpace;
		} else {
			this.tns = tns;
		}
		
		String ns = this.namespaces.get("-");
		if (ns == null) {
			this.ns = tns;
		} else {
			this.ns = ns;
		}
		log.info("Parsing XML Schema with namespace "+this.tns);

		//TODO Xerces 2.0.2 supports only the following two lines...
//		dom.getDomConfig().setParameter("create-cdata-nodes", false);
//		dom.getDomConfig().setParameter("comments", false);
				
		this.checkNodeList(dom.getElementsByTagNameNS(NS_XS, "include"));
		this.checkNodeList(dom.getElementsByTagNameNS(NS_XS, "element"));
		this.checkNodeList(dom.getElementsByTagNameNS(NS_XS, "complexType"));
		this.checkNodeList(dom.getElementsByTagNameNS(NS_XS, "simpleType"));
		
		this.getElements().remove(currentElement);
		this.getTypes().remove(currentType);

	}
	
	@Override
	public Element getElement(QName qname) {
		if (elements.containsKey(qname)) {
			return elements.get(qname);
		}
		return null;
	}

	@Override
	public Type getType(QName qname) {
		if (types.containsKey(qname)) {
			return types.get(qname);
		}
		return null;
	}

	@Override
	public Element addElement(QName qname, Type type) {
		Element e = new Element(qname, type.getQName());
		this.elements.put(qname, e);
		return e;
	}

	@Override
	public Type addType(QName qname) {
		Type t = this.getType(qname);
		if (t != null) {
			return t;
		} else {
			t = new Type(qname);
			this.addType(qname, t);
			return t;
		}
	}

	@Override
	public Collection<Element> getElements() {
		return this.elements.values();
	}

	@Override
	public Collection<Type> getTypes() {
		return this.types.values();
	}

	@Override
	public String getTargetNameSpace() {
		return this.tns;
	}
	
	public String getNameSpace() {
		return this.ns;
	}

	private void addType(QName q, Type t) {
		this.types.put(q, t);
	}
	
	private void setCurrentType(Type t) {
		this.addType(currentType, t);
	}
	
	private Type getCurrentType() {
		Type c = this.getType(currentType);
		if (c == null) {
			c = new Type();
			this.addType(currentType, c);
		}
		return c;
	}
	
	private void addElement(QName q, Element e) {
		this.elements.put(q, e);
	}
	
	private void setCurrentElement(Element e) {
		this.addElement(currentElement, e);
	}
	
	private Element getCurrentElement() {
		Element e = this.getElement(currentElement);
		if (e == null) {
			e = new Element();
			this.addElement(currentElement, e);
		}
		return e;
	}
	
	/**
	 * This method gets the required information how often an element, sequence,
	 * choice or whatever may occur. It returns an array of two elements. The first
	 * integer defines the mininum number, the second defines the maximum number of occurrences.
	 * If the min- and max- information of an element could not be found. Then the default
	 * values will be used (minimum: 0, maximum: 1)
	 * 
	 * @param node - The node, which could contain min- and maxOccurs information
	 * @return an array of integers, with exactly two elements. The first elements describes the
	 * minimum number, the second the maximum number how often an element (element, sequence, choice
	 * etc.) may occur.
	 */
	private int[] getMinMaxOccurringInformation(Node node) {
		Node minOcc = node.getAttributes().getNamedItem("minOccurs");
		Node maxOcc = node.getAttributes().getNamedItem("maxOccurs");
		int min = 0;
		int max = 1;
		
		// evaluate minium occurrences
		if (minOcc != null && minOcc.getNodeValue().length() > 0) {
			min = Integer.parseInt(minOcc.getNodeValue());
		}
		// evaluate maximum occurrences
		if (maxOcc != null && maxOcc.getNodeValue().length() > 0) {
			try {
				max = Integer.parseInt(maxOcc.getNodeValue());
			} catch (NumberFormatException e) {
				log.info("The maximum number of occurrences is set to be \"unbounded\". However, it will be limited to 999 elements to process the information further.");
				max = 999;
			}
		}
		
		// correct max occ to 1
		if (max <= 0) {
			log.warn("The maximum number of occurrences does not seem correct. It was corrected to be at least 1. Recognized value is: "+max);
			max = 1;
		}		
		
		return new int[]{min, max};
	}
	
	/**
	 * @param node
	 * @return
	 */
	private QName createQName(Node node) {
		Node name = node.getAttributes().getNamedItem("name");
		Node ref = node.getAttributes().getNamedItem("ref");
		if (name != null && name.getNodeValue().length() > 0) {
			return this.createQName(name.getNodeValue());
		} else if (ref != null && ref.getNodeValue().length() > 0) {
			return this.createQName(ref.getNodeValue());
		}
		return null;
	}

	/**
	 * @param qname
	 * @return
	 */
	private QName createQName(String qname) {
		QName q = null;
		if (qname.contains(":")) {
			String[] arr = qname.split(":");
			if (arr.length == 2) {
				q = new QName(this.document.lookupNamespaceURI(arr[0]), arr[1], arr[0]);
			}
		} else if (qname.contains("/")) {
			String[] arr = qname.split("/");
			StringBuffer ns = new StringBuffer();
			for (int i = 0; i < arr.length-1; i++) {
				ns.append(arr[i]+"/");
			}
			q = new QName(ns.toString(), arr[arr.length-1], this.document.lookupPrefix(ns.toString()));
		} else {
			q = new QName(this.getNameSpace(), qname);
		}
		return q;
	}

	private QName createQName(String nodeName, String nodeValue) {
		QName q = null;
		if (!nodeName.startsWith("xmlns")) {
			if (nodeName.equalsIgnoreCase("targetnamespace")) {
				q = new QName(nodeValue, "undefined", "tns");
			} else {
				log.debug("Found attribute: "+nodeName+"=\""+nodeValue+"\". Will be ignored.");
			}
		}
		if (nodeName.contains(":")) {
			q = new QName(nodeValue, "undefined", nodeName.split(":")[1]);
		} else {
			q = new QName(nodeValue, "undefined", "-");
		}
		return q;
	}
	
	private String getNodeName(Node node) {
		String name = new String();
		if (node.getLocalName() != null) {
			name = node.getLocalName();
		}
		return name;
	}
	
	/**
	 * @param list
	 */
	private void checkNodeList(NodeList list) {
		if (list.getLength() == 0) {
			log.debug("Found node list contains 0 elements.");
			return;
		}
		log.debug("Found node list with "+list.getLength()+" elements.");
		
		for (int i = 0; i < list.getLength(); i++) {
			followNode(list.item(i));
		}
	}

	/**
	 * @param node
	 */
	private void followNode(Node node) {
		if (node != null) {
			this.checkNode(node);
			if (node.hasChildNodes()) {
				NodeList list = node.getChildNodes();
				for (int i=0; i < list.getLength(); i++) {
					this.followNode(list.item(i));
				}
			}
		}
	}
	
	/**
	 * @param node
	 */
	private void checkNode(Node node) {
		String name = node.getLocalName();
		if (name == null || name.length() == 0) {
			// IGNORE nodes, which do not have a local name specified. These are not important at all and will procude errors for sure
			return;
		}
		
		log.debug("Checking node: "+name);
		
		if (name.equalsIgnoreCase("INCLUDE")) {
			// INCLUDE ############################## BEGIN
			this.addNodeInformationInclude(node);
		}
		if (name.equalsIgnoreCase("ELEMENT")) {
			// ELEMENT ############################## BEGIN
			this.addNodeInformationElement(node);
		} else if (name.equalsIgnoreCase("COMPLEXTYPE") || name.equalsIgnoreCase("SIMPLETYPE")) {
			/*
			 * it does not matter if the type definiton is a complex or simple one. 
			 * Because of design reasons both are organized in one type of class {@link Type}
			 */
			this.addNodeInformationType(node);
		} else if (name.equalsIgnoreCase("ALL")) {
			/*
			 * ALL ################ BEGIN
			 */
			this.addNodeInformationAll(node);
		} else if (name.equalsIgnoreCase("ATTRIBUTE")) {
			/*
			 * ATTRIBUTE ########## BEGIN
			 */
			this.addNodeInformationAttribute(node);
			/*
			 * ATTRIBUTE ########## END
			 */
		} else if (name.equalsIgnoreCase("ATTRIBUTEGROUP")) {				
			/*
			 * ATTRIBUTEGROUP ##### BEGIN
			 */
			this.addNodeInformationAttributeGroup(node);
		} else if (name.equalsIgnoreCase("CHOICE")) {				
			/*
			 * CHOICE ############# BEGIN
			 * only one of the elements
			 */
			this.addNodeInformationChoice(node);
		} else if (name.equalsIgnoreCase("EXTENSION")) {				
			/*
			 * EXTENSION ########## BEGIN
			 * elements of this type has to added to the currentType
			 */
			this.addNodeInformationExtension(node.getAttributes().getNamedItem("base"));
		} else if (name.equalsIgnoreCase("ENUMERATION")) {				
			/*
			 * ENUMERATION ######## BEGIN
			 */
			this.addNodeInformationEnumeration(node.getAttributes().getNamedItem("value"));
		} else if (name.equalsIgnoreCase("GROUP")) {			
			/*
			 * GROUP ############## BEGIN
			 */
			this.addNodeInformationGroup(node);
		} else if (name.equalsIgnoreCase("LIST")) {				
			/*
			 * LIST ############### BEGIN
			 * requires an itemtype Attribute (refereing to a simple type) or containing a simple type definition
			 * the result would be something like <node>1 2 3 4 5</node>.
			 */
			this.addNodeInformationList(node);
		} else if (name.equalsIgnoreCase("RESTRICTION")) {
			// RESTRICTION ######## BEGIN
			this.addNodeInformationRestriction(node);
		} else if (name.equalsIgnoreCase("SEQUENCE")) {				
			// SEQUENCE ########### BEGIN
			this.addNodeInformationSequence(node);
		} else if (name.equalsIgnoreCase("UNION")) {				
			// UNION ############## BEGIN
			this.addNodeInformationUnion(node);
		}
		
	}
	
	/**
	 * @param node
	 * @return
	 */
	private All addNodeInformationAll(Node node) {
		int[] minmax = this.getMinMaxOccurringInformation(node);
		All all = this.getCurrentType().addAll(minmax[0], minmax[1]);
		
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			// The list type ALL can contain only instances of element, therefore the qualified name is more than enough
			if (this.getNodeName(child).equalsIgnoreCase("element")) {
				minmax = this.getMinMaxOccurringInformation(child);
				all.addElement(this.createQName(child), minmax[0], minmax[1]);
			}
		}		
		return all;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private Type addNodeInformationAttribute(Node node) {
		Node attgrp = node.getParentNode();
		Type atts = null;
		QName q;
		if (attgrp != null && this.getNodeName(attgrp).equalsIgnoreCase("ATTRIBUTEGROUP")) {				
			// ATTRIBUTEGROUP ##### BEGIN
			Node name = attgrp.getAttributes().getNamedItem("name");
			if (name != null && name.getNodeValue().length() > 0) {
				q = this.createQName(name.getNodeValue());
				atts = this.getType(q);
				if (atts == null) {
					atts = new Type(q);
					this.addType(q, atts);
				}
			}
		}
		// Attributes will be handled like elements 
		Element e = new Element();
		int i = 1;
		for (Node att = node.getAttributes().item(0); att != null;att = node.getAttributes().item(i++)) {
			String name = this.getNodeName(att);
			if (name.equalsIgnoreCase("name")){
				e.setQName(this.createQName(att.getNodeValue()));
			} else if (name.equalsIgnoreCase("type")) {
				e.setType(this.createQName(att.getNodeValue()));
			}
		}
		this.getCurrentType().addAttribute(e);
		
		if (atts != null) {
			atts.addAttribute(e);
		}
		return atts;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private Type addNodeInformationAttributeGroup(Node node) {
		Node ref = node.getAttributes().getNamedItem("ref");
		Type attgrp = null;
		if (ref != null) {
			attgrp = this.getType((this.createQName(ref.getNodeValue())));
			if (attgrp != null) {
				this.getCurrentType().addAttributes(attgrp.getAttributesQName());
			}
		} else {
			Node name = node.getAttributes().getNamedItem("name");
			if (name != null && name.getNodeValue().length() > 0) {
				attgrp = new Type(this.createQName(name.getNodeValue()));
			}
		}
		if (attgrp == null) {
			throw new NotImplementedException("");
		}
		return attgrp;
	}

	/**
	 * @param node
	 * @return
	 */
	private Choice addNodeInformationChoice(Node node) {
		
		int[] minmax = this.getMinMaxOccurringInformation(node);
		Choice choice = this.getCurrentType().addChoice(minmax[0], minmax[1]);
		
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			String name = this.getNodeName(child);
			if (name.equalsIgnoreCase("element")) {
				int[] minmaxEle = this.getMinMaxOccurringInformation(child);
				choice.addElement(this.createQName(child), minmaxEle[0], minmaxEle[1]);
				// the tree will be checked further, therefore no need to address elements here already
			} else if (name.equalsIgnoreCase("sequence")) {
				int[] minmaxSeq = this.getMinMaxOccurringInformation(child);
				choice.addSequence(minmaxSeq[0], minmaxSeq[1]);
			} else if (name.equalsIgnoreCase("choice")) {
				int[] minmaxSeq = this.getMinMaxOccurringInformation(child);
				choice.addChoice(minmaxSeq[0], minmaxSeq[1]);
			}
		}
		return choice;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private Element addNodeInformationElement(Node node) {
		// save the current misplaced element to its rightful place
		Element e = this.getCurrentElement();
		this.elements.put(e.getQName(), e);					
		
		e = new Element();
		Node ref = node.getAttributes().getNamedItem("ref");
		if (ref != null && ref.getNodeValue().length() > 0) {
			QName q = this.createQName(ref.getNodeValue());
			if (this.elements.containsKey(q)) {
				e = this.elements.get(q);
			} else {
				e.setQName(q);
				this.elements.put(q, e);
			}
		} else {
			Node name = node.getAttributes().getNamedItem("name");
			if (name != null && name.getNodeValue().length() > 0) {
				e.setQName(this.createQName(name.getNodeValue()));				
				log.debug("Current element gets name assigned: "+name.getNodeValue());
			} else {
				throw new UnsupportedOperationException("An element definition without a qualified name is not supported, and will be not supported depending on the XML schema.");
			}
			
			if (node.hasChildNodes()) {
				this.setCurrentElement(e);
			}
			
			Node type = node.getAttributes().getNamedItem("type");
			if (type != null && type.getNodeValue().length() > 0) {
				QName q = this.createQName(type.getNodeValue());
				Type t = this.getType(q);
				if (t == null) {
					Type impl = new Type(q);
					this.addType(q, impl);
				}
				e.setType(q);
				log.debug("Current element follows type definition: "+q);
			} else {
				// an inner type definition has to follow. -> #checkNode(Node) will trigger addNodeInformationType(Node)
			}
		}
		
		return e;
	}

	/**
	 * Functions adds enumeration values to the current type and its {@link Restriction}
	 * @param node - the attribute "value" of the element enumeration with its content
	 */
	private void addNodeInformationEnumeration(Node value) {
		if (value != null && value.getNodeValue().length() > 0) {
			this.getCurrentType().getRestriction().addEnumeration(value.getNodeValue());
		}
	}

	/**
	 * Function extends the current type with the information which can be found through 
	 * the assigned node or attribute node base.
	 * @param base - the base attribute of an extension element which points to another instance of class {@link Type}
	 * @return the extended instances of class {@link Type}. If the an other type could not be found, then a reference will be 
	 * set to the type, which information should be used. The reference will be used later.
	 */
	private Type addNodeInformationExtension(Node base) {
		Type type = null;
		if (base != null && base.getNodeValue().length() > 0) {
			QName q = this.createQName(base.getNodeValue());
			type = this.getCurrentType();
			Type t = this.getType(q);
			if (t != null) {
				type.extend(t);
			} else {
				type.setReference(q);
			}
		} else {
			throw new NotImplementedException("Received extension node, but the base attribute is empty or does not exist.");
		}
		return type;
	}

	/**
	 * @param node
	 * @return instance of class {@link Type} containing the information of the Document {@link Node} with the localname <i>GROUP</i>.
	 */
	private Element addNodeInformationGroup(Node node) {
		Node ref = node.getAttributes().getNamedItem("ref");
		Element grp;
		if (ref != null && ref.getNodeValue().length() > 0) {
			grp = this.getElement(this.createQName(ref.getNodeValue()));
			int[] minmax = this.getMinMaxOccurringInformation(node);
			if (grp != null) {
				this.getCurrentType().addElement(grp, minmax[0], minmax[1]);
			}
		} else {
			grp = new Element();
			int i = 1;
			for (Node att = node.getAttributes().item(0); att != null; att = node.getAttributes().item(i++)) {
				if (att.getNodeName().equalsIgnoreCase("name")){
					grp.setQName(this.createQName(att.getNodeValue()));
				}
			}
			this.setCurrentElement(grp);
		}
		return grp;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private Schema addNodeInformationInclude(Node node) {
		Node schemaLocation = node.getAttributes().getNamedItem("schemaLocation");
		if (schemaLocation != null && schemaLocation.getNodeValue().length() > 0){
			try {
				return SchemaParser.getFactory().addSchema(schemaLocation.getNodeValue());
			} catch (IllegalArgumentException e) {
				log.warn("Linked Schema could not be retrieved, and therefore only existing information can be used.",e);
			} catch (DOMException e) {
				log.warn("Linked Schema could not be retrieved, and therefore only existing information can be used.",e);
			} catch (IOException e) {
				log.warn("Linked Schema could not be retrieved, and therefore only existing information can be used.",e);
			}
		}
		return null;
	}

	/**
	 * @param node
	 * @return
	 * @throws NotImplementedException 
	 */
	private Type addNodeInformationList(Node node) {
		Node itemType = node.getAttributes().getNamedItem("itemType");
		if (itemType != null && itemType.getNodeValue().length() > 0) {
			QName q = this.createQName(itemType.getNodeValue());
			Type type = this.getType(q);
			if (type != null) {
				this.getCurrentType().setListItemType(type.getQName());
			} else {
				type = new Type(q);
				this.getCurrentType().setListItemType(type.getQName());
				this.addType(q, type);
			}
			return type;
		} else {
			throw new NotImplementedException("The method addNodeInformationList does not support XML Schema elements other than the element <list itemType=\"uriToSimpleType\" />");
		}
	}

	/**
	 * @param node
	 */
	private Restriction addNodeInformationRestriction(Node node) {
		Node base = null;
		if (node.hasAttributes()) {
			base = node.getAttributes().getNamedItem("base");
		}
		Restriction res = null;
		if (base != null && base.getNodeValue().length() > 0) {
			res = new Restriction(this.createQName(base.getNodeValue()));

			
			int start = 0, stop = 0;
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				String name = this.getNodeName(child);
				Node value = null;
				if (child.hasAttributes()) {
					value = child.getAttributes().getNamedItem("value");
				}
				if (name.equalsIgnoreCase("minInclusive")) {
					if (value != null && value.getNodeValue().length() > 0) {
						start = Integer.parseInt(value.getNodeValue());
					}
				} else if(name.equalsIgnoreCase("maxInclusive")) {
					if (value != null && value.getNodeValue().length() > 0) {
						stop = Integer.parseInt(value.getNodeValue());
					}							
				}
			}
			if (stop > start) {
				int count = start;
				while (count < stop) {
					res.addEnumeration(String.valueOf(count));
					count++;
				}
			} else {
				for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
					if(this.getNodeName(child).equalsIgnoreCase("pattern")) {
						if (child.hasAttributes()) {
							Node value = child.getAttributes().getNamedItem("value");
							if (value != null && value.getNodeValue().length() > 0) {
								res.setPattern(value.getNodeValue());
							}
						}
					}
				}
			}
		}
		
		this.getCurrentType().setRestriction(res);
		
		return res;
	}
	
	/**
	 * @param node
	 * @return
	 */
	private Type addNodeInformationSequence(Node node) {
		Type c = this.getCurrentType();

		int[] minmax = this.getMinMaxOccurringInformation(node);
		Sequence seq = c.addSequence(minmax[0], minmax[1]);
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			String name = this.getNodeName(child);
			if (name.equalsIgnoreCase("element")) {
				int[] minmaxEle = this.getMinMaxOccurringInformation(child);
				seq.addElement(this.createQName(child), minmaxEle[0], minmaxEle[1]);
				// the tree will be checked further, therefore no need to address elements here already
			} else if (name.equalsIgnoreCase("sequence")) {
				int[] minmaxSeq = this.getMinMaxOccurringInformation(child);
				seq.addSequence(minmaxSeq[0], minmaxSeq[1]);
			} else if (name.equalsIgnoreCase("choice")) {
				int[] minmaxSeq = this.getMinMaxOccurringInformation(child);
				seq.addChoice(minmaxSeq[0], minmaxSeq[1]);
			}
		}
		
		return c;
	}

	/**
	 * @param node
	 * @return
	 */
	private Type addNodeInformationType(Node node) {
		Type t = this.getCurrentType();
		if (t.getQName() != null) {
			this.addType(t.getQName(), t);					
		}		
		
		Node name = node.getAttributes().getNamedItem("name");
		Node element = node.getParentNode();
		if (name != null && name.getNodeValue().length() > 0) {
			t = new Type(this.createQName(name.getNodeValue()));
		} else if (element != null && element.getLocalName().equalsIgnoreCase("element")) {
			QName q = new QName(this.getTargetNameSpace(),this.getCurrentElement().getName()+"AnonymousTypeDefinition");
			t = new Type(q);
			this.getCurrentElement().setType(t);
			log.debug("Current Element follows type definition "+q);
		} else {
			/*
			 * a simple type may occur after a LIST without itemType attribute
			 * or RESTRICTION without base attribute. Further more if an attribute
			 * follows a type defintion, which is included in a different xml schema
			 * than the parent node if this type definition will be the attribute, and
			 * not the parent of the attribute node.
			 * therefore this kind of if-else should be ignored.
			 */
		}
		
		this.setCurrentType(t);
		
		return t;
	}
	
	//TODO add some life to this function
	private Type addNodeInformationUnion(Node node) {
		Type t = this.getCurrentType();
		
		return t;
	}

}