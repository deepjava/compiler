package ch.ntb.inf.junitTarget.comp.statements;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ DoWhileTest.class, ForTest.class, IfTest.class, SwitchTest.class, WhileTest.class})

@MaxErrors(500)
public class StatementSuite {

}
