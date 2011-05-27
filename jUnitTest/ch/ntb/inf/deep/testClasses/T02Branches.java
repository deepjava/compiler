package ch.ntb.inf.deep.testClasses;

public class T02Branches {
    public static int if1(int a) {
        int i;
        if(a > 0) {
            i = a + 1;
        }
        else {
            i = a - 1;
        }
        return i;
    }

    public static void if2() {	// constants
        int a = 1;
        int b = 2;
        if(a >= b){
            a = 6;
        }
        else{
            a = 8;
        }
        b=a;
        a++;
        b++;
    }

    public static void if3() {	// nested if
        int x = 0, y = 1;
        if(x == 0) {
            if(y == 1) {
                x++;
                if(x == 1) {
                    y++;
                    if(y == 2) {
                        int a, b, c;                        
                        a = 1;
                        b = 2;
                        c = a - b;
                        c++;
                    }

                }
            }
        }
    }

	public void if4() {	// if boolean
		boolean bool = true;
		int a = 1;
		int b = 0;

		if(bool) {
			b = 8;
		}
		else {
			b = 2;
		}
		a = b;
		a++;
	}
	
	public int if5(int n, int m){ //example from mössenböck for loadParameter
		if(n < 0){
			n = 0; m = 0;
		}
		return n + m;
	}
	
	public int if6(double val){ // from doubleToChars
		int exp = 1000;
		int high = 12;
		if (exp < 1023){
			val = -val;
		}
		if (exp == 0){
			high = 13;
		} else {
			if (val > 100)
				val *= 3;
			else 
				val /=5;
			if (val > 200)
				high = 14;
		}
		if (val > 10) exp++;
		else val--;
		return exp;
	}
}
