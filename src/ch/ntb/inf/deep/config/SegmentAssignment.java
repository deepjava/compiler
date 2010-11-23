package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class SegmentAssignment {
	SegmentAssignment next;
	HString contentAttribute;
	HString segmentDesignator;

	public SegmentAssignment(HString contentAttribute){
		this.contentAttribute = contentAttribute;
	}
	
	public void setSegmentDesignator(HString designator){
		segmentDesignator = designator;
	}
	
	public void print(int indentLevel){
		System.out.print(contentAttribute.toString() + "@" + segmentDesignator.toString());
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			System.out.print("  ");
			indentLevel--;
		}
		System.out.println(contentAttribute.toString() + "@" + segmentDesignator.toString());
	}
	
}
