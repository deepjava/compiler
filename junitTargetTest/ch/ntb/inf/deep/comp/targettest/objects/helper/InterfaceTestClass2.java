package ch.ntb.inf.deep.comp.targettest.objects.helper;

public class InterfaceTestClass2 implements Interface1, Interface3 {

	@Override
	public int method11() {
		return Interface1.var5 + Interface2.var5;
	}

	public int method2(int val) {
		return val + 10;
	}
	
	@Override
	public int method12(int i) {
		return i + 20;
	}

	@Override
	public short method32() {
		return 30;
	}
	
	
	public static int getI2Var0(){
		return Interface2.var0;
	}
	public static double getI2Var1(){
		return Interface2.var1;
	}
	public static float getI2Var2(){
		return Interface2.var2;
	}
	public static boolean getI2Var3(){
		return Interface2.var3;
	}
	public static long getI2Var4(){
		return Interface2.var4;
	}
	public static byte getI2Var5(){
		return Interface2.var5;
	}
	public static short getI2Var6(){
		return Interface2.var6;
	}
	public static char getI2Var7(){
		return Interface2.var7;
	}

	public static int getVar0(){
		return Interface1.var0;
	}
	public static double getVar1(){
		return Interface1.var1;
	}
	public static float getVar2(){
		return Interface1.var2;
	}
	public static boolean getVar3(){
		return Interface1.var3;
	}
	public static long getVar4(){
		return Interface1.var4;
	}
	public static byte getVar5(){
		return Interface1.var5;
	}
	public static short getVar6(){
		return Interface1.var6;
	}
	public static char getVar7(){
		return Interface1.var7;
	}


}
