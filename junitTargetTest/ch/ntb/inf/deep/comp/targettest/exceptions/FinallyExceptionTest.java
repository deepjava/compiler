package ch.ntb.inf.deep.comp.targettest.exceptions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Ignore;
import ch.ntb.inf.junitTarget.Test;

public class FinallyExceptionTest {
	
	static int x;
	
	static void m1() throws Exception {
		if (x == 1000) throw new E100();
	}
	
	@Test
	public static void test1() {
		int x = 0;
		try {
			x = 10;
			m1();
		} catch (E100 e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
//		} finally {	// ist noch Problem
//			x += 5;
		}
		Assert.assertEquals("test1", 20, x);		
		
		CmdTransmitter.sendDone();
	}

	
}

class E100 extends Exception { }
