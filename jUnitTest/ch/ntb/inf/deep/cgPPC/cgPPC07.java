package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC07 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T07Arrays" };
		try {
			Class.buildSystem(rootClassNames,workspace, (1 << IClassFileConsts.atxCode)
					| (1 << IClassFileConsts.atxLocalVariableTable)
					| (1 << IClassFileConsts.atxLineNumberTable)
					| (1 << IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Type.nofRootClasses > 0) {
			createCgPPC(Type.rootClasses[0]);
		}
	}

//	@Ignore
	@Test
	public void emptyIntArray() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifless, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
//	@Ignore
	@Test
	public void intArray() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifless, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  36"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r4, r3, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r5, 8(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r3, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r5, r3, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r6, r2, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r4, r5, r6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  CRF0, 0, r3, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -44"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 2"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r3, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r3, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r2, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwzx  r2, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
//	@Ignore
	@Test
	public void stringArray() {
		int[] code = getCode(3);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifless, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 9"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r5, 8(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r3, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r3, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 14"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r3, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 23"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r3, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r3, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 41"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r4, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r4, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r3, r4, r5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r3, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r3, r2, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r4, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwzx  r2, r3, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void objectArray() {
		int[] code = getCode(4);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r29, 8(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifless, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 9"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 14"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r29, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  R2, -4(r29)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  R2, -24(r2)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr LR, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r2, 8(r31)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r30, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("rlwinm  r2, r30, 2, 0, 29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r31, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwx  r29, r2, r3"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r29, 8(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

}
