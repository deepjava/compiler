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

package org.deepjava.linker;

import org.deepjava.classItems.Item;
import org.deepjava.host.StdStreams;

/** 
 * Base class for entries in the constant block, the type descriptor, and the system table.
 */
public class ConstBlkEntry extends Item {
	
	protected int getItemSize() {
		return -1;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		return -1;
	}
	
	public byte[] getBytes() {
		return null;
	}
	
	public void printList() {
		ConstBlkEntry i = this;
		while (i != null) {
			StdStreams.vrb.println(i);
			i = (ConstBlkEntry) i.next;
		}
	}
	
	public String getList() {
		String retString = "";
		ConstBlkEntry i = this;
		while (i != null) {
			retString += i.toString() + "\r\n";
			i = (ConstBlkEntry) i.next;
		}
		
		return retString;
	}
	
}
