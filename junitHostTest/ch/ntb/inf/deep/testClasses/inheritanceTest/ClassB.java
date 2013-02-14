package ch.ntb.inf.deep.testClasses.inheritanceTest;

public class ClassB extends ClassA{
	
	public static boolean cVar2 = false;
	
	public double iVar1 = 2 * 3.1415926535897932384626433832795; // 40 19 21 fb 54 44 2d 18

	public float methodB() {
		return (float)(iVar0 * iVar1);
	}
	
	public int methodC() {
		if(cVar2) return iVar0; 
		return cVar0; 
	}
}
