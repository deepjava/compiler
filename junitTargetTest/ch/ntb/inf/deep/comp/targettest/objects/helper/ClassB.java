package ch.ntb.inf.deep.comp.targettest.objects.helper;

/**
 * NTB 12.04.2011
 * 
 * @author Urs Graf
 * 
 */

public class ClassB extends ClassA {

	public static boolean cVar0;

	public int iVar0 = 143;
	public byte iVar1 = 0x05;

	public ClassB(boolean var) {
		cVar0 = var;
	}

	public int methodB() {
		return iVar0 * iVar1;
	}

	public int methodC() {
		if (cVar0)
			return iVar0;
		return iVar1;
	}
}



