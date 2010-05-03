package ch.ntb.inf.deep.testClasses.interfaceTest;


public class ClassB extends ClassA implements InterfaceA, InterfaceC {
	static int b = 100;

	public int methodA(){
		return 0;
	}
	
	int methodA1(){
		return 0;
	}
	
	int methodA2(){
		return 0;
	}
	
	public int methodB(){
		return 0;
	}
	
	public int methodC(){
		return 0;
	}
	
	static {
		ClassA obj1 = new ClassB();
		obj1.methodA1();
		InterfaceC obj2 = new ClassB();
		obj2.methodB();
	}

}
