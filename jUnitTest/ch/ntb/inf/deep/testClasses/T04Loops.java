package ch.ntb.inf.deep.testClasses;

public class T04Loops {
    public static void doWhile1() {
        int i = 0;
        do {
            i++;
        } while(i < 10);
        int j = i + 1; 
    }
    
	public static void doWhileIf1() {
		int j, i;
		boolean b, c;
		j = 0;
		i = -6; 
		do {
			j++;
			i = i + j;
			b = i < 10;
			c = j < 5;
		} while (b && c);

		if (b) j = -1;
		else j = 1;
	}

	public static int while1() {
		int i = 0;
		while(i < 10) {
			i++;
		}
		return i;
	}

	public static void whileTrue() {
		int a = 10;
		while(true) {
			int b = a + 1;
		}
	}

    public static void whileTrueBreak() {
    	int a = 10;
        while(true) {
        	int b = a + 1;
        	break;
        }
        int b = a;
    }

	public static int whileMultiCond() {
		int i = 0;
		while(i < 10 && i >= 0) {
			i++;
		}
		return i;
	}
	
	public static void for1(){
		int a = 0;
		for(int i=0; i < 10; i++){
			a++;
		}
	}
	
	public int forWhile(int x){
		for(int i = 0; i < x; i++){
			while(x > 4){
				x--;
			}
		}
		return x;
	}
	
	public void forIfWhile(){
		for(int i = 0; i < 100; i++){
			if(i > 50){
				while(i < 75){
					i++;
				}
			}
			
		}
	}
	
	private void whileTrue2() {
		while (true) {
			help3(65);
			for (int i = 0; i < 1000000; i++);
		}
	}
	
	private static int help1() { return 0; }
	private static void help2() {}
	private static void help3(int a) {}

	private static void phiFunctionTest1() {
		int a;	// a erhält Wert erst in der Schleife
		int b;	// b erhält Wert erst in der Schleife
		do {
			a = 100;
			b = a * 2;
		} while (b < 0);
	}

	private static void phiFunctionTest2() {
		help2();
		int a;	// a erhält Wert erst in der Schleife
		int b;	// b erhält Wert erst in der Schleife
		do {
			a = 100;
			b = a * 2;
		} while (b < 0);
	}

	private static void phiFunctionTest3() {
		int a = 100;	// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			a += 10;
			b = a * 2;
		} while (b < 0);
	}

	private static void phiFunctionTest4() {
		int a = 100;	// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			b = a * 2;
		} while (b < 0);
	}
	
	private static int phiFunctionTest5() {
		int a;
		for (int i = 0; i < 10; i++);
		a = 100;
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	private static int phiFunctionTest6() {
		int a = 100;
		for (int i = 0; i < 10; i++);
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	private static int phiFunctionTest7() {
		int a = 100;
		int b;
		for (int i = 0; i < 10; i++);
		b = 200;
		for (int i = 0; i < 20; i++) b++;
		return a + b;
	}

	private static void phiFunctionTest8(int a) {
		// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			b = a * 2;
			b++;
		} while (b < 0);	// Problem!!!!
	}

	private static void phiFunctionTest9(int a) {
		// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b = 100;			// b erhält Wert erst in der Schleife
		do {
			b += a;
		} while (b < 0);
	}

	static int phiFunctionTest10(int a) {
		if (a > 0) {
			float f2 = 3.0f;
			while (f2 < 10.0f) f2 += 5.0f;
			a = (int)f2;
		} else {
			int b = 4;
			while (b < 10) b += 5;
			a = b;
		}
		return (a);
	}

 	private void phiFunctionTest11(int a) {
		int b = 10;
		do a--; while(a < -1);
		b++;
		while (b == 0) {
			int c = a * 2;
			int d = c - b;
			if (c > 1) break;
			int e = c + d;
			b++;
		}
		int f = 2 + b;
	}

	private void phiFunctionTest12() {
		int a = 10;
		do a--; while(a > -1);
		int b = 20;
		while (b == 0) {
			int c = a * 2;
			int d = a -1;
			int e = a + b;
			b--;
		}
		int f = 2 + b;
	}

	private void phiFunctionTest13() {
		int a = 10;
		while (a < 15) a++;
		int b = 20;
		while (b > 10) {
			int c = b + 1;
		}
		if (a < b) a += 10;
		int c = 2 + a;
	}
	


}
