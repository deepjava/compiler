package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC01 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T01SimpleMethods" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},Configuration.getSystemPrimitives(), (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Type.nofRootClasses > 0) {
			createCgPPC(Type.rootClasses[0]);
		}
	}

	@Test
	public void emptyMethodStatic() {
		int[] code = getCode("emptyMethodStatic");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void emptyMethod() {
		int[] code = getCode("emptyMethod");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void assignment1() {
		int[] code = getCode("assignment1");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	//	@Ignore
	@Test 
	public void assignment2() {
		createCgPPC1(Type.rootClasses[0],"assignment2");
		assertTrue("wrong join", checkJoin(getJoin(0), 3, 7, vol, false));
		assertNull("wrong join", getJoin(1));
	}
	
	//	@Ignore
	@Test 
	public void assignment3() {
		createCgPPC1(Type.rootClasses[0],"assignment3");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, false));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 25, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 16, 23, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	@Test
	public void simple1() {
		int[] code = getCode("simple1");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r4, r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r4, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r4, r3, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void simple2() {
		int[] code = getCode("simple2");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r3, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r4, r3, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r2, r4, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void simple3() {
		int[] code = getCode("simple3");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r4, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r2, r4, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void simple4() {
		int[] code = getCode("simple4");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -25536"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 14464"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -31072"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 21888"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 25536"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -14464"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 31072"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -21888"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r2, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void simple5() {
		int[] code = getCode("simple5");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 17493"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 8755"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, -30567"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 26232"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 30566"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, -26232"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 13090"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 21828"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void simple6() {
		int[] code = getCode("simple6");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfs  fr1, 0(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfd  fr1, 0(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfd  fr1, 0(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 9"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r3, 0(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
}
