package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC06 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T06Operators" };
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
	public void conditionalOperator1() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -48(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r27, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r28, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r27, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 101"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 102"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r28, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r27, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 103"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r27, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
//	@Ignore
	@Test
	public void conditionalOperator2() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -64(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 60(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r23, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r28, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r27, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r26, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r25, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r24, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 101"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r26, r25"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iffalse, CRF0[LT], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r26"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 102"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r26, r25"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iffalse, CRF0[LT], 28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r26"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r25"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r28, r27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r25"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r23, r24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 103"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r31, r23"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iffalse, CRF0[GT], 20"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc iftrue, CRF0[EQ], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r23, 16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 60(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 64"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
}
