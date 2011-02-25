package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
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
		StdStreams.vrb.print(contentAttribute.toString() + "@" + segmentDesignator.toString());
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			StdStreams.vrb.print("  ");
			indentLevel--;
		}
		StdStreams.vrb.println(contentAttribute.toString() + "@" + segmentDesignator.toString());
	}
	
}
