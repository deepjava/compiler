package ch.ntb.inf.deep.host;
// muss noch ins paket conguration?? verschoben werdne


public class SystemClass {
	public SystemClass next;
	public String  name;
	public SystemMethod  methods;
	public int	attributes;	//e.g.  (1<<dpfSynthetic) | (1<<dpfUnsafe)

	public SystemClass(String  name, int attributes){
		this.name = name;
		this.attributes = attributes;
	}

	public void  add(SystemMethod method){
		method.next = methods;
		methods = method;
	}
}
