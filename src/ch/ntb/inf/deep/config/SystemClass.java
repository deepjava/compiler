package ch.ntb.inf.deep.config;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.StdStreams;

public class SystemClass {
	public SystemClass next;
	public String name;
	public SystemMethod methods;
	public int attributes;

	public SystemClass(String name) {
		this.name = name;
	}
	
	public void addAttributes(int attributes){
		this.attributes |= attributes;
	}
	

	public void addMethod(SystemMethod method) {
		method.next = methods;
		methods = method;
	}
	
	//--- debug primitives
	public void print(int indentLevel){
		PrintStream vrb = StdStreams.vrb;
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("class = "+name);
		SystemMethod current = methods;
		while(current != null){
			current.print(indentLevel);
			current = current.next;
		}
	}
}
