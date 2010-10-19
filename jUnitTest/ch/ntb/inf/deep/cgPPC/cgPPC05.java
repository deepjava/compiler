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
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T05Returns" };
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

	@Test
	public void multipleReturns1() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 22"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
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
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 22"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  72"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  56"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  40"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
}
