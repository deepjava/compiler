package ch.ntb.inf.deep.testClasses;

public class T09Types {
	static void m1() {
		boolean res;
		long a = -30000;
		if (a > 10000) {
			int b = 100;
			float bb = b;
		} else {
			float c = 1.0f;
		}
		float d = a * 2;
		res = a > 0? true : false;
		double e = a * 16;
		int f = (int) d;
	}

	float m2(long a, float b, double c, byte[] d, short e, int f, int g) {
		a = 0x7545 & a;
		e += 100;
		T08Calls.classMethod(d[2]);
		e = (short)(20 + e);
		g |= 0x223344;
		c = 3.2;
		int h = g - e;
		T08Calls.classMethod(h);
		short i = (short)h;		
		return b;
	}

}
