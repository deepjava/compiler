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

}
