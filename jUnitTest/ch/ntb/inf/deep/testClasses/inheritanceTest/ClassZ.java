package ch.ntb.inf.deep.testClasses.inheritanceTest;

public class ClassZ extends ClassA {
	
	public static boolean c = false;
	
	public float d = 3.1415926535897932384626433832795f; // 40 49 0f db

	public float methodB() {
		return a * d;
	}
	
	public int methodC() {
		if(c) return a; 
		return b; 
	}
}
