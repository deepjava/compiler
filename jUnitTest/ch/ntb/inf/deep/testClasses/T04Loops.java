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
		i = -6; // 0: c TRUE, b FALSE ; - 6 b TRUE c FALSE

		do {
			j++;
			i = i + j;
			b = i < 10;
			c = j < 5;
		} while (b && c);

		if (b) {
			j = -1;
		} else {
			j = 1;
		}
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
	
 	private void whileAfterWhile(int a) {
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

	private void while2() {
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

	private void while3() {
		int a = 10;
		while (a < 15) a++;
		int b = 20;
		while (b > 10) {
			int c = b + 1;
		}
		if (a < b) a += 10;
		int c = 2 + a;
	}
	
	private void whileTrue2() {
		while (true) {
			whileAfterWhile(65);
			for (int i = 0; i < 1000000; i++);
		}
	}

}
