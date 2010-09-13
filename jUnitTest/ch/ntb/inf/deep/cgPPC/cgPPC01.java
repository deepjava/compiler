package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC01 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T01SimpleMethods" };
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
	public void testConstructor() {
		int[] code = getCode(0);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bl  0x0"), code[1]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 0x40"), code[2]);
	}
	
	@Test
	public void testAssignment1() {
		int[] code = getCode(3);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0x1"), code[1]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 0x40"), code[2]);
	}
	
	@Test
	public void testSimple1() {
		int[] code = getCode(4);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[0]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0x0"), code[1]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi r30, r30, 0x1"), code[2]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 0x3"), code[3]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 0xff"), code[4]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r31, 0x1"), code[5]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 0x40"), code[6]);
	}
	
}
