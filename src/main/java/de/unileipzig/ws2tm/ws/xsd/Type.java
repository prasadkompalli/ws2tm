package de.unileipzig.ws2tm.ws.xsd;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

/**
 * This class abstract type definition which can be complex or simple. However, this aspect
 * is not of interest.
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/20)
 *
 */
public class Type {
	
	private static Logger log = Logger.getLogger(Type.class);

	public static final int ELEMENT = 0;
	public static final int SEQUENCE = 1;
	public static final int CHOICE = 2;
	public static final int ALL = 3;
	
	private int seqCount = 0;
	private int choiCount = 0;
	
	private QName name;
	private boolean hasAnnotation;
	
	/**
	 * @see {@link Restriction}
	 */
	private Restriction restriction;
	/**
	 * like an extension, however, if the type definition does not exist, the reference will be saved here, and used later.
	 */
	private QName reference;
	/**
	 * the children of this instance of class {@link Type}
	 */
	private HashMap<ListElement, OccurringDefinition> elements = new HashMap<ListElement, OccurringDefinition>();
	private List<QName> attributes = new ArrayList<QName>();
	
	private Type itemType;
	
	public Type() {
		log.debug("Created new datatype.");
	}

	public Type(QName name) {
		this.setQName(name);
		log.debug("Created new datatype "+name.toString());
	}

	public boolean hasReference() {
		if (this.getReference() != null) {
			return true;
		}
		return false;
	}
	
	public boolean hasAnnotation() {
		return this.hasAnnotation;
	}

