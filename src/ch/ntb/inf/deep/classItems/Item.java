/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.classItems;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public abstract class Item implements Cloneable, ICclassFileConsts, ICdescAndTypeConsts {
	protected static final boolean dbg = false;
	static final boolean enAssertion = true;
	public static PrintStream vrb = StdStreams.vrb;
	static PrintStream log = StdStreams.log;
	static ErrorReporter errRep = ErrorReporter.reporter;

	protected static final HString UNDEF = HString.getRegisteredHString("???");	// default name for linker entries
	public static StringTable stab;

	public Item next;	// for building linked lists
	public HString name; // the key string for any item

	public Item type; // base type for objects of "RefType" or else type for other objects, e.g. fields or constants 
	public int accAndPropFlags; // access and property flags (see ICclassFileConsts)
	
	public int offset = -1;	// offset in bytes
	public int address = -1; // the absolute address of this item on the target
	public int index = -1; // index in table
	
	protected Item() {}

	Item(HString name) {
		this.name = name;
	}

	Item(HString name, Type type) {
		this.name = name;
		this.type = type;
	}

	protected Item getMethod(HString name, HString descriptor) {
		assert false : "override this method at class: "+ getClass().getName();
		return null;
	}

	Item getReplacedStub() {
		return this;
	}

	/**
	 * returns last item in linked list starting from parameter <code>this</code>
	 */
	public Item getTail() {
		Item item = this;
		while (item.next != null) item = item.next;
		return item;
	}


	/**
	 * inserts parameter <code>item</code> at head of list starting with <code>this</code>
	 * returns new head
	 */
	public Item insertHead(Item item) {
		item.next = this;
		return item;
	}
	
	/**
	 * appends parameter <code>item</code> at end of linked list starting with <code>this</code>
	 */
	public void appendTail(Item item) {
		Item tail = getTail();
		tail.next = item;
	}
		
	/**
	 * combines to linked lists given by <code>head1</code>, <code>tail1</code> and <code>head2</code>
	 * returns head of combined list
	 */
	public static Item appendItemList(Item head1, Item tail1, Item head2) {
		if (tail1 == null) head1 = head2; else tail1.next = head2;
		return head1;
	}

	/**
	 * returns item in linked list starting from parameter <code>this</code> with name <code>name</code>
	 */
	public Item getItemByName(HString name) {
		Item item = this;
		while (item != null && name != item.name)  item = item.next;
		return item;
	}

	/**
	 * returns item in linked list starting from parameter <code>this</code> with name <code>jname</code>.
	 * <code>jname</code> is registered in the string table if not already present 
	 */
	public Item getItemByName(String jname) {
		HString name = stab.insertCondAndGetEntry(jname);
		return getItemByName(name);
	}

	protected Item clone() {
		Item cln = null;
		try{
			cln = (Item)super.clone();
		}catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return cln;
	}

	//--- debug primitives
	public void printFields(int indentLevel){ vrb.println("no meth printFields for class "+ getClass().getName());}
	public void printMethods(int indentLevel){ vrb.println("no meth printMethods for class "+ getClass().getName());}

	public void printShort(int indentLevel) {
		indent(indentLevel);
		if(type != null) vrb.print(type.name);
		vrb.printf(" %1$s ", name);
	}

	public void printTypeCategory() {
		if(type != null) type.printTypeCategory(); else  vrb.print("(-)");
	}
	
	public void printSize() {
		vrb.print("<?>");
	}

	public void printName() {
		vrb.print(name);
	}

	public void printOwner() {
		vrb.print('?');
	}
	
	public void printName(int indentLevel) {
		indent(indentLevel);
		vrb.print(name);
	}

	public void printTypeName(int indentLevel) {
		indent(indentLevel);
		vrb.print(type.name);
	}

	public void print(int indentLevel) {
		printName(indentLevel);
	}

	public void println(int indentLevel) {
		print(indentLevel);  vrb.println();
	}
	
	public void printOffset(int indentLevel) {
		indent(indentLevel);
		printOffset();
	}
	
	public void printOffset() {
		vrb.print("offset = " + offset);
	}
	
	public void printAddress(int indentLevel) {
		indent(indentLevel);
		printAddress();
	}
	
	public void printAddress() {
		vrb.print("address = " + address);
	}
	
	public static void indent(int indentLevel) {
		StdStreams.vrbPrintIndent(indentLevel);
	}


}
