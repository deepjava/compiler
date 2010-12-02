package ch.ntb.inf.deep.config;


public class OperatingSystem {
	private SystemClass kernel;
	private SystemClass heap;
	private SystemClass exception;
	private SystemClass us;
	private SystemClass lowlevel;
	private SystemClass list;
	
	
	
	public void setKernel(SystemClass kernel){
		this.kernel = kernel;
		this.addClass(kernel);
	}
	
	public void setHeap(SystemClass heap){
		this.heap = heap;
		this.addClass(heap);
	}
	
	public void setException(SystemClass exception){
		this.exception = exception;
		this.addClass(exception);
	}
	
	public void setUs(SystemClass us){
		this.us = us;
		this.addClass(us);
	}
	
	public void setLowLevel(SystemClass lowlevel){
		this.lowlevel = lowlevel;
		this.addClass(lowlevel);
	}
	
	public SystemClass getKernel(){
		return kernel;
	}
	
	public SystemClass getHeap(){
		return heap;
	}
	
	public SystemClass getException(){
		return exception;
	}
	
	public SystemClass getUs(){
		return us;
	}
	
	public SystemClass getLowLevel(){
		return lowlevel;
	}
	
	public SystemClass getClassList(){
		return list;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("operatingsystem {");
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("kernel {");
		kernel.print(indentLevel+2);
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("exception {");
		exception.print(indentLevel+2);
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("heap {");
		heap.print(indentLevel+2);		
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("us {");
		us.print(indentLevel+2);
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("lowlevel {");
		lowlevel.print(indentLevel+2);
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
	
	private void addClass(SystemClass clazz){
		clazz.next = list;
		list = clazz;
	}
}
