package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Module {
	SegmentAssignment root;
	SegmentAssignment tail;
	HString name;
	
	public Module next;
	

	public Module(HString module){
		name = module;
	}
	
	public void setSegmentAssignment(SegmentAssignment assign){
		if(root == null){
			root = assign;
			tail = root;
		}
		int segHash = assign.contentAttribute.hashCode();
		SegmentAssignment current = root;
		while(current != null){
			if(current.contentAttribute.hashCode() == segHash){
				if(current.contentAttribute.equals(assign.contentAttribute)){
					//TODO warn the User
					//Overwrite the old assignment
					current.segmentDesignator = assign.segmentDesignator;
					return;
				}
			}
			current = current.next;			
		}
		tail.next = assign;
		tail = tail.next;
	}
	
	public SegmentAssignment getSegmentAssignments(){
		return root;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.print(name.toString() + " : ");
		SegmentAssignment current = root;
		while(current != null){
			current.print(indentLevel + 1);
			if(current.next != null){
				System.out.print(", ");
			}
			current = current.next;
		}
		System.out.println(";");		
	}
}
