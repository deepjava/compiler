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

public class OperatingSystem implements ICclassFileConsts {
	private SystemClass kernel;
	private SystemClass heap;
	private SystemClass exceptionBaseClass;
	private SystemClass exceptions;
	private SystemClass us;
	private SystemClass lowlevel;
	private SystemClass list;

	public void setKernel(SystemClass kernel) {
		this.kernel = kernel;
		this.addClass(kernel);
	}

	public void setHeap(SystemClass heap) {
		this.heap = heap;
		this.addClass(heap);
	}

	public void addException(SystemClass exception) {
		// check if already exists
		SystemClass current = exceptions;
		while (current != null) {
			if (current.name.equals(exception.name)) {
				current.attributes = exception.attributes;
				current.methods = exception.methods;
				return;
			}
			current = current.next;
		}
		// add SystemClass
		exception.next = this.exceptions;
		this.exceptions = exception;
		this.addClass(exception);

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

	public SystemClass getUs() {
		return us;
	}

	public SystemClass getLowLevel() {
		return lowlevel;
	}

	public SystemClass getClassList() {
		return list;
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
			current = current.next;
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
		StdStreams.out.println("}");
	}

	private void addClass(SystemClass clazz) {
		clazz.next = list;
		list = clazz;
	}
}
