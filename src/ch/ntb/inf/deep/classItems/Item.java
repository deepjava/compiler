/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.classItems;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public abstract class Item   implements Cloneable, ICclassFileConsts, ICdescAndTypeConsts {
	static final boolean verbose = false, enAssertion = true;
	static PrintStream vrb = StdStreams.vrb;
	static PrintStream log = StdStreams.vrb; // StdStreams.log;
	static ErrorReporter errRep = ErrorReporter.reporter;

	static StringTable stab;

	public static void indent(int indentLevel){
		StdStreams.vrbPrintIndent(indentLevel);
	}

	//--- instance fields
	public Item next;
	public HString name; // the key string for any item

	public Item type; // base type for objects of "Class" or "Type" or null
	public int accAndPropFlags; // access and property flags (see ClassFileConsts)
	
	public int offset = -1;
	public int address = -1; // the absolute address of this item on the target
	public int index = -1;
	
	//--- constructors
	Item(){
	}

	Item(HString name){
		this.name = name;
	}

	Item(HString name, Type type){
		this.name = name;
		this.type = type;
	}

	//--- instance methods
	public int getObjectSize(){
		return -1;
	}

	/**
	 * get size in Byte - always a power of 2 or -1 if undefined
	 * @return  {-1,  1, 2, 4, 8}
	 */
	public int getTypeSize(){
		return -1;
	}

	protected Item getMethod(HString name, HString descriptor){
		assert false : "override this method at class: "+ getClass().getName();
		return null;
	}

	protected int createInterfaceIdsToRoot(){
		return 0;
	}

	Item getReplacedStub(){
		return this;
	}

	public static Item getTailItem(Item item){
		if( item == null ) return null;
		while(item.next != null)  item = item.next;
		return item;
	}

	public static Item appendItem(Item head1, Item tail1, Item head2){
		if(tail1 == null)  head1 = head2;  else  tail1.next = head2;
		return head1;
	}

	public Item getItemByName(HString name){
		Item item = this;
		while(item != null && name != item.name)  item = item.next;
		return item;
	}

	public Item getItemByName(String jname){
		HString name = stab.insertCondAndGetEntry(jname);
		return getItemByName(name);
	}

	protected Item clone(){
		Item cln = null;
		try{
			cln = (Item)super.clone();
		}catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return cln;
	}

	protected void selectAndMoveInitClasses(){
		assert false : "override this method at class: "+ getClass().getName();
	}


	//--- debug primitives
	public void printFields(int indentLevel){ vrb.println("no meth printFields for class "+ getClass().getName());	}
	public void printMethods(int indentLevel){ vrb.println("no meth printMethods for class "+ getClass().getName());	}


	public void printShort(int indentLevel){
		indent(indentLevel);
		if(type != null) vrb.print(type.name);
		vrb.printf(" %1$s ", name);
	}

	public void printTypeCategory(){
		if(type != null) type.printTypeCategory(); else  vrb.print("(-)");
	}
	
	public void printSize() {
		vrb.print("<?>");
	}

	public void printName(){
		vrb.print(name);
	}

	public void printOwner(){
		vrb.print('?');
	}
	
	public void printName(int indentLevel){
		indent(indentLevel);
		vrb.print(name);
	}

	public void printTypeName(int indentLevel){
		indent(indentLevel);
		vrb.print(type.name);
	}

	public void print(int indentLevel){
		printName(indentLevel);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
	
	public void printOffset(int indentLevel){
		indent(indentLevel);
		printOffset();
	}
	
	public void printOffset(){
		vrb.print("offset = " + offset);
	}
	
	public void printAddress(int indentLevel){
		indent(indentLevel);
		printAddress();
	}
	
	public void printAddress(){
		vrb.print("address = " + address);
	}
}
