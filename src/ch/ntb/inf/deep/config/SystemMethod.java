package ch.ntb.inf.deep.config;

import java.io.PrintStream;

import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.host.StdStreams;

public class SystemMethod implements ICclassFileConsts{
	public SystemMethod next;
	public String name;
	public int attributes; // e.g. (1<<dpfNew) 
	public int offset = -1;

	public SystemMethod(String name) {
		this.name = name;
	}

	public SystemMethod(String name, int attributes) {
		this.name = name;
		this.attributes = attributes & (dpfSetSysMethProperties | sysMethCodeMask);
	}
	
	//--- debug primitives
	public void print(int indentLevel){
		PrintStream vrb = StdStreams.vrb;
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("method "+name+" {");
		StdStreams.vrbPrintIndent(indentLevel+1);
		vrb.printf("attributes: 0x%1$x\n", attributes);
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("}");
	}
}
