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
}
