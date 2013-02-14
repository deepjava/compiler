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

package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class OperatingSystem extends ConfigElement implements ICclassFileConsts, ErrorCodes {
	private static final int maxNofImplementations = 4;
	
	private HString description;
	private SystemClass[] us = new SystemClass[maxNofImplementations];
	private int nOfUsImplementations = 0;
	private SystemClass[] lowlevel = new SystemClass[maxNofImplementations];
	private int nOfLowlevelImplementations = 0;
	private SystemClass[] kernel = new SystemClass[maxNofImplementations];
	private int nOfKernelImplementations = 0;
	private SystemClass[] heap = new SystemClass[maxNofImplementations];
	private int nOfHeapImplementations = 0;
	private SystemClass[] exceptionBaseClass = new SystemClass[maxNofImplementations];
	private int nOfExceptionBaseClassImplementations = 0;
	private SystemClass exceptions;
	private SystemClass list;

	public OperatingSystem(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public void addUS(SystemClass us) {
		if(nOfUsImplementations < maxNofImplementations) {
			this.us[nOfUsImplementations++] = us;
			this.addClass(us);
		}
	}
	
	public void addLowlevel(SystemClass ll) {
		if(nOfLowlevelImplementations < maxNofImplementations) {
			this.lowlevel[nOfLowlevelImplementations++] = ll;
			this.addClass(ll);
		}
	}
	
	public void addKernel(SystemClass kernel) {
		if(nOfKernelImplementations < maxNofImplementations) {
			this.kernel[nOfKernelImplementations++] = kernel;
			this.addClass(kernel);
		}
	}
		
	public void addHeap(SystemClass heap) {
		if(nOfHeapImplementations < maxNofImplementations) {
			this.heap[nOfHeapImplementations++] = heap;
			this.addClass(heap);
		}
	}
	
	public void addExceptionBaseClass(SystemClass ebc) {
		if(nOfExceptionBaseClassImplementations < maxNofImplementations) {
			this.exceptionBaseClass[nOfExceptionBaseClassImplementations++] = ebc;
			this.addClass(ebc);
		}
	}
	
	public void addException(SystemClass exception) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] OperatingSystem: adding exception " + exception.getName());
		if(exceptions != null) {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for exception " + exception.getName());
			SystemClass e = (SystemClass)exceptions.getElementByName(exception.name);
			if(e == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding exception");
				exceptions.insertBefore(exception);
				exceptions = exception;
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> noting to do");
			}
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first exception");
			this.exceptions = exception;
			this.addClass(exception);
		}
	}

	public HString getDescription() {
		return description;
	}
	
	public SystemClass getUS(CPU cpu) {
		int i = 0;
		while(i < nOfUsImplementations) {
			if(us[i].checkCondition(cpu)) return us[i];
			i++;
		}
		return null;
	}
	
	public int getNofUsImplementations() {
		return nOfUsImplementations;
	}
	
	public SystemClass getLowlevel(CPU cpu) {
		int i = 0;
		while(i < nOfUsImplementations) {
			if(lowlevel[i].checkCondition(cpu)) return lowlevel[i];
			i++;
		}
		return null;
	}
	
	public int getNofLowlevelImplementations() {
		return nOfLowlevelImplementations;
	}
	
	public SystemClass getKernel(CPU cpu) {
		int i = 0;
		while(i < nOfKernelImplementations) {
			if(kernel[i].checkCondition(cpu)) return kernel[i];
			i++;
		}
		return null;
	}
	
	public int getNofKernelImplementations() {
		return nOfKernelImplementations;
	}
	
	public SystemClass getHeap(CPU cpu) {
		int i = 0;
		while(i < nOfHeapImplementations) {
			if(heap[i].checkCondition(cpu)) return heap[i];
			i++;
		}
		return null;
	}
	
	public int getNofHeapImplementations() {
		return nOfHeapImplementations;
	}
	
	public SystemClass getExceptionBaseClass(CPU cpu) {
		int i = 0;
		while(i < nOfExceptionBaseClassImplementations) {
			if(exceptionBaseClass[i].checkCondition(cpu)) return exceptionBaseClass[i];
			i++;
		}
		return null;
	}
	
	public int getNofExceptionBaseClassImplementations() {
		return nOfExceptionBaseClassImplementations;
	}
	
	public SystemClass getExceptions() {
		return exceptions;
	}

	public SystemMethod getSystemMethodById(int id, CPU cpu) {
		SystemMethod meth;
		SystemClass[] sysClass = {
			getUS(cpu),
			getLowlevel(cpu),
			getHeap(cpu),
			getKernel(cpu)
		};
		
		for (int i = 0; i < sysClass.length; i++) {
			meth = sysClass[i].methods;	
			while (meth != null){
				if ((meth.id) == id){
					return meth;
				}
				meth = (SystemMethod)meth.next;
			}
		}
		return null; 
	}
	
	public SystemMethod getSystemMethodByName(HString registeredName, CPU cpu) {
		SystemMethod meth;
		SystemClass[] sysClass = {
			getUS(cpu),
			getLowlevel(cpu),
			getHeap(cpu),
			getKernel(cpu)
		};
		
		for(int i = 0; i < sysClass.length; i++) {
			meth = sysClass[i].methods;	
			while(meth != null){
				if(meth.name == registeredName){
					return meth;
				}
				meth = (SystemMethod)meth.next;
			}
		}
		return null;
	}
	
	public SystemMethod getExceptionMethodByName(HString name, CPU cpu) {
		SystemClass sysClass = exceptions;
		SystemMethod m;
		while(sysClass != null) {
			if(sysClass.checkCondition(cpu)){ 
				m = (SystemMethod)sysClass.methods.getElementByName(name);
				if(m != null) return m;
			}
			sysClass = (SystemClass)sysClass.next;
		}
		return null;
	}
	
	public SystemClass getAllSystemClasses() {
		return list;
	}
	
	private void addClass(SystemClass clazz) {
		if(list == null) {
			list = clazz;
		}
		else {
			list.append(clazz);
		}
	}
}
