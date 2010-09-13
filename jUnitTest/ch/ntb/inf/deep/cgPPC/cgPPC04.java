package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

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

	@Test
	public void doWhile1() {
		int[] code = getCode(1);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("li  r31, 0"), code[1]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r31, r31, 1"), code[2]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 10"), code[3]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("bc  12, 31, 0x8"), code[4]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r30, r31, 1"), code[5]);
		assertEquals("wrong instruction ", InstructionDecoder.getCode("addi  r1, r1, 0x40"), code[6]);
	}
	
}
