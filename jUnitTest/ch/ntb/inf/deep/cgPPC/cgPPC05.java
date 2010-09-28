package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC05 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T05Returns" };
		try {
			Class.buildSystem(rootClassNames, (1 << IClassFileConsts.atxCode)
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

	@Test
	public void multipleReturns1() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 0x2c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x38"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 22"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void multipleReturns2() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 22"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x60"), code[i++]);
		
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 22"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
}
