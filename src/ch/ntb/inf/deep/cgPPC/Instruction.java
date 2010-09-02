package ch.ntb.inf.deep.cgPPC;

public class Instruction implements InstructionOpcs{

	/**
	 * integer representation of the machine instruction.
	 */
	private int instruction;

	/**
	 * debug text for the machine-instruction.
	 */
	private String debugText;

	public static int createInstructionSSD(int opCode, int sReg1, int sReg2, int dReg) {
		return opCode | (sReg1 << 16) | (sReg2 << 11) | (dReg << 21);
	}

	public static int createInstructionSDI(int opCode, int sReg, int dReg, int immVal) {
//		System.out.println(Integer.toHexString(opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff)));
		return opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff);
	}


	/**
	 * setter for the SH-Part of the MPC555-Instruction.
	 * 
	 * @param sh
	 *            SH-Part of the Instruction
	 */
	public final void setSH(int sh) {
		sh = sh & 0x1F; // keep only 5 bits
		instruction = instruction & (0xFFFF07FF);
		instruction |= (sh << (31 - 20));
	}

	/**
	 * setter for the Opcode-Part of the MPC555-Instruction.
	 * 
	 * @param opcode
	 *            Opcode-Part of the Instruction
	 */
	public void setOpcode(int opcode) {
		opcode = opcode & 0x3F; // keep only 6 Bits
		instruction |= (opcode << 26);
	}

	/**
	 * getter for the Opcode-Part of the MPC555-Instruction.
	 * 
	 * @return opcode Opcode-Part of the Instruction
	 */
	public int getOpcode() {
		return (instruction >>> 26) & 0x3F;
	}

	/**
	 * setter for the LI-Part of the MPC555-Instruction.
	 * 
	 * @param Li
	 *            LI-Part of the Instruction
	 */
	public final void setLi(int Li) {
		Li = Li & 0x3FFFFFC; // keep only 24 bits
		instruction = instruction & (0xFC000003); // clear Li Part
		instruction |= Li;
	}

	/**
	 * setter for the TO-Part of the MPC555-Instruction.
	 * 
	 * @param to
	 *            TO-Part of the Instruction
	 */
	public final void setTO(int to) {
		to = to & 0x1F; // keep only 5 bits
		instruction |= (to << (31 - 10));
	}

	/**
	 * setter for the rA-Part of the MPC555-Instruction.
	 * 
	 * @param rA
	 *            rA-Part of the Instruction
	 */
	public final void setrA(int rA) {
		rA = rA & 0x1F; // keep only 5 bits
		instruction |= (rA << (31 - 15));
	}

	/**
	 * setter for the crfD-Part of the MPC555-Instruction.
	 * 
	 * @param crfD
	 *            LI-Part of the Instruction
	 */
	public final void setcrfD(int crfD) {
		crfD = crfD & 0x07; // keep only 3 bits
		instruction |= (crfD << (31 - 8));
	}

	/**
	 * setter for the rB-Part of the MPC555-Instruction.
	 * 
	 * @param rB
	 *            rB-Part of the Instruction
	 */
	public final void setrB(int rB) {
		rB = rB & 0x1F;
		instruction |= (rB << (31 - 20));
	}

	/**
	 * setter for the rS-Part of the MPC555-Instruction.
	 * 
	 * @param rS
	 *            rS-Part of the Instruction
	 */
	public final void setrS(int rS) {
		rS = rS & 0x1F;
		instruction |= (rS << (31 - 10));
	}

	/**
	 * setter for the rD-Part of the MPC555-Instruction.
	 * 
	 * @param rD
	 *            rD-Part of the Instruction
	 */
	public final void setrD(int rD) {
		setrS(rD);
	}

	/**
	 * setter for the UIMM-Part of the MPC555-Instruction.
	 * 
	 * @param uimm
	 *            UIMM-Part of the Instruction
	 */
	public final void setUIMM(int uimm) {
		setSIMM(uimm);
	}

	/**
	 * setter for the AA-Part of the MPC555-Instruction.
	 * 
	 * @param aa
	 *            AA-Part of the Instruction
	 */
	public final void setAA(int aa) {
		aa = aa & 0x01; // only 1 bit
		instruction |= (aa << (31 - 30)); // shift to 2nd position
	}

	/**
	 * setter for the LK-Part of the MPC555-Instruction.
	 * 
	 * @param lk
	 *            LK-Part of the Instruction
	 */
	public final void setLK(int lk) {
		lk = lk & 0x01; // only 1 bit
		instruction |= lk;
	}

	/**
	 * setter for the L-Part of the MPC555-Instruction.
	 * 
	 * @param l
	 *            L-Part of the Instruction
	 */
	public final void setL(int l) {
		l = l & 0x01; // only 1 bit
		instruction |= (l << (31 - 9));
	}

	/**
	 * setter for the SIMM-Part of the MPC555-Instruction.
	 * 
	 * @param simm
	 *            SIMM-Part of the Instruction
	 */
	public final void setSIMM(int simm) {
		simm = simm & 0xFFFF;
		instruction &= 0xFFFF0000; // Clear 16 Bit
		instruction |= simm;
	}

	/**
	 * setter for the OE-Part of the MPC555-Instruction.
	 * 
	 * @param oe
	 *            OE-Part of the Instruction
	 */
	public final void setOE(int oe) {
		oe = oe & 0x1;
		instruction |= (oe << 10);
	}

	/**
	 * setter for the D-Part of the MPC555-Instruction.
	 * 
	 * @param d
	 *            D-Part of the Instruction
	 */
	public final void setD(int d) {
		d = d & 0xFFFF;
		instruction &= 0xFFFF0000; // Clear 16 Bit
		instruction |= d;
	}

	/**
	 * setter for the rC-Part of the MPC555-Instruction.
	 * 
	 * @param rC
	 *            rC-Part of the Instruction
	 */
	public final void setrC(int rC) {
		rC = rC & 0x1;
		instruction |= rC;
	}

	/**
	 * setter for the BO-Part of the MPC555-Instruction.
	 * 
	 * @param bo
	 *            BO-Part of the Instruction
	 */
	public final void setBO(int bo) {
		bo = bo & 0x1F; // keep 5 bits
		instruction |= (bo << (31 - 10));
	}

	/**
	 * setter for the BI-Part of the MPC555-Instruction.
	 * 
	 * @param bi
	 *            BI-Part of the Instruction
	 */
	public final void setBI(int bi) {
		bi = bi & 0x1F;
		instruction |= (bi << (31 - 15));
	}

	/**
	 * setter for the secondary Opcode (21-30) of the MPC555-Instruction.
	 * 
	 * @param xo
	 *            secondary Opcode (21-30) of the Instruction
	 */
	public final void setXO21to30(int xo) {
		xo = xo & 0x3FF;
		instruction |= (xo << 1);
	}

	/**
	 * setter for the secondary Opcode (22-30) of the MPC555-Instruction.
	 * 
	 * @param xo
	 *            secondary Opcode (22-30) of the Instruction
	 */
	public final void setXO22to30(int XO) {
		XO = XO & 0x1FF;
		instruction |= (XO << 1);
	}

	/**
	 * setter for the secondary Opcode (26-30) of the MPC555-Instruction.
	 * 
	 * @param xo
	 *            secondary Opcode (26-30) of the Instruction
	 */
	public final void setXO26to30(int XO) {
		XO = XO & 0x1F;
		instruction |= (XO << 1);
	}

	/**
	 * setter for the secondary Opcode (30) of the MPC555-Instruction.
	 * 
	 * @param xo
	 *            secondary Opcode (30) of the Instruction
	 */
	public final void setXO30(int XO) {
		XO = XO & 0x1;
		instruction |= (XO << 1);
	}

	/**
	 * setter for the CRM-Part of the MPC555-Instruction.
	 * 
	 * @param crm
	 *            CRM-Part of the Instruction
	 */
	public void setCRM(int crm) {
		crm = crm & 0xFF;
		instruction |= (crm << (31 - 19));
	}

	/**
	 * setter for the SPR-Part of the MPC555-Instruction.
	 * 
	 * @param spr
	 *            SPR-Part of the Instruction
	 */
	public final void setSPR(int spr) {
		spr = spr & 0x3FF;
		switch (spr) {
			case 1:
				instruction |= Integer.valueOf("0000100000", 2) << (31 - 20);
				break;
			case 8:
				instruction |= Integer.valueOf("0100000000", 2) << (31 - 20);
				break;
			case 9:
				instruction |= Integer.valueOf("0100100000", 2) << (31 - 20);
				break;
			case 268:
				instruction |= Integer.valueOf("0110001000", 2) << (31 - 20);
				break;
			case 269:
				instruction |= Integer.valueOf("0110101000", 2) << (31 - 20);
				break;
			case 18:
				instruction |= Integer.valueOf("1001000000", 2) << (31 - 20);
				break;
			case 19:
				instruction |= Integer.valueOf("1001100000", 2) << (31 - 20);
				break;
			case 22:
				instruction |= Integer.valueOf("1011000000", 2) << (31 - 20);
				break;
			case 26:
				instruction |= Integer.valueOf("1101000000", 2) << (31 - 20);
				break;
			case 27:
				instruction |= Integer.valueOf("1101100000", 2) << (31 - 20);
				break;
			case 80:
				instruction |= Integer.valueOf("1000000010", 2) << (31 - 20);
				break;
			case 81:
				instruction |= Integer.valueOf("1000100010", 2) << (31 - 20);
				break;
			case 82:
				instruction |= Integer.valueOf("1001000010", 2) << (31 - 20);
				break;
			case 272:
				instruction |= Integer.valueOf("1000001000", 2) << (31 - 20);
				break;
			case 273:
				instruction |= Integer.valueOf("1000101000", 2) << (31 - 20);
				break;
			case 274:
				instruction |= Integer.valueOf("1001001000", 2) << (31 - 20);
				break;
			case 275:
				instruction |= Integer.valueOf("1001101000", 2) << (31 - 20);
				break;
			case 284:
				instruction |= Integer.valueOf("1110001000", 2) << (31 - 20);
				break;
			case 285:
				instruction |= Integer.valueOf("1110101000", 2) << (31 - 20);
				break;
			case 287:
				instruction |= Integer.valueOf("1111101000", 2) << (31 - 20);
				break;
			case 560:
				instruction |= Integer.valueOf("1000010001", 2) << (31 - 20);
				break;
			case 562:
				instruction |= Integer.valueOf("1001010001", 2) << (31 - 20);
				break;
			case 1022:
				instruction |= Integer.valueOf("1111011111", 2) << (31 - 20);
				break;
		}
	}

	@Override
	public String toString() {
		String mask = "00000000000000000000000000000000";
		String ds = Integer.toBinaryString(instruction);
		String z = mask.substring(0, mask.length() - ds.length()) + ds;
		String hex = Integer.toHexString(instruction);

		return z + "  [" + hex + "] (" + debugText + ")";
	}

	public final void setDebugText(String debugText) {
		this.debugText = debugText;
	}

	public Integer get() {
		return instruction;
	}

	public String getDebugText() {
		return debugText;
	}

}
