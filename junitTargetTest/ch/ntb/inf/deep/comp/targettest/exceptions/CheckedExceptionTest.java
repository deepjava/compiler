package ch.ntb.inf.deep.comp.targettest.exceptions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Ignore;
import ch.ntb.inf.junitTarget.Test;

public class CheckedExceptionTest {
	
	static int x;
	
	@Test
	public static void test1() {
		try {
			x = 10;
			if (x > 1) throw new E30(); 
		} catch (E10 e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("test1", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	static void m1() throws Exception {
		throw new E10();
	}
	
//	@Ignore
	@Test
	public static void test2() {
		try {
			m1();
		} catch (E30 e3) {
			x = 20;
		} catch (E20 e1) {
			x = 30;
		} catch (E10 e1) {
			x = 40;
		} catch (Exception e) {
			x = 50;
		}
		Assert.assertEquals("test2", x, 40);		
		
		CmdTransmitter.sendDone();
	}

	static void m2() throws Exception {
		throw new E30();
	}
	
//	@Ignore
	@Test
	public static void test3() {
		try {
			m2();
		} catch (E30 e3) {
			x = 20;
		} catch (E20 e1) {
			x = 30;
		} catch (E10 e1) {
			x = 40;
		} catch (Exception e) {
			x = 50;
		}
		Assert.assertEquals("test3", 20, x);		
		
		CmdTransmitter.sendDone();
	}

//	@Ignore
	@Test
	public static void testContext1() {
		int a = 1;
		int b = 1000;
		try {
			a += 10;
			throw new E20();
		} catch (Exception e) {
			a += 100;
			b += 1;
		}
		Assert.assertEquals("testContext10", 111, a);		
		Assert.assertEquals("testContext11", 1001, b);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testContext2() {
		int a = 1;
		int b = 1000;
		try {
			while (a <= 10) a++;
			throw new E20();
		} catch (Exception e) {
			a += 100;
			b += 1;
		}
		Assert.assertEquals("testContext20", 111, a);		
		Assert.assertEquals("testContext31", 1001, b);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testLoop() {
		int a = 1;
		double b = 1000.5;
		while (a < 10) {
			try {
				throw new E30();
			} catch (E10 e) {
				a += 1;
				b += 0.5;
			}
		}
		Assert.assertEquals("testLoop1", 10, a);		
		Assert.assertEquals("testLoop2", 1005, b, 10e-10);		
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testCatchTry1() {
		int a = 1;
		try {
			a += 10;
			throw new E30();
		} catch (E10 e) {
			try {
				a += 100;
				throw new E1();
			} catch (E1 e1) {
				a += 1000;
			}
		}
		Assert.assertEquals("testCatchTry1", 1111, a);		
		
		CmdTransmitter.sendDone();
	}
	
	static int m3() throws E1 {
		int a = 1;
//		throw new E1();
		try {
			a += 10;
			throw new E30();
		} catch (E10 e) {
			throw new E1();
		}
	}

	// geht noch nicht
	@Ignore
	@Test
	public static void testCatchTry2() {
		int a = 1;
		try {
			m3();
		} catch (Exception e) {
			a = -1;
		}
		Assert.assertEquals("testCatchTry2", -1, a);		
		
		CmdTransmitter.sendDone();
	}

}

class E10 extends Exception { }
class E20 extends Exception { }
class E30 extends E10 { }