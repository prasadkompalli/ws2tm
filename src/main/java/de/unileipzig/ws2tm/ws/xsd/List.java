package de.unileipzig.ws2tm.ws.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class abstracts possible list types defined by the XML schema recommendation of the W3C.
 * Currently three types of 
 * 
 * 
 * @author Torsten Grigull
 * @version 0.1 (2011/02/25)
 *
 */
public class List {

	/**
	 * CHOICE: describes a list, which can have more than children, but only child can be used
	 * for a request or a response XML. This follows the XML Schema Recommendation of the W3C
	 */
	public static final int CHOICE = 0;
	/**
	 * SEQUENCE: describes a list, which should have at least one child, the added children are
	 * places in the order they were added.
	 * Every child has to be used for a XML response or request following the XML Schema Recommendation
	 * of the W3C.
	 */
	public static final int SEQUENCE = 1;
	/**
	 * ALL: describes a list which can have one or more children, which can appear in any random
	 * order.
	 */
	public static final int ALL = 2;
	
	/**
	 * LIST: this type of list is a simple but very special list. Elements will occur divided by
	 * white-spaces in the same element. Like [element]12 2 54 3[/element].
	 */
	public static final int LIST = 3;
	
	private Collection<Element> list = new ArrayList<Element>();
	private int type;
	private boolean isChoice = false;
	
	public List() {
		this.setListType(1);
	}
	
	public List(int listType) {
		this.setListType(listType);
	}
	

	
	/**
	 * Add one instance of class {@link Element} as child of a list
	 * @param e - instance of class {@link Element}, which should be added to this list
	 * @return the added instance of class {@link Element} to this list
	 */
	public Element addElement(Element e) {
		this.list.add(e);
		return e;
	}
	
	/**
	 * Add a number of instances of class {@link Element} as children of a list
	 * @param list - instance of class {@link Collection}, which contains a number of instances
	 * of class {@link Element}, which should be added to this list
	 */
	public void addElements(Collection<Element> list) {
		this.list.addAll(list);
	}

	
	/**
	 * @return all elements contained in this list, depending on the {@link #type}, and the min and maxOccurs.
	 */
	public Collection<Element> getElements() {
		return this.list;
	}
	
	/**
	 * This method changes the list type of this instance of class {@link List}. The list type
	 * can be currently one of the following possibilities:
	 * <ul>
	 * <li>{@link #CHOICE}: One of the elements in this list has to be used</li>
	 * <li>{@link #SEQUENCE}: All of the elements in this list have to be used</li>
	 * <li>{@link #ALL}: All of the elements have to be used in any order.</li>
	 * </ul>
	 * @param listType - 0: {@link #CHOICE}, 1: {@link #SEQUENCE}, 2: {@link #ALL}.
	 * @return the newly collection of elements depending on the parameter listType and its value
	 */
	public Collection<Element> setListType(int listType) {
		Collection<Element> temp = list;
		
		switch (listType){
			case CHOICE: list = new ArrayList<Element>(); this.setChoice(true); break;
			case SEQUENCE: list = new ArrayList<Element>(); break;
			case ALL: list = new HashSet<Element>(); break;
			default: throw new IllegalArgumentException("The list type has to be between 0 and 2. 0: CHOICE, 1: SEQUENCE, 2: ALL.");
		}
		
		if (temp.size() > 0) {
			for (Element e: temp) {
				list.add(e);
			}
		}
		
		this.type = listType;
		
		return this.list;
	}
	
	/**
	 * Returns an integer currently between 0 and 2, defining the type of the list.
	 * @return 0: {@link #CHOICE}, 1: {@link #SEQUENCE}, 2: {@link #ALL}
	 */
	public int getListType() {
		return this.type;
	}

	/**
	 * @return true, if the listType describes the list as {@link CHOICE}
	 */
	public boolean isChoice() {
		if (this.type == CHOICE) {
			this.setChoice(true);
		}
		return this.isChoice;
	}
		
	/**
	 * @param b - sets the boolean flag {@link #isChoice} true or false
	 */
	private void setChoice(boolean b) {
		this.isChoice = b;
	}
	
	/**
	 * @return true, if the list is in an random order, which means that the list type is a list defined by {@link #ALL}.
	 */
	public boolean isRandomized() {
		if (this.type == ALL) {
			return true;
		}
		return false;
	}

}

