package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPCKernel extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/TKernel" };
		Configuration.parseAndCreateConfig("C:/NTbcheckout/EUser/JCC/Deep/ExampleProject.deep","BootFromRam");
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},Configuration.getSystemPrimitives(), (1 << atxCode)
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
	public void blink() {
		int[] code = getCode("blink");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -16(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, -31072"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r5, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r5, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r5, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r5, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r5, r5, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r5, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r4, r4, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r4, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 16960"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 15"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mulli  r5, r2, 10000"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("subf  r4, r5, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r3, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -24"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr  always, 0"), code[i++]);
	}

//	@Ignore
	@Test
	public void boot() {
		int[] code = getCode("boot");
		int i = 0;
		assertEquals("wrong instruction", InstructionDecoder.getCode("stwu  r1, -48(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mfspr  r0, LR"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stw  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("stmw  r26, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);		
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lis  r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("and  r2, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], -36"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 11"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, -32736"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("and  r2, r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 24"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 21"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 5"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 31"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mulli  r2, r2, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r31, r2, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r30, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 7"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 37"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r29, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mulli  r28, r31, 8"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r27, r28, 12"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r2, r28, 16"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("add  r26, r27, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  36"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r2, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 46"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r3, r3, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r3, r2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r27"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);	
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r27, r27, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r27, r26"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[LT], -36"), code[i++]);		
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r27, r28, 20"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r30, 2"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 58"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);		
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmpi  crf0, 0, r27, -1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iffalse, CRF0[EQ], 36"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("cmp  crf0, 0, r27, r29"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bc  iftrue, CRF0[EQ], 28"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r3, 6"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("li  r4, 69"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addis  r4, r4, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lr  r2, r3"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclrl  always, 0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r30, r30, 1"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r3, r28, 4"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("b  -140"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lmw  r26, 12(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("lwz  r0, 44(r1)"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("mtspr  LR, r0"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("addi  r1, r1, 48"), code[i++]);
		assertEquals("wrong instruction", InstructionDecoder.getCode("bclr always, 0"), code[i++]);
	}

}
