package ch.ntb.inf.deep.testClasses;

public class T03Switch {
	public static int switchNear1(int i) {
		switch(i) {
		case 0: return 0;
		case 1: return 1;
		case 2: return 2;
		default: return -1;
		}
	}

	public static int switchNear2(int i) {
		switch(i) {
		case 0: return 0;
		case 1: return 1;
		case 2: i++; break;
		case 3: return 3;
		case 4: i += 4;
		case 5: i += 5; break;
		default: return -1;
		}
		return i + 3;
	}
	
	public static void switchNear3(int i) {
		switch(i & 1) {
		case -3: 
		case 1:
			double a = i; //2.4;
			break;
		case 3: 
			int b = -1;
		}
		float c = 2 * i;
	}
	
	public static int switchFar1(int i) {
		switch(i) {
			case -100: return -100;
			case 0: return 0;
			case 100: return 100;
			default: return -1;
		}
	}
	
	public static int switchFar2(int i) {
		switch(i) {
		case -100: return -100;
		case 0: 
			switch (i) {
			case 0: break;
			case 1: return 2;
			case 2: i += 2;
			case 3: break;
			case 4: return 3;
			}
		case 100: 
			switch (i) {
			case 0: break;
			case 2: i += 2;		
			case 5: break;
			default: return -1;
			}
		}
		return i;
	}

	protected boolean terminated;

	public void switchWhile() {
		switch (0) {
		case 0: 
			while (terminated)
				terminated = false;
			break;
		default:
			break;
		}
	}
}
