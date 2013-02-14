package ch.ntb.inf.deep.comp.targettest.various;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 25.5.2011, Urs Graf
 * tests various forms of phi functions
 *  
 *         Changes:
 */
@MaxErrors(100)
public class VariousTest3 {

	// checks if phi functions with same index are splitted correctly 
	static int phiFunction1(int a) {
		if (a > 0) {
			float f2 = 3.0f;
			while (f2 < 10.0) f2 += 5.0;
			a = (int)f2;
		} else {
			int b = 4;
			while (b < 10) b += 5;
			a = b;
		}
		return (a);
	}

	@Test
	public static void phiFunction1Test() {
		Assert.assertEquals("Test1", 13, phiFunction1(2));
		Assert.assertEquals("Test2", 14, phiFunction1(-2));

		CmdTransmitter.sendDone();
	}	

	private static int forIfFor() {
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

	@Test
	public static void forIfForTest() {
		Assert.assertEquals("Test1", 290, forIfFor());

		CmdTransmitter.sendDone();
	}	

}
