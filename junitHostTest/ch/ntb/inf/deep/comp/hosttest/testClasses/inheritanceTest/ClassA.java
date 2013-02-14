package ch.ntb.inf.deep.comp.hosttest.testClasses.inheritanceTest;

public class ClassA {

	public static int cVar0 = 12;
	public static float cVar1 = 3.1415926535897932384626433832795f; // 40 49 0f db
		
	public short iVar0 = 1;
	
	public int methodA() {
		return cVar0 + iVar0;
	}
	
	public float methodB() {
		return (float)-1.0;
	}
	
	public void add(int y) {
		iVar0 += y;
	}
	
}
