package ch.ntb.inf.deep.config;

public class OperatingSystem {
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
			System.out.print("  ");
		}
		System.out.println("operatingsystem {");

		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("kernel {");
		kernel.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("exceptionbaseclass {");
		exceptionBaseClass.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

		SystemClass current = exceptions;
		while (current != null) {
			for (int i = indentLevel + 1; i > 0; i--) {
				System.out.print("  ");
			}
			System.out.println("exception {");
			current.print(indentLevel + 2);
			for (int i = indentLevel + 1; i > 0; i--) {
				System.out.print("  ");
			}
			System.out.println("}");
			current = current.next;
		}

		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("heap {");
		heap.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("us {");
		us.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("lowlevel {");
		lowlevel.print(indentLevel + 2);
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

		for (int i = indentLevel; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");
	}

	private void addClass(SystemClass clazz) {
		clazz.next = list;
		list = clazz;
	}
}
