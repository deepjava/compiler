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
	
	public void whileTrue2() {
		while (true) {
			help3(65);
			for (int i = 0; i < 1000000; i++);
		}
	}
	
	public static int forIfFor() {
		int  offset, k; 
		offset = 0; 
		int val = 10;
		if (val == 10) {
			offset += 4;	
			for (int i = 0; i < 5; i++) {
				offset += 2;
			}
			for (int i = 0; i < 3; i++) {
				boolean valid = true;
				if(valid == false) offset += 138;
				else {
					for (k = 0; k < 32; k++) {
						offset += 2;
					}
					for (k = 0; k < 7; k++) {
						offset += 4;
					}	
				} 
			}
		} 
		return offset;
	}
	
	public static int help1() { return 0; }
	public static void help2() {}
	public static void help3(int a) {}
	public static int help4(int a) {return 0;}
	public static void help5(int a, int b) {}

	public static void phiFunctionTest1() {
		int a;	// a erhält Wert erst in der Schleife
		int b;	// b erhält Wert erst in der Schleife
		do {
			a = 100;
			b = a * 2;
		} while (b < 0);
	}

	public static void phiFunctionTest2() {
		help2();
		int a;	// a erhält Wert erst in der Schleife
		int b;	// b erhält Wert erst in der Schleife
		do {
			a = 100;
			b = a * 2;
		} while (b < 0);
	}

	public static void phiFunctionTest3() {
		int a = 100;	// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			a += 10;
			b = a * 2;
		} while (b < 0);
	}

	public static void phiFunctionTest4() {
		int a = 100;	// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			b = a * 2;
		} while (b < 0);
	}
	
	public static void phiFunctionTest5() {
		int a = 100;	// a erhält in der Schleife neuen Wert
		int b;			// b erhält Wert erst in der Schleife
		do {
			a = 200;
			b = a * 2;
		} while (b < 0);
	}
	
	public static int phiFunctionTest6() {
		int a;
		for (int i = 0; i < 10; i++);
		a = 100;
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	public static int phiFunctionTest7() {
		int a = 100;
		for (int i = 0; i < 10; i++);
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	public static int phiFunctionTest8() {
		int a = 100;
		for (int i = 0; i < 10; i++);
		int b = a * 3;
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	public static int phiFunctionTest9() {
		int a = 100;
		for (int i = 0; i < 10 + a; i++);
		int b = a * 3;
		for (int i = 0; i < 20; i++);
		return a + 3;
	}
	
	public static int phiFunctionTest10() {
		int a = 100;
		int b;
		for (int i = 0; i < 10; i++);
		b = 200;
		for (int i = 0; i < 20; i++) b++;
		return a + b;
	}

	public static void phiFunctionTest11(int a) {
		// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b;			// b erhält Wert erst in der Schleife
		do {
			b = a * 2;
			b++;
		} while (b < 0);	// Problem!!!!
	}

	public static void phiFunctionTest12(int a) {
		// a muss Register bis zum Ende der Schleife besitzen, sonst wird es neu vergeben
		int b = 100;			// b erhält Wert erst in der Schleife
		do {
			b += a;
		} while (b < 0);
	}

	public static int phiFunctionTest13(int a) {
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

	public void phiFunctionTest14(int a) {
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

	public void phiFunctionTest15() {
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

	public void phiFunctionTest16() {
		int a = 10;
		while (a < 15) a++;
		int b = 20;
		while (b > 10) {
			int c = b + 1;
		}
		if (a < b) a += 10;
		int c = 2 + a;
	}
	
	static int heapPtr;
	
	// from Heap.newMultiDimArray
	private static int phiFunctionTest17(int ref, int nofDim, int dim0, int dim1, int dim2, int dim3, int dim4) {
		if (nofDim > 3 || nofDim < 2) help3(20);
		if (nofDim == 2) {
			int elemSize = help4(ref);
			int dim1Size = (8 + dim1 * elemSize + 3) >> 2 << 2;	
			int size = 8 + dim0 * 4 + dim0 * dim1Size;
			int addr = heapPtr; 
			while (addr < heapPtr + size) {help5(addr, 0); addr += 4;}
			help5(heapPtr + 4, ref);	// write tag
			help5(heapPtr + 2, dim0);	// write length of dim0
			ref = heapPtr + 8;
			addr = ref;
			for (int i = 0; i < dim0; i++) {
				int elemAddr = ref + 4 * dim0 + 8 + i * dim1Size; 
				help5(addr, elemAddr);
				help5(elemAddr - 4, ref);	// write tag
				help5(elemAddr - 6, dim1);	// write length of dim0
				addr += 4;
			}
			heapPtr += ((size + 15) >> 4) << 4;
		}
		return ref;
	}

	static final int maxStringLen = 64;
	static byte[] txData = new byte[maxStringLen];
	static final byte startSymbol = 0x11;
	public static final boolean host = false;
	static void write(byte[] txData2, int i, int j) {}

	// from CmdTransmitter.sendFailed
	public static void phiFunctionTest18(byte code, String message, int expected, int actual) {
		int len = 0;
		byte checkByte;
		byte[] m = null;
		if (message != null) {
			len = message.length();
			m = new byte[len];
			for (int i = 0; i < len; i++) m[i] = (byte)message.charAt(i);
			if (len > maxStringLen)
				len = maxStringLen;
		}
		len += 11;
		txData[0] = startSymbol;
		txData[1] = (byte) len;
		checkByte = txData[1];
		txData[3] = (byte) (expected >> 24);
		checkByte ^= txData[3];
		txData[7] = (byte) (actual >> 24);
		checkByte ^= txData[7];
		txData[11] = code;
		checkByte ^= txData[11];
		for (int i = 12; i <= len; i++) {
			txData[i] = m[i - 12];
			checkByte ^= txData[i];
		}
		txData[len + 1] = checkByte;
		
		if(host){
			write(txData, 0, len + 2);
		}else{
			write(txData, 0, len + 2);
		}
	}

}
