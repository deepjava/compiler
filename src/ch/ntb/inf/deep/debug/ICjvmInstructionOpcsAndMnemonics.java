package ch.ntb.inf.deep.debug;

import ch.ntb.inf.deep.classItems.ICjvmInstructionOpcs;

public interface ICjvmInstructionOpcsAndMnemonics extends ICjvmInstructionOpcs{
	String[] bcAttributes = { // index = bcapX - bcapBase
		"Branch",
		"CondBranch",
		"UncondBranch",
		"Return",
		"Switch",
		"Call",
		"New",
		"CpRef",// rferences const pool item
		"Undef"
	};

	String[] bcMnemonics = {
			"nop",

			"aconst_null",

			"iconst_m1",
			"iconst_0",
			
			"iconst_1",
			"iconst_2",
			"iconst_3",
			"iconst_4",
			"iconst_5",

			"lconst_0",
			"lconst_1",

			"fconst_0",
			"fconst_1",
			"fconst_2",

			"dconst_0",
			"dconst_1",

			"bipush",
			"sipush",

			"ldc",
			"ldc_w",
			"ldc2_w",

			"iload",
			"lload",
			"fload",
			"dload",
			"aload",

			"iload_0",
			"iload_1",
			"iload_2",
			"iload_3",

			"lload_0",
			"lload_1",
			"lload_2",
			"lload_3",

			"fload_0",
			"fload_1",
			"fload_2",
			"fload_3",

			"dload_0",
			"dload_1",
			"dload_2",
			"dload_3",

			"aload_0",
			"aload_1",
			"aload_2",
			"aload_3",

			"iaload",
			"laload",
			"faload",
			"daload",
			"aaload",

			"baload",
			"caload",
			"saload",

			"istore",
			"lstore",
			"fstore",
			"dstore",
			"astore",

			"istore_0",
			"istore_1",
			"istore_2",
			"istore_3",

			"lstore_0",
			"lstore_1",
			"lstore_2",
			"lstore_3",

			"fstore_0",
			"fstore_1",
			"fstore_2",
			"fstore_3",

			"dstore_0",
			"dstore_1",
			"dstore_2",
			"dstore_3",

			"astore_0",
			"astore_1",
			"astore_2",
			"astore_3",

			// ---- store top of stack into array
			"iastore",
			"lastore",
			"fastore",
			"dastore",
			"aastore",
			"bastore",
			"castore",
			"sastore",

			"pop",
			"pop2",

			"dup",
			"dup_x1",
			"dup_x2",
			"dup2",
			"dup2_x1",
			"dup2_x2",

			"swap",

			"iadd",
			"ladd",
			"fadd",
			"dadd",

			"isub",
			"lsub",
			"fsub",
			"dsub",

			"imul",
			"lmul",
			"fmul",
			"dmul",

			"idiv",
			"ldiv",
			"fdiv",
			"ddiv",

			"irem",
			"lrem",
			"frem",
			"drem",

			"ineg",
			"lneg",
			"fneg",
			"dneg",

			"ishl",
			"lshl",
			"ishr",
			"lshr",
			"iushr",
			"lushr",

			"iand",
			"land",
			"ior",
			"lor",
			"ixor",
			"lxor",

			"iinc",

			"i2l",
			"i2f",
			"i2d",

			"l2i",
			"l2f",
			"l2d",

			"f2i",
			"f2l",
			"f2d",

			"d2i",
			"d2l",
			"d2f",

			"i2b",
			"i2c",
			"i2s",

			"lcmp",
			"fcmpl",
			"fcmpg",
			"dcmpl",
			"dcmpg",

			"ifeq",
			"ifne",
			"iflt",
			"ifge",
			"ifgt",
			"ifle",

			"if_icmpeq",
			"if_icmpne",
			"if_icmplt",
			"if_icmpge",
			"if_icmpgt",
			"if_icmple",

			"if_acmpeq",
			"if_acmpne",
		
			"goto",

			"jsr",

			"ret",

			"tableswitch",
			"lookupswitch",

			"ireturn",
			"lreturn",
			"freturn",
			"dreturn",
			"areturn",

			"return",

			"getstatic",
			"putstatic",
			"getfield",
			"putfield",

			"invokevirtual",
			"invokespecial",
			"invokestatic",
			"invokeinterface",

			"xxxunusedxxx",

			"new",
			"newarray",
			"anewarray",

			"arraylength",
			
			"athrow",

			"checkcast",
			"instanceof",

			"monitorenter",
			"monitorexit",

			"wide",

			"multianewarray",

			"ifnull",
			"ifnonnull",

			"goto_w",
			"jsr_w",

			"breakpoint",

			"unused203",
			"unused204",	"unused205",   "unused206",   "unused207",
			"unused208",   "unused209",   "unused210",   "unused211",
			"unused212",   "unused213",   "unused214",   "unused215",
			"unused216",   "unused217",   "unused218",   "unused219",
			"unused220",   "unused221",   "unused222",   "unused223",
			"unused224",   "unused225",   "unused226",   "unused227",
			"unused228",   "unused229",   "unused230",   "unused231",
			"unused232",   "unused233",   "unused234",   "unused235",
			"unused236",   "unused237",   "unused238",   "unused239",
			"unused240",   "unused241",   "unused242",   "unused243",
			"unused244",   "unused245",   "unused246",   "unused247",
			"unused248",   "unused249",   "unused250",   "unused251",
			"unused252",   "unused253",
			
			"impdep1",
			"impdep2"
	};
}
