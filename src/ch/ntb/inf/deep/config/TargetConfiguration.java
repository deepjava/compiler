package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class TargetConfiguration {
		
	Module targetConfig;
	HString name;
	TargetConfiguration next;
	
	public TargetConfiguration(HString name){
		this.name = name;
	}
	
	public void setModule(Module mod) {
		if (targetConfig == null) {
			targetConfig = mod;
		}
		int modHash = mod.name.hashCode();
		Module current = targetConfig;
		Module prev = null;
		while (current != null) {
			if (current.name.hashCode() == modHash) {
				if (current.name.equals(mod.name)) {
					//TODO warn the User
					mod.next = current.next;
					if(prev != null){
						prev.next = mod;
					}else{
						targetConfig = mod;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		//if no match prev shows the tail of the list
		prev.next = mod;
	}

	public Module getModules(){
		return targetConfig;
	}
	
	public void print(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("TargetConfiguration " + name.toString() + " {");
		Module current = targetConfig;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
