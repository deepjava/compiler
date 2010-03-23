package ch.ntb.inf.deep.testClasses;


public class T05Returns {
	public static int multipleReturns1(boolean a) {
		int x = 0;
		if(a) {
			x = 1;
			return x;
		}
		x = 22;
		return x;
	}

	public static int multipleReturns1() {
		int x = 22;
		if(x == 1) {
			return 1;
		}
		else if(x == 3) {
			return 3;
		}
		else if(x == 5) {
			return 5;
		}
		else {
			if(x == 2) {
				return 2;
			}
			else if(x == 4) {
				return 4;
			}
			else {
				return 100;
			}
		}
	}
}
