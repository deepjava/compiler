package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class LineNrSSAInstrPair implements SSAInstructionMnemonics {
	public int bca;
	public int lineNr;
	public SSAInstruction instr;
	
	public LineNrSSAInstrPair(int bca, int lineNr, SSAInstruction instr){
		this.bca = bca;
		this.lineNr = lineNr;
		this.instr = instr;
	}
	
	@Override
	public String toString(){
		if(instr == null)
			return "Pc: " + bca + " <=> " + "Line: " + lineNr + " has no SSAInstruction";
		if(instr.machineCodeOffset == -1)
			return "Pc: " + bca + " <=> " + "Line: " + lineNr + " <=> " + instr.toString();
		return "Pc: " + bca + " <=> " + "Line: " + lineNr + " <=> " + instr.toString() + " <=> CodeOffset " + instr.machineCodeOffset;
	}

}
