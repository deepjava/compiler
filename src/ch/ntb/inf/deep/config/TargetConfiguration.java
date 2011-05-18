package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class TargetConfiguration {
		
	Module modules;
	Module system;
	HString name;
	TargetConfiguration next;
	
	public TargetConfiguration(HString name){
		this.name = name;
	}
	
	public void setModule(Module mod) {
		if (modules == null) {
			modules = mod;
		}
		int modHash = mod.name.hashCode();
		Module current = modules;
		Module prev = null;
		while (current != null) {
			if (current.name.hashCode() == modHash) {
				if (current.name.equals(mod.name)) {
					//TODO warn the User
					mod.next = current.next;
					if(prev != null){
						prev.next = mod;
					}else{
						modules = mod;
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
	public void addSystemModule(Module mod){
		if(system == null){
			system = mod;
		}else{
			mod.next = system;
			system = mod;
		}		
	}

	public Module getModules(){
		return modules;
	}
	
	public Module getSystemModules(){
		return system;
	}
	
	public Module getModuleByName(HString moduleName){
		Module current = modules;
		while (current != null) {
			if (current.name.charAt(current.name.length() - 1) == '*') {
				if (current.name.length() <= moduleName.length()) {
					HString temp = current.name.substring(0, current.name.length() - 2);
					if (temp.equals(moduleName.substring(0, temp.length()))) {
						return current;
					}
				}
			} else if (current.name.equals(moduleName)) {
				return current;
			}
			current = current.next;
		}
		return null;
	}
	
	public void print(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("targetconfiguration " + name.toString() + " {");
		
		for(int i = indentLevel+1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("system {");
		
		Module current = system;
		while(current != null){
			current.println(indentLevel + 2);
			current = current.next;
		}
		
		for(int i = indentLevel+1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
		
		for(int i = indentLevel+1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("modules {");
				
		current = modules;
		while(current != null){
			current.println(indentLevel + 2);
			current = current.next;
		}
		
		for(int i = indentLevel+1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
		
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}
}
