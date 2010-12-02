package ch.ntb.inf.deep.host;
// muss noch ins paket conguration?? verschoben werdne


public class SystemMethod {
	public SystemMethod next;
	public String  name;
	public int	attributes;

	public SystemMethod(String  name){
		this.name = name;
	}

	public SystemMethod(String  methName, int	attributes){
		this(methName);
		this.attributes = attributes;
	}
}
