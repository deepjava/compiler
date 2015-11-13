package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.cg.ppc.InstructionDecoderPPC;
import ch.ntb.inf.deep.cg.arm.InstructionDecoderARM;


public class TestDecoder {

	public static void main(String[] args) {

		InstructionDecoderARM a = new InstructionDecoderARM();


		
		int code;	// expected code
		String Mn;	// expected Mnemonic
		
		Mn = "adcs R1, R2, #3";	// (immediate)	p.300
		code = 0xe2b21003;		
		Mn = "adds R1, R2, R3, ASR #4";	// (register)	p.312
		code = 0xe0921243;
		Mn = "adds R1, R2, R3";			// (register)	p.312
		code = 0xe0921003;
		Mn = "adds if carry set R1, R2, R3, ASR R4";
		code = 0x20921453;
		Mn = "and R1, R2, R3, ASR R4";	// (register-shifted register) p.328
		code = 0xe0021453;
		Mn = "bic R1, R2, #3";	// (immediate)	p.340
		code = 0xe3c21003;
		Mn = "eor R1, R2, #3";	// (immediate)	p.382
		code = 0xe2221003;
		Mn = "orr R1, R2, #3";	// (immediate)	p.516
		code = 0xe3821003;
		Mn = "rsb R1, R2, #3";	// (immediate)	p.574
		code = 0xe2621003;
		Mn = "rsc R1, R2, #3";	// (immediate)	p.580
		code = 0xe2e21003;
		Mn = "sbc R1, R2, #3";	// (immediate)	p.592
		code = 0xe2c21003;
		Mn = "sub R1, R2, #3";	// (immediate)	p.710
		code = 0xe2421003;

		Mn = "mvns R1, R2, ASR #4";	// (register)	p.506
		code = 0xe1f01242;			
		
		
		Mn = "cmn R1, #2";
		code = 0xe3710002;	// p.364
		Mn = "tst R1, R2, ASR R3";
		code = 0xe1110352;	// p.748
		
		Mn = "asrs R1, R2, #3";
		code = 0xe1b011c2;	// p.330{	// (immediate)
		Mn = "rors R1, R2, R3";	
		code = 0xe1b01372;	// p.570	(register)
		Mn = "rrx R1, R2";	// p.572
		code = 0xe1a01062;

		Mn = "movs R1, #2";	// p.484 A1
		code = 0xe3b01002;
		Mn = "movw R1, #4660";	// p.484 A2
		code = 0xe3011234;
		Mn = "movs R1, R2";	// p.488 (register, ARM)
		code = 0xe1b01002;
		Mn = "movt R1, #4660";	// p.491
		code = 0xe3411234;
		
		Mn = "ldrex R1, [R2]";	// p.432
		code = 0xe1921f9f;
		Mn = "ldrexb R1, [R2]";	// .p434
		code = 0xe1d21f9f;
		Mn = "strexd R1, R2, R3, [R4]";	// p.694
		code = 0xe1a41f92;
		Mn = "strexh R1, R2, [R3]";	// p.694
		code = 0xe1e31f92;
		
		Mn = "swpb R1, R2, [R3]";	// p.722
		code = 0xe1431092;
		
		Mn = "nop if negative";	// p.510
		code = 0x4320f000;
		Mn = "sev";				// p.606
		code = 0xe320f004;
		Mn = "wfe";				// p.1104;
		code = 0xe320f002;
		Mn = "wfi";				// p.1106;
		code = 0xe320f003;
		Mn = "yield";				// p.1108;
		code = 0xe320f001;

		Mn = "b -8";			// p.334
		code = 0xeafffffe;
		Mn = "bl -4";			// p.348	A1
		code = 0xebffffff;
		Mn = "blx -2";			// p.348	(immediate) A2
		code = 0xfbffffff;
		Mn = "blx R1";			// p.350	(register)
		code = 0xe12fff31;
		Mn = "bx R1";			// p.352
		code = 0xe12fff11;
		Mn = "bxj R5";			// p.354
		code = 0xe12fff25;

		Mn = "qadd R1, R2, R3";			// p.540
		code = 0xe1031052;
		Mn = "qdadd R1, R2, R3";		// p.548
		code = 0xe1431052;
		Mn = "qdsub R1, R2, R3";		// p.550
		code = 0xe1631052;
		Mn = "qsub R1, R2, R3";			// p.554
		code = 0xe1231052;

		Mn = "bkpt #19";		// p.346
		code = 0xe1200173;
		Mn = "hvc #19";		// p.1984
		code = 0xe1400173;
		
		Mn = "clz R1, R2";	// p.362
		code = 0xe16f1f12;

		Mn = "eret";	// p.1982
		code = 0xe160006e;

		Mn = "smc #1";	// p.2002
		code = 0xe1600071;

		Mn = "ldr R1, [R2, #-3]";	// p.408 (imm) offset
		code = 0xe5121003;
		Mn = "ldr R1, [R2, #+0]";	// p.408 (imm) offset
		code = 0xe5921000;
		Mn = "ldr R1, [R2]";		// p.408 (imm) offset
		code = 0xe5921000;
		Mn = "ldr R1, [R2, #3]!";	// p.408 (imm) Pre-index
		code = 0xe5b21003;
		Mn = "ldr R1, [R2], #-3";	// p.408 (imm) Post-index
		code = 0xe4121003;
//		Mn = "ldrb R1, -2";			// p.420 (literal) offset
//		code = 0xe4121003;
		Mn = "ldrb R1, [R2, -R3, LSR #4]";	// p.422 (register) offset
		code = 0xe7521223;
		Mn = "strb R1, [R2], R3";	// p.682 (register) Post-index
		code = 0xe6e21003;
		Mn = "ldrt R1, [R2], -R3, LSR #4";	// p.466 A2
		code = 0xe6321223;
		Mn = "strbt R1, [R2], -R3, LSR #4";	// p.684 A1
		code = 0xe6621223;

		Mn = "ldm R1!, R0, R4, PC";	// p.398
		code = 0xe8b18011;
		Mn = "stmda R1, R0, R4, PC";	// p.666
		code = 0xe8018011;

		Mn = "pop R0, R4, PC";		// p.536 A1
		code = 0xe8bd8011;
		Mn = "push R0, R4, PC";		// p.538 A1
		code = 0xe92d8011;

		Mn = "rfeia R1!";		// p.2000
		code = 0xf8b10a00;
		Mn = "srsib SP, #fiq";		// p.2006
		code = 0xf9cd0511;
		
		

		System.out.print("expected Mnemonic:	");		
		System.out.println(Mn);
		System.out.print("decoded Mnemonic:	");		
		System.out.print(a.getMnemonic(code));
		System.out.println("		" + Mn.equals(a.getMnemonic(code)));
		
		System.out.print("original code:		");
		System.out.println("0x" + Integer.toHexString(code));
		System.out.print("calculated code:	");
		System.out.print("0x" + Integer.toHexString(a.getCode(Mn)));
//		System.out.print("0x" + Integer.toHexString(a.getCode(a.getMnemonic(code))));
		System.out.println("			" + (a.getCode(Mn) == code));
		
		
//		System.out.println("0x" + Integer.toHexString(code));
//		System.out.println("0x" + Integer.toHexString(a.getCode(a.getMnemonic(code))));
//		System.out.println("0x" + Integer.toHexString(code));
//		System.out.println(InstructionDecoderARM.encodeShift(3,0));
		
	
//		// imm16 test
//		int code1 = 0x1;
//		int code2 = 0x76543210;
//		int code2 = 0x20000001;
//		int code3 = (code2 << 4) + code1;
//		System.out.println(Integer.toHexString(code3));
//		System.out.println(Integer.toString(code2));
//		System.out.println(Integer.toHexString(code2));
//		int code3 = ((code2<<2)>>1);
//		System.out.println(Integer.toString((code2<<2)>>1));
//		System.out.println(Integer.toHexString(code3));
//		System.out.println(Integer.toString(Integer.rotateRight(code2, 0),16));
//		System.out.println(Integer.toString(Integer.rotateRight(code2, 4),16));
//		System.out.println(Integer.toString(Integer.rotateRight(code2, 8),16));
//		
//		int m = code3 & 0xf;
//		int imm16 = (((int)((code3 >>> 8) & 0xfff))<<4) + m;
//		System.out.println(Integer.toHexString(m));
//		System.out.println(Integer.toHexString(imm16));
//		
//		int test = 0x4321;
//		System.out.println((test>>>4) & 0x3);
//		switch ((test>>>4) & 0x3) {
//		case 0: System.out.println("t0"); break;
//		case 1: System.out.println("t1"); break;
//		case 2: System.out.println("t2"); break;
//		case 3: System.out.println("t3"); break;
//		case 4: System.out.println("t4"); break;
//		}
//		System.out.println("t5");
////		
//		String str = "abc";
//		System.out.println(str);
//		System.out.println(str.contains("bc"));
//		System.out.println(str.contains("abcdgsdfgdsg"));
//		
//		
//
//		
//		String mnemonic = "  Ands R1, R2, R2, R4 ";
//		
//		
//		code=0;
//		
//		mnemonic = mnemonic.toLowerCase();
//		mnemonic = mnemonic.trim();
//		
//		String[] parts =  mnemonic.split(" ");	// Split mnemonic by spaces
//		System.out.println(parts.length);
//		if (parts[4].substring(0,1).equals("#")) {	// (immediate)
//			System.out.println("A");
//		}
//
//		int a0 = 0x1;
//		a0 |= 0x2;
//		System.out.println(a0);
//		
//		
//		
//		
//		
//		
//		
//		if (parts[0].substring(0, 3).equals("and")) System.out.println("true1");
//		if (parts[1].substring(0, 3).equals("add")) System.out.println("true2");
//		System.out.println(Integer.parseInt("R19".replaceAll("[^0-9]", "")));
//	

	}
}
