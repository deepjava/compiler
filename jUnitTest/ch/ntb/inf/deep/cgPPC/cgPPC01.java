package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.*;

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
	public void emptyMethodStatic() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void emptyMethod() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void testAssignment1() {
		int[] code = getCode(3);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0x1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void testSimple1() {
		int[] code = getCode(4);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
	@Test
	public void testSimple4() {
		int[] code = getCode(7);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, -25536"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 14464"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, -31072"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, 32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 21888"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, -100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, -10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 25536"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, -14464"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 31072"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -32767"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, -21888"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r31, -32768"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
}
