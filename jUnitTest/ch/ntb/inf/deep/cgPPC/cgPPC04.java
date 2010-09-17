package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.*;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC04 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T04Loops" };
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

	@Ignore
	@Test
	public void doWhile1() {
		int[] code = getCode(1);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r31, 0"), code[1]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r31, 1"), code[2]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 10"), code[3]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  12, 31, 0x8"), code[4]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r31, 1"), code[5]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 64"), code[6]);
	}
	
	@Test
	public void doWhileIf1() {
		int[] code = getCode(2);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("mfspr  r0, LR"), code[1]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stw  r0, 64(r1)"), code[2]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stmw  r29, 52(r1)"), code[3]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r31, 0"), code[4]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r30, -6"), code[5]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r31, 1"), code[6]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("add  r30, r30, r31"), code[7]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 10"), code[8]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x30"), code[9]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r29, 1"), code[10]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x34"), code[11]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r29, 0"), code[12]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 5"), code[13]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x44"), code[14]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r28, 1"), code[15]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x48"), code[16]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r28, 0"), code[17]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[18]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 0x58"), code[19]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r28, 0"), code[20]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x18"), code[21]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[22]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 0x68"), code[23]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r31, -1"), code[24]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x6c"), code[25]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r31, 1"), code[26]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 64"), code[27]);
	}
	
}
