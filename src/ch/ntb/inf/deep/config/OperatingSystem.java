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
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class OperatingSystem extends ConfigElement implements ICclassFileConsts {
	private HString description;
	private SystemClass kernel;
	private SystemClass heap;
	private SystemClass exceptionBaseClass;
	private SystemClass exceptions;
	private SystemClass us;
	private SystemClass lowlevel;
	private SystemClass list;

	public OperatingSystem(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}

	public void setKernel(SystemClass kernel) {
		this.kernel = kernel;
		this.addClass(kernel);
	}

	public void setHeap(SystemClass heap) {
		this.heap = heap;
		this.addClass(heap);
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

	public void setExceptionBaseClass(SystemClass exceptionBaseClass) {
		this.exceptionBaseClass = exceptionBaseClass;
		this.addClass(exceptionBaseClass);
	}

	public void setUs(SystemClass us) {
		this.us = us;
		this.addClass(us);
	}

	public void setLowLevel(SystemClass lowlevel) {
		this.lowlevel = lowlevel;
		this.addClass(lowlevel);
	}

	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public SystemClass getKernel() {
		return kernel;
	}

	public SystemClass getHeap() {
		return heap;
	}

	public SystemClass getExceptionBaseClass() {
		return exceptionBaseClass;
	}

	public SystemClass getExceptions() {
		return exceptions;
	}

	public SystemMethod getExceptionMethodByName(String name) {
		int hash = name.hashCode();
		SystemClass sys = exceptions;
		SystemMethod m;
		while(sys != null) {
			m = sys.methods;
			while(m != null) {
				if(hash == m.name.hashCode()){
					if(name.equals(m.name)){
						return m;
					}
				}
				m = m.next;
			}			
			sys = (SystemClass)sys.next;
		}
		return null;
	}
	
	public SystemClass getUs() {
		return us;
	}

	public SystemClass getLowLevel() {
		return lowlevel;
	}

	public SystemClass getClassList() {
		return list;
	}

	public HString getDescription() {
		return description;
	}
	
	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("operatingsystem {");

		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("kernel {");
		kernel.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("exceptionbaseclass {");
		exceptionBaseClass.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

		SystemClass current = exceptions;
		while (current != null && (current.attributes & (1 << dpfExcHnd)) != 0 && (current != exceptionBaseClass)) {
			for (int i = indentLevel + 1; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("exception {");
			current.print(indentLevel + 2);
			for (int i = indentLevel + 1; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("}");
			current = (SystemClass)current.next;
		}

		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("heap {");
		heap.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("us {");
		us.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("lowlevel {");
		lowlevel.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
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
