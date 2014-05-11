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

package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.RefType;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class OperatingSystem extends Item implements ICclassFileConsts {
	private static final int maxNofExceptionClasses = 16;
	private HString description;
	public Class usClass;
	public Class llClass;
	public Class kernelClass;
	public Class heapClass;
	Class resetClass;
	Class exceptionBaseClass;
	Class[] exceptions = new Class[maxNofExceptionClasses];
	int nofExcClasses;
	Class[] sysClasses;
	Method[][] sysMethods;

	public OperatingSystem(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
	}
		
	public void addExceptionClass(Class exc) {
		if (exc == null) return;
		if (Configuration.dbg) vrb.println("[CONF] OperatingSystem: adding exception " + exc.name);
		if (nofExcClasses == maxNofExceptionClasses) {ErrorReporter.reporter.error(252); return;}
		exceptions[nofExcClasses++] = exc;
		int index = exc.name.lastIndexOf('R');
		if (index > 0 ) {
			HString str = exc.name.substring(index);
//			HString str = exc.name.substring(index, index+5);	TODO
			if (Configuration.RESETCLASS.equals(str)) resetClass = exc;
		}
	}

	public HString getDescription() {
		return description;
	}
	
	/** 
	 * returns all methods defined in system classes with a fixed offset 
	 * the returned array is sorted with ascending offsets
	 */
	public Method[] getSystemMethodsWithOffsets() {
		int nof = 0;
		for (int i = 0; i < sysClasses.length; i++) {
			Method[] meths = sysMethods[i];
			for (int j = 0; j < meths.length; j++) {
				if (meths[j].fixed) nof++;
			}
		}
		if (nof > 0) {
			Method[] mOff = new Method[nof];
			int count = 0;
			for (int i = 0; i < sysClasses.length; i++) {
				Method[] meths = sysMethods[i];
				for (int j = 0; j < meths.length; j++) {
					if (meths[j].fixed) mOff[count++] = meths[j];
				}
			}
			// ascending (bubble sort), lowest key comes first
			int maxIndex = mOff.length-1;
			for (int left = 0; left < maxIndex; left++) {
				for (int right = maxIndex-1; right >= left; right--) {
					if (mOff[right].offset > mOff[right+1].offset) { // swap
						Method m = mOff[right];
						mOff[right] = mOff[right+1];
						mOff[right+1] = m;
					}
				}
			}
			return mOff;
		} else return null; 
	}
	
	/**
	 * goes through the refTypeList and inserts all classes which are defined as system classes into array
	 * for each class all the methods defined in the configuration are inserted in an array as well
	 */
	void addSysClassesAndMethods() {
		int nof = 0;
		Item type = RefType.refTypeList;
		while (type != null) {
			if ((type.accAndPropFlags & (1<<dpfSysPrimitive)) != 0) nof++;
			type = type.next;
		}
		if (nof == 0) return;
		sysClasses = new Class[nof];
		sysMethods = new Method[nof][];
		int i = 0;
		type = RefType.refTypeList;
		while (type != null) {
			if ((type.accAndPropFlags & (1<<dpfSysPrimitive)) != 0) {
				sysClasses[i] = (Class) type;
				Item meth = ((Class) type).methods;
				nof = 0;
				while (meth != null) {
					nof++;
					meth = meth.next;
				}
				sysMethods[i] = new Method[nof];
				int k = 0;
				meth = ((Class) type).methods;	
				while (meth != null) {
					sysMethods[i][k++] = (Method) meth;
					meth = (Method)meth.next;
				}
				((Class) type).methods = null;
				i++;
			}
			type = type.next;
		}
	}

	public Method[] getSystemMethods(Class cls) {
		int i = 0;
		assert sysClasses != null;
		while (i < sysClasses.length) {
			if (sysClasses[i].name.equals(cls.name)) break;
			i++;
		}
		assert i < sysClasses.length;
		return sysMethods[i];
	}
	
	public Method getSystemMethodByName(Class cls, String name) {
		HString hName = HString.getHString(name);
		return getSystemMethodByName(cls, hName);
	}

	public Method getSystemMethodByName(Class cls, HString name) {
		int i = 0;
		assert sysClasses != null;
		while (i < sysClasses.length) {
			if (sysClasses[i].name.equals(cls.name)) break;
			i++;
		}
		assert i < sysClasses.length;
		Method[] meths = sysMethods[i];
		i = 0;
		while (i < meths.length) {
			if (meths[i].name.equals(name)) break;
			i++;
		}
		if (i < meths.length) return meths[i];
		else return null;
	}

	public int getSystemMethodIdByName(Class cls, HString name) {
		int i = 0;
		assert sysClasses != null;
		while (i < sysClasses.length) {
			if (sysClasses[i].name.equals(cls.name)) break;
			i++;
		}
		assert i < sysClasses.length;
		Method[] meths = sysMethods[i];
		i = 0;
		while (i < meths.length) {
			if (meths[i].name.equals(name)) break;
			i++;
		}
		if (i < meths.length) return meths[i].id;
		else return -1;
	}


	public Class getSystemClassByName(HString name) {
		int i = 0;
		assert sysClasses != null;
		while (i < sysClasses.length) {
			if (sysClasses[i].name.equals(name)) break;
			i++;
		}
		assert i < sysClasses.length;
		return sysClasses[i];
	}

}
