package ch.ntb.inf.deep.testClasses;

import ch.ntb.inf.deep.testClasses.ntbMpc555HB;

public class TKernel implements ntbMpc555HB {

	public static void blink(int nTimes) { 
		int delay = 100000;
		for (int i = 0; i < nTimes; i++) {
			for (int k = 0; k < delay; k++);
			for (int k = 0; k < delay; k++);
		}
		for (int k = 0; k < (1000000 - nTimes * 10000); k++);
	}


	private static short FCS(int begin, int end) {
		return 0;
	}
	
	private static void boot() {
		blink(1);
		int reg;
		do reg = FCS(1,2); while ((reg & (1 << 16)) == 0);	// wait for PLL to lock 
		short reset = FCS(3,4);
		if ((reset & (1<<5 | 1<<15)) != 0) {	// boot from flash
			blink(2);
		}
		int classConstOffset = FCS(5,6) * 4 + 4;
		int state = 0;
		int kernelClinitAddr = FCS(7,8); 
		while (true) {
			int constBlkBase = classConstOffset * 8;
			int varBase = constBlkBase + cblkVarBaseOffset;
			int varSize = constBlkBase + cblkVarSizeOffset;
			int begin = varBase;
			int end = varBase + varSize;
			while (begin < end) {FCS(begin, 0); begin += 4;}
			int clinitAddr = constBlkBase + cblkClinitAddrOffset;
			blink(state + 2);
			if (clinitAddr == -1) {	// skip empty constructors 
				if (clinitAddr != kernelClinitAddr) {	// skip kernel 
					blink(6);
				} else {	// kernel
				}
			}
			state++; //modNr++;
			constBlkBase += 4;
		}
	}

}
