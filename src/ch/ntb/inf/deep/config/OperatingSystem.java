package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class OperatingSystem {
	private HString kernel;
	private HString heap;
	private HString interrupt;
	private HString exception;
	
	
	
	public void setKernel(HString kernel){
		this.kernel = kernel;
	}
	
	public void setHeap(HString heap){
		this.heap = heap;
	}
	
	public void setInterrupt(HString interrupt){
		this.interrupt = interrupt;
	}
	
	public void setException(HString exception){
		this.exception = exception;
	}
	
	public HString getKernel(){
		return kernel;
	}
	
	public HString getHeap(){
		return heap;
	}
	
	public HString getInterrupt(){
		return interrupt;
	}
	
	public HString getException(){
		return exception;
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
		System.out.println("interrupt: " + interrupt.toString());
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("exception: " + exception.toString());
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
