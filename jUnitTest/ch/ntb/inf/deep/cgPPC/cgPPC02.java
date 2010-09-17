package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC02 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T02Branches" };
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
	public void testIf1() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[GT], 0x20"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x24"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r31, -1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
	}
	
	@Test
	public void testIf2() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R30, 2"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmp  crf0, 0, r30, R31"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iftrue, CRF0[GT], 0x28"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r30, 6"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x2c"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r30, 8"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
	}
	
	@Test
	public void testIf3() {
		int[] code = getCode(3);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r27, 20(r1)"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R31, 0"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 2"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R29, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R28, 2"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("subf  r27, r28, r29"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r27, r27, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
	}
	
	@Test
	public void testIf4() {
		int[] code = getCode(4);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r28, 20(r1)"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R29, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R28, 0"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 2"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x50"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R29, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  R28, 2"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("subf  r27, r28, r29"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r27, r27, 1"), code[i++]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
	}
	
}
