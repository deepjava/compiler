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

package org.deepjava.classItems;

import java.io.PrintStream;

public class InterfaceList {
	private static PrintStream vrb = Item.vrb;
	
	private static final byte listCapacityAddOn = 8;
	
	private Class[] interfaces; // interfaces
	public int length;
	public boolean done;
	
	public InterfaceList() {
		interfaces = new Class[listCapacityAddOn];
		done = true;
	}
	
	public void clear() {
		for (int n = interfaces.length-1; n >= 0; n--) interfaces[n] = null;
		length = 0;
		done = true;
	}
	
	public Class getFront() {
		assert length > 0;
		return interfaces[0];
	}
	
	public Class getTail() {
		assert length > 0;
		return interfaces[length-1];
	}
	
	/**
	 * get interface at specified position: position == 0: front item, position == length-1: tail item.
	 * @param position  item position in list: 0 <= position < length
	 * @return  interface at the specified position or null if position out of range
	 */
	public Class getInterfaceAt(int position) {
		if (position < 0 || position >= length) return null;  else return interfaces[position];
	}

	/**
	 * @param interf
	 * @return
	 */
	public int getIndexOf(Class interf) {
		int n = length-1;
		while (n >= 0 && interfaces[n] != interf) n--;
		return n;
	}

	/**
	 * @param interfaceId
	 * @return the position(index) of interfaceId if found, else -1
	 */
	public int getIndexWithThisId(int interfaceId) {
		int n = length-1;
		while (n >= 0 && interfaces[n].index < interfaceId) n--;
		if (n >= 0 && interfaces[n].index != interfaceId) n = -1;
		return n;
	}

	void updateBaseIndex(int interfaceId, int baseIndex) {
		assert false;
	}

	/**
	 * append interface sorted according to the extension level of their referencing classes (descending ext. level)
	 */
	void appendSorted(Class interf) {
		int pos = getIndexWithThisId(interf.index);	// select interface with the same identifier
		if (pos >= 0) {	// replace it, if the new interface with same id is an extension of the selected one
			if (interfaces[pos].methTabLength < interf.methTabLength) interfaces[pos] = interf;
		} else 	// append the new interface
			append(interf);
	}

	/**
	 * append interface if field index is not present in the list
	 */
	void appendCallId(Class interf) {
		int n = length-1;
		while (n >= 0 && interfaces[n].index != interf.index) n--;
		if (n < 0) append(interf);
	}

	/**
	 * append interface if field chkId is not present in the list
	 */
	void appendChkId(Class interf) {
		int n = length-1;
		while (n >= 0 && interfaces[n].chkId != interf.chkId) n--;
		if (n < 0) append(interf);
	}

	/**
	 * sort interfaces according to their ident(index) in descending way
	 */
	void append(Class interf) {
		if (length >= interfaces.length) {
			Class[] newInterfaces = new Class[length + listCapacityAddOn];
			System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
			interfaces = newInterfaces;
		}
		interfaces[length] = interf;
		length++;
	}

	/**
	 * sort interfaces according to their id (index) in descending way
	 */
	void sortId() {
		for (int left = 0; left < length-1; left++) {
			for (int right = left+1; right < length; right++) {
				if (interfaces[left].index < interfaces[right].index) {
					Class h = interfaces[left];
					interfaces[left] = interfaces[right];
					interfaces[right] = h;
				}
			}
		}
	}

	/**
	 * sort interfaces according to their type check identifier in descending way
	 */
	void sortChkId() {
		for (int left = 0; left < length-1; left++) {
			for (int right = left+1; right < length; right++) {
				if (interfaces[left].chkId < interfaces[right].chkId) {
					Class h = interfaces[left];
					interfaces[left] = interfaces[right];
					interfaces[right] = h;
				}
			}
		}
	}

	//--- debug utilities
	
	public void print(){
		vrb.printf("interface list: (length=%1$d, done=%2$b)\n", length, done);
		for( int n = 0; n < length; n++){
			Class interf = interfaces[n];
			vrb.printf("\tname=%1$s, id=%2$d, #meths=%3$d\n", interf.name, interf.index, interf.methTabLength );
		}
		vrb.println();
	}

//	public static void insertAndPrint(InterfaceList ia, int interfaceId, int nofInterfaceMethods ){
//		vrb.printf("\nupdate( %1$d, %2$d)\n", interfaceId, nofInterfaceMethods );
//		ia.update( interfaceId, nofInterfaceMethods  );
//		ia.print( );
//	}
//	
//	public static void main(String[] args) {
//		InterfaceList ia = new InterfaceList(5);
//		insertAndPrint( ia, 2, 12 );
//		insertAndPrint( ia, 3, 13 );
//		insertAndPrint( ia, 1, 11 );
//		
//		insertAndPrint( ia, 3, 3 );
//		insertAndPrint( ia, 3, 33 );
//		
//		insertAndPrint( ia, 2, 2 );
//		insertAndPrint( ia, 2, 22 );
//		
//		insertAndPrint( ia, 1, 1 );
//		insertAndPrint( ia, 1, 11 );
//		insertAndPrint( ia, 1, 111 );
//	}
}
