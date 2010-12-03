package ch.ntb.inf.deep.config;

public class SystemMethod {
	public SystemMethod next;
	public String name;
	public int attributes; // e.g. (1<<dpfNew) 
	public int addr = -1;

	public SystemMethod(String name) {
		this.name = name;
	}

	public SystemMethod(String name, int attributes) {
		this.name = name;
		this.attributes = attributes;
	}
	
	public void print(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("method "+name+" {");
		
		for(int i = indentLevel+1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("attributes: 0x"+Integer.toHexString(attributes));
		
		if(addr > -1){
			for(int i = indentLevel+1; i > 0; i--){
				System.out.print("  ");
			}
			System.out.println("addr: 0x"+Integer.toHexString(addr));
		}
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
