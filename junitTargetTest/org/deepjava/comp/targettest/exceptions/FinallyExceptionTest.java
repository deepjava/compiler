package org.deepjava.comp.targettest.exceptions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Test;

public class FinallyExceptionTest {
	
	static int x;
	
	static void m1() throws Exception {
		if (x == 1000) throw new E100();
	}
	
	@Test
	public static void test1() {
		x = 0;
		try {
			x = 10;
			m1();
		} catch (E100 e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		} finally {
			x += 5;
		}
		Assert.assertEquals("test1", 15, x);		
		
		CmdTransmitter.sendDone();
	}


	@Test
	public static void test2() {
		x = 0;
		try {
			x = 10;
			m2();
		} catch (E100 e1) {
			x += 20;
		} catch (Exception e) {
			x = +30;
		} finally {
			x += 1;
		}
		Assert.assertEquals("test2", 1131, x);		
		
		CmdTransmitter.sendDone();
	}

	private static void m2() throws E100 {
		try {
			m3();
		} finally {
			x += 100;
		}		
	}

	private static void m3() throws E100 {
		m4();		
	}

	private static void m4() throws E100 {
		x += 1000;
		throw new E100();		
	}

}

@SuppressWarnings("serial")
class E100 extends Exception { }
