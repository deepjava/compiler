package ch.ntb.inf.deep.dwarf;

public interface DieVisitor {
	
	public void visit(BaseTypeDIE die);
	public void visit(CompilationUnitDIE die);
	public void visit(SubProgramDIE die);
	public void visit(VariableDIE die);
}
