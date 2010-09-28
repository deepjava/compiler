package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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

//	@Ignore
	@Test
	public void doWhile1() {
		int[] code = getCode(1);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x14"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
//	@Ignore
	@Test
	public void doWhileIf1() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r28, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, -6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r30, r30, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r29, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x34"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x44"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r28, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r28, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 0x58"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r28, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 0x18"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 0x68"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x6c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r28, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void while1() {
		int[] code = getCode(3);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x1c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x18"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void whileTrue() {
		int[] code = getCode(4);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x14"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void whileTrueBreak() {
		int[] code = getCode(5);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void whileMultiCond() {
		int[] code = getCode(6);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x1c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x2c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[LT], 0x18"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void for1() {
		int[] code = getCode(7);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 10"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x1c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void forWhile() {
		int[] code = getCode(8);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r29, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r30, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r29, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[GT], 0x20"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r29, r29, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r29, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r30"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r29, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void forIfWhile() {
		int[] code = getCode(9);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x38"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 50"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[GT], 0x34"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  0x2c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 75"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r30, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], 0x1c"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
}
