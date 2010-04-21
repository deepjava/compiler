package ch.ntb.inf.deep.ssa;

public interface SSAValueType {
	int //Type of SSAValue
	t_void = 0, t_object = 3, t_boolean = 4, t_char = 5,
	t_float = 6, t_double = 7, t_byte = 8, t_short = 9,
	t_integer = 10, t_long = 11,t_aobject= 13, t_aboolean =14,
	t_achar = 15, t_afloat = 16, t_adouble = 17, t_abyte = 18,
	t_ashort = 19, t_ainteger = 20, t_along = 21;

}
