package org.deepjava.comp.targettest.exceptions;

import java.io.IOException;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
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

	@Test
	public static void test4() {
		x = 0;
		try {
			m4();
		} catch (E20 e1) {
			x += 1;
		} catch (Exception e) {
			x = 50;
		}
		Assert.assertEquals("test4", 100, x);		
		
		CmdTransmitter.sendDone();
	}

	private static void m4() throws E20 {
		int a = 10;
		try {
			if (a == 100) throw new E20();
			else throw new E10();
		} catch (E10 e) {
			x = 100;
		}
	}

	@Test
	public static void test5() {
		x = 0;
		try {
			m5();
		} catch (E20 e1) {
			x += 1;
		} catch (Exception e) {
			x = 50;
		}
		Assert.assertEquals("test5", 1, x);		
		
		CmdTransmitter.sendDone();
	}

	private static void m5() throws E20 {
		int a = 100;
		try {
			if (a == 100) throw new E20();
			else throw new E10();
		} catch (E10 e) {
			x = 100;
		}
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
		Assert.assertEquals("test1", 1111, a);		
		
		CmdTransmitter.sendDone();
	}
	
	@SuppressWarnings("unused")
	static int m3() throws E1 {
		int a = 1;
		try {
			a += 10;
			throw new E30();
		} catch (E10 e) {
			throw new E1();
		}
	}

	@Test
	public static void testCatchTry2() {
		int a = 1;
		try {
			m3();
		} catch (Exception e) {
			a = -1;
		}
		Assert.assertEquals("test1", -1, a);		
		CmdTransmitter.sendDone();
	}

	static int a;
	
	public static int m60() {
		try {
			return m62();
		} catch (IOException e) {}
		return 10;
	}

	public static int m61() {
		try {
			return m62() + 100;
		} catch (IOException e) {return 20;}
	}

	private static int m62() throws IOException {
		if (a == 0) throw new IOException("IOException");
		return a;
	}

	private static int m63() {
		try {
			return m62();
		} catch (IOException e) {return 100;}
	}
	
	@Test
	public static void testCatchReturn() {
		a = -1;
		Assert.assertEquals("test1", -1, m60());		
		Assert.assertEquals("test2", 99, m61());		
		a = 0;
		Assert.assertEquals("test3", 10, m60());		
		Assert.assertEquals("test4", 20, m61());		
		a = 200;
		Assert.assertEquals("test5", 200, m63());	
		a = 0;
		Assert.assertEquals("test6", 100, m63());	
		
		CmdTransmitter.sendDone();
	}
	



}

@SuppressWarnings("serial")
class E10 extends Exception { }
@SuppressWarnings("serial")
class E20 extends Exception { }
@SuppressWarnings("serial")
class E30 extends E10 { }