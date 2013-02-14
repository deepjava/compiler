package ch.ntb.inf.deep.comp.hosttest.testClasses;

public class T01SimpleMethods {
	static int global1, count;
	static boolean done;
	
    public static void emptyMethodStatic() {
    }
    
    public void emptyMethod() {
    }

    public static void assignment1() {
        int x = 1;
    }

	public static boolean assignment2(int a) {
		boolean b = a > 10;
		return b;
	}
	
	public static void assignment3(Object obj) {
		done = done && global1 > 10;
		if (done) {
			count++;
			int n = count;
			while(obj != null) n >>= 1;
		}
	}


    public static void simple1(int y) {
        int x = 0;
        x++;
        x += 3;
        x--;  
        x = y + 1;
    }

    public static void simple2() {
        
        int a = 1;
        int b = 2;
        a = 1 + 2;
        a = b; 
        a = a + b;
        b = a + 1;
    }
    
    public static void simple3() {
        int x = 1;
        int y = 2;
        x = x + y;
        y = x + y;        
    }

    public static void simple4() {
        int a;
        a = 0;
        a = 1;
        a = 100;
        a = 10000;
        a = 32767;
        a = 40000;
        a = 80000;
        a = 100000;
        a = 2147440000;
        a = 2147483647;
        a = -100;
        a = -10000;
        a = -32768;
        a = -40000;
        a = -80000;
        a = -100000;
        a = -2147440000;
        a = -2147483648;
    }

    public static void simple5() {
        long a;
        a = 1;
        a = -1;
        a = 0x2233445566778899L;
        a = 0x9988776655443322L;
    }
    
    public static void simple6() {
        float a = 1.5f;
        double b = 2.5;
        b = ExampleInterface.d;
        String c = "abc";
        Object d = null;
        d = ExampleInterface.o;
    }


}
