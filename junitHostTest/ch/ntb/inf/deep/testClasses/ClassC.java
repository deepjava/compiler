package ch.ntb.inf.deep.testClasses;

public class ClassC {
	static void m1(){
		Object o1 = new int[3];
		((int[])o1)[2] = 7;
	}
}