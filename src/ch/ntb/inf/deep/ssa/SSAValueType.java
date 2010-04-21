package ch.ntb.inf.deep.ssa;

public interface SSAValueType {
	int //Type of SSAValue
	t_void = 0, t_object = 1, t_boolean = 4, t_char = 5,
	t_float = 6, t_double = 7, t_byte = 8, t_short = 9,
	t_integer = 10, t_long = 11;

}
