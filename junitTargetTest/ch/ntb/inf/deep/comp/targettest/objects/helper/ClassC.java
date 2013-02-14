package ch.ntb.inf.deep.comp.targettest.objects.helper;

/**
 * NTB 12.04.2011
 * 
 * @author Urs Graf
 * 
 */

public class ClassC extends ClassB {
	
	public static int cVar0 = 15;
	public static boolean cVar2 = false;

	public double iVar1 = 2 * 3.1415926535897932384626433832795; // 40 19 21 fb 54 44 2d 18


	public ClassC(boolean var) {
		super(var);
	}

	public int methodB() {
		return cVar0;
	}
	
	public int methodD(short x) {
		return iVar0 * x;
	}
}