	public boolean hasChildren() {
		if (this.getElementsQName().size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean hasAttributes() {
		if (this.getAttributesQName().size() > 0) {
			return true;
		}
		return false;
	}

	public boolean hasRestriction() {
		if (this.getRestriction() != null) {
			return true;
		}
		return false;
	}

	public boolean hasEnumeration() {
		if (this.hasRestriction() && this.getRestriction().hasEnumeration()) {
			return true;
		}
		return false;
	}
	
	public boolean hasSequence() {
		for (ListElement e : this.getListElements()) {
			if (e.getListElementType() == Type.SEQUENCE) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasChoice() {
		for (ListElement e : this.getListElements()) {
			if (e.getListElementType() == Type.CHOICE) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAll() {
		for (ListElement e : this.getListElements()) {
			if (e.getListElementType() == Type.ALL) {
				return true;
			}
		}
		return false;
	}
	
	public void extend(Type type) {
		if (type.hasChildren()) {
			this.addElements(type.getElementsQName());
		}
		
		if (type.hasAttributes()) {
			this.addAttributes(type.getAttributesQName());
		}
		
		if (type.hasRestriction()) {
			this.setRestriction(type.getRestriction());
		}
		if (type.hasEnumeration()) {
			this.getRestriction().addEnumerations(type.getRestriction().getEnumerations());
		}
	}

	/**
	 * @param newQName
	 */
	public void setQName(QName newQName) {
		this.name = newQName;
	}

	/**
	 * @return
	 */
	public QName getQName() {
		return this.name;
	}

	public Sequence addSequence(int minOccurs, int maxOccurs) {
		if (this.hasAll()) {
			throw new UnsupportedOperationException("This type contains already an element ALL and its children. Other children of type Element, Sequence or Choice cannot be added.");
		}
		
		Sequence s = new Sequence(minOccurs, maxOccurs);
		s.setQName(new QName("innerSequence"+seqCount++));
		this.elements.put(s, new OccurringDefinition(minOccurs, maxOccurs));
		
		return s;
	}

	public Choice addChoice(int minOccurs, int maxOccurs) {
		if (this.hasAll()) {
			throw new UnsupportedOperationException("This type contains already an element ALL and its children. Other children of type Element, Sequence or Choice cannot be added.");
		}
		
		Choice c = new Choice(minOccurs, maxOccurs);
		c.setQName(new QName("innerChoice"+choiCount++));
		this.elements.put(c, new OccurringDefinition(minOccurs, maxOccurs));		
		
		return c;
	}
	
	public All addAll(int minOccurs, int maxOccurs) {
		if (this.hasChildren()) {
			throw new UnsupportedOperationException("This type contains already other children of type Element, Sequence or Choice. An ALL-Element and its children cannot therefore not be added.");
		}
		
		All a = new All(minOccurs, maxOccurs);
		a.setQName(new QName("innerAll"));
		this.elements.put(a, new OccurringDefinition(minOccurs, maxOccurs));
		return a;
	}
	
	public boolean existsInSequence(QName q) {
		if (this.getSequence(q) != null) {
			return true;
		}
		return false;
	}
	
	public Sequence getSequence(QName q) {
		for (ListElement s : this.getListElements()) {
			if (s.getListElementType() == Type.SEQUENCE) {
				Sequence seq = (Sequence) s;
				if (seq.getElements().contains(q)) {
					return seq;
				}
			}
		}
		return null;
	}
	
	public boolean existsInChoice(QName q) {
		if (this.getChoice(q) != null) {
			return true;
		}
		return false;
	}
	
	public Choice getChoice(QName q) {
		for (ListElement s : this.getListElements()) {
			if (s.getListElementType() == Type.CHOICE) {
				Choice seq = (Choice) s;
				if (seq.getElements().contains(q)) {
					return seq;
				}
			}
		}
		return null;
	}

	public void addElement(QName q) {
		this.addElement(q, 0, 1);
	}
	
	public void addElement(QName q, int minOccurs, int maxOccurs) {
		if (this.hasAll()) {
			throw new UnsupportedOperationException("This type contains already an element ALL and its children. Other children of type Element, Sequence or Choice cannot be added.");
		}
		
		Element e = new Element();
		e.setQName(q);
		this.elements.put(e, new OccurringDefinition(minOccurs, maxOccurs));
	}
	
	/**
	 * Add an element to the list of this instance of class {@link Type}.
	 * A list can differ depending on the type of the list.
	 * 
	 * @param e - instance of class {@link Element}, which should be added as a child of this instance of class {@link Type}
	 */
	public void addElement(Element e) {
		this.addElement(e.getQName(), 0, 1);
	}
	
	public void addElement(Element e, int minOccurs, int maxOccurs) {
		this.addElement(e.getQName(), minOccurs, maxOccurs);
	}
	
	/**
	 * @param list
	 * @return
	 */
	public void addElements(Collection<QName> list) {
		for (QName q : list) {
			this.addElement(q);
		}
	}
	
	/**
	 * Method simplifies the access by accessing the list directly.
	 * 
	 * @return all elements of a list
	 * @throws IOException 
	 */
	public Collection<Element> getElements() throws IOException {
		Collection<Element> list = new ArrayList<Element>();
		for (QName q : this.getElementsQName()) {
			list.add(SchemaParser.getElement(q));
		}
		return list;
	}
	
	public Collection<QName> getElementsQName() {
		Collection<QName> list = new ArrayList<QName>();
		for (ListElement e : this.getListElements()) {
			list.add(e.getQName());
		}
		return list;
	}
	
	public Collection<ListElement> getListElements() {
		return this.elements.keySet();
	}
	
	public int getElementMinOccurs(QName q) {
		if (this.elements.containsKey(q)) {
			this.elements.get(q).getMinOccurs();
		}
		return -1;
	}
	
	public int getElementMaxOccurs(QName q) {
		if (this.elements.containsKey(q)) {
			this.elements.get(q).getMaxOccurs();
		}
		return -1;
	}
	
	/**
	 * @param q
	 * @return
	 * @throws IOException
	 */
	public void addAttribute(QName q) {
		this.getAttributesQName().add(q);
	}
	
	/**
	 * @param att
	 * @return
	 */
	public void addAttribute(Element att) {
		this.addAttribute(att.getQName());
	}	

	/**
	 * @param list
	 * @return
	 */
	public void addAttributes(Collection<QName> list) {
		this.getAttributesQName().addAll(list);
	}
	
	/**
	 * @return a collection of attributes, which will have to be used or not, depends on the attribute and its parameter
	 * @throws IOException
	 * @see Element#isRequired()
	 * @see Element#canOccurrMoreThanOnce()
	 */
	public Collection<Element> getAttributes() throws IOException {
		Collection<Element> atts = new ArrayList<Element>();
		for (QName q : this.getAttributesQName()) {
			atts.add(SchemaParser.getElement(q));
		}
		return atts;
	}
	
	public Collection<QName> getAttributesQName() {
		return this.attributes;
	}

	/**
	 * Create an restriction to this instance of class {@link Type}
	 * @param restrictionBase - the qualified name of the type definition, which restricts this type with its type
	 * @return an instance of class {@link Restriction}
	 * @see Restriction
	 */
	public Restriction setRestriction(QName restrictionBase) {
		Restriction res = new Restriction(restrictionBase);
		if (restrictionBase != null) {
			this.restriction = res;
		}
		return res;
	}
	
	/**
	 * Create an restriction to this instance of class {@link Type}
	 * @param res - instance of class {@link Restriction} with a defined qualified name, which defines the restriction in more detail
	 * @see Restriction
	 */
	public void setRestriction(Restriction res) {
		this.restriction = res;
	}

	/**
	 * Create an restriction to this instance of class {@link Type}
	 * @return an instance of class {@link Restriction}
	 * @see Restriction
	 */
	public Restriction getRestriction() {
		return this.restriction;
	}

	public void setReference(QName reference) {
		this.reference = reference;
	}
	
	public QName getReference() {
		return this.reference;
	}

	public void setListItemType(QName itemType) {
		this.itemType = new Type(itemType);
	}
	
	public Type getListItemType() {
		return this.itemType;
	}
	

	/**
	 * This class CHOICE extends class {@link Sequence}. Furthermore this class
	 * describes a totally different kind of list of elements. First, only one element
	 * of this list can be used, the other elements should not be used or an error may
	 * occur.
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/02/27)
	 */
	public class Choice extends Sequence {
		
		public Choice(int minOccurs, int maxOccurs) {
			this(new ArrayList<ListElement>(), minOccurs, maxOccurs);
		}

		public Choice(Collection<ListElement> list, int minOccurs, int maxOccurs) {
			super(list, minOccurs, maxOccurs);
		}
		
		@Override
		public Choice getObject() {
			return this;
		}
		
		@Override
		public int getListElementType() {
			return Type.CHOICE;
		}
		
	}
	
	/**
	 * This class abstracts the methods required for the organization of xml schema sequences
	 * and choices. It is the most simplest list type existing.
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/02/27)
	 * @see Choice
	 * @see OccurringDefinition
	 */
	public class Sequence extends All implements ListElement {
		
		public Sequence(int minOccurs, int maxOccurs) {
			this(new ArrayList<ListElement>(), minOccurs, maxOccurs);
		}

		public Sequence(Collection<ListElement> list, int minOccurs, int maxOccurs) {
			super(minOccurs, maxOccurs);
			this.list = list;
		}
		
		@Override
		public int getListElementType() {
			return Type.SEQUENCE;
		}
		
		/**
		 * @return true if this instance contains an instance of {@link Sequence}.
		 */
		public boolean hasSequence() {
			for (ListElement e : this.getElements()) {
				if (e.getListElementType() == Type.SEQUENCE) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * @param list - the list with the child elements of a {@link Sequence}
		 * @param minOccurs - the sequence how often it should be used at minimum
		 * @param maxOccurs - the sequence how often it should be used at maximum
		 * @return 
		 */
		public Sequence addSequence(int minOccurs, int maxOccurs) {
			return this.addSequence(new Sequence(minOccurs, maxOccurs));
		}
		
		/**
		 * @param s
		 * @return 
		 */
		public Sequence addSequence(Sequence s) {
			this.list.add(s);
			return s;
		}
		
		/**
		 * @return true if this instance contains an instance of {@link Choice} as child element.
		 */
		public boolean hasChoice() {
			for (ListElement e : this.getElements()) {
				if (e.getListElementType() == Type.CHOICE) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * @param list - the list with the child elements of a {@link Choice}
		 * @param minOccurs - the choice how often it should be used at minimum
		 * @param maxOccurs - the choice how often it should be used at maximum
		 * @return 
		 */
		public Choice addChoice(int minOccurs, int maxOccurs) {
			return this.addChoice(new Choice(minOccurs, maxOccurs));
		}
		
		/**
		 * @param c
		 * @return 
		 */
		public Choice addChoice(Choice c) {
			this.list.add(c);
			return c;
		}
	}
	
	public class All extends OccurringDefinition implements ListType, ListElement {
	
		protected Collection<ListElement> list;
		
		private QName qname;
		
		public All(int minOccurs, int maxOccurs) {
			this(new ArrayList<QName>(), minOccurs, maxOccurs);
		}
		
		public All(Collection<QName> elements, int minOccurs, int maxOccurs) {
			super(minOccurs, maxOccurs);
			this.list = new ArrayList<ListElement>();
			for (QName q : elements) {
				this.addElement(new ElementReference(q));
			}
		}
		
		@Override
		public All getObject() {
			return this;
		}

		@Override
		public QName getQName() {
			return this.qname;
		}
		
		@Override
		public void setQName(QName name) {
			this.qname = name;
		}
		
		@Override
		public int getListElementType() {
			return Type.ALL;
		}

		@Override
		public Collection<ListElement> getElements() {
			return this.list;
		}		
		
		public void addElement(QName q) {
			this.addElement(q, 0, 1);
		}
		
		public void addElement(QName q, int minOccurs, int maxOccurs) {
			this.addElement(new ElementReference(q, minOccurs, maxOccurs));
		}
		
		public void addElement(ElementReference e) {
			this.list.add(e);
		}		
	}

	public class ElementReference extends OccurringDefinition implements ListElement {

		private QName qname;
		
		public ElementReference(QName q) {
			this(q,0,1);
		} 
		
		public ElementReference(int minOccurs, int maxOccurs) {
			this(null,0,1);
		}
		
		public ElementReference(QName q, int minOccurs, int maxOccurs) {
			super(minOccurs, maxOccurs);	
			this.setQName(q);
		}

		@Override
		public int getListElementType() {
			return Type.ELEMENT;
		}

		@Override
		public ElementReference getObject() {
			return this;
		}

		@Override
		public QName getQName() {
			return this.qname;
		}

		@Override
		public void setQName(QName name) {
			this.qname = name;
		}
		
	}
	
	/**
	 * This class summarizes how often an element or list of elements can
	 * occur. Therefore the minimum and the maximum can be defined through
	 * this class. This class needs to be extended for further functionality.
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/02/25)
	 *
	 */
	public class OccurringDefinition {
		
		private int minOccurs;
		private int maxOccurs;
		
		public OccurringDefinition(int minOccurs, int maxOccurs) {
			this.setMinOccurs(minOccurs);
			this.setMaxOccurs(maxOccurs);
		}

		
		/**
		 * @param minOccurs the minOccurs to set
		 */
		public void setMinOccurs(int minOccurs) {
			this.minOccurs = minOccurs;
		}

		/**
		 * @return the minOccurs
		 */
		public int getMinOccurs() {
			return minOccurs;
		}

		/**
		 * @param maxOccurs the maxOccurs to set
		 */
		public void setMaxOccurs(int maxOccurs) {
			this.maxOccurs = maxOccurs;
		}

		/**
		 * @return the maxOccurs
		 */
		public int getMaxOccurs() {
			return maxOccurs;
		}
		
		
		
	}
	
	public interface ListType {
		
		/**
		 * @return a number of elements contained in a list
		 */
		public Collection<ListElement> getElements();
	}
	
	public interface ListElement {
		
		/**
		 * @return the object which implemented this interface
		 */
		public Object getObject();
		
		public int getListElementType();
		
		public QName getQName();
		
		public void setQName(QName name);
	}
	
}
