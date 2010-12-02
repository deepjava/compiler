package ch.ntb.inf.deep.config;

public class SystemClass {
	public SystemClass next;
	public String name;
	public SystemMethod methods;
	public int attributes; //e.g (

	public SystemClass(String name) {
		this.name = name;
	}

	public void add(SystemMethod method) {
		method.next = methods;
		methods = method;
	}
	
	public void print(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("class = "+name);
		SystemMethod current = methods;
		while(current != null){
			current.print(indentLevel);
			current = current.next;
		}
	}
}
