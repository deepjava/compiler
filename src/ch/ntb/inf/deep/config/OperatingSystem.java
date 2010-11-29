package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class OperatingSystem {
	private HString kernel;
	private HString heap;
	private HString exception;
	private HString hwd;
	
	
	
	public void setKernel(HString kernel){
		this.kernel = kernel;
	}
	
	public void setHeap(HString heap){
		this.heap = heap;
	}
	
	public void setException(HString exception){
		this.exception = exception;
	}
	
	public void setHwd(HString hwd){
		this.hwd = hwd;
	}
	
	public HString getKernel(){
		return kernel;
	}
	
	public HString getHeap(){
		return heap;
	}
	
	public HString getException(){
		return exception;
	}
	
	public HString getHwd(){
		return hwd;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("operatingsystem {");
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("kernel: " + kernel.toString());
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("heap: " + heap.toString());
		
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("exception: " + exception.toString());
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("hwd: " + hwd.toString());
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
