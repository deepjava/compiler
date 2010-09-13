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
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[1]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  4, 30, 0x14"), code[2]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r31, 1"), code[3]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("b  0x18"), code[4]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("subfic  r30, r31, 1"), code[5]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 0x40"), code[6]);
	}
	
}
