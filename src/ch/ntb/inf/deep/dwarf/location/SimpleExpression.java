package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class SimpleExpression implements DwarfExpression {

	private final DwOpType expression;

	public SimpleExpression(DwOpType expression) {
		this.expression = expression;
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.put((byte) 1); // Length 1
		buf.put(expression.value());
	}
}
