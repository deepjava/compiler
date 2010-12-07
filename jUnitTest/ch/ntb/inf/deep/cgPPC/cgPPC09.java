package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class cgPPC09 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T09Types" };
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace},null, (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
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
	public void m1() {
		int[] code = getCode(0);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -32(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, -30000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r5, 10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("subfc  r4, r5, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("subfe  r4, r4, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[GT], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfs  fr1, 24(r4)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[GT], 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r31, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 32"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
//	@Ignore
	@Test
	public void m2() {
		int[] code = getCode(2);
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -48(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r30, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stfd  fr31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r31, r8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("fmr  fr31, fr1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("andi.  r7, r4, 0x7545"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r2, r6, 100"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("extsh  r30, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("twi  ifequal, r5, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lha  r3, -6(r5)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("tw  ifgeU, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r4, r5, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lbzx  r2, r4, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("extsb  r2, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r2, r30, 20"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("extsh  r2, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 13124"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 34"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("or  r3, r31, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 18"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfd  fr1, 24(r4)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("subf  r31, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r2, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("extsh  r2, r31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("fmr  fr1, fr31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lfd  fr31, 20(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r30, 28(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}
	
}
