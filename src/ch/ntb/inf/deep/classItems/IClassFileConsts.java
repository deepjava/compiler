package ch.ntb.inf.deep.classItems;

public interface IClassFileConsts {

	byte//--- constant pool tags (cpt), mnemonis in: debug.IClassFileConstMnemonics
		cptExtSlot = 0,
		cptUtf8 = 1,
		cptInteger = 3, cptFloat = 4, cptLong = 5, cptDouble = 6,
		cptClass = 7, cptString = 8, cptFieldRef = 9, cptMethRef = 10,
		cptIntfMethRef = 11, cptNameAndType = 12;

	byte//--- access and property flags (apf) for class, field, method, mnemonis in: debug.IClassFileConstMnemonics
		apfPublic = 0, // 0x001, class, field, method
		apfPrivate = 1, // 0x002, ----, field, method
		apfProtected = 2, // 0x004, ----, field, method
		apfStatic = 3, // 0x008, ----, field, method
		apfFinal = 4, // 0x010, class, field, method

		apfSuper = 5, // 0x020, class
		apfSynchronized = 5, // 0x020, method
		
		apfVolatile = 6, // 0x040, field
		apfTransient = 7, // 0x080, field
		apfNative = 8, // 0x100, method
		
		apfInterface = 9, // 0x200, class (declared as interface in source)
		apfAbstract = 10, // 0x400, class, method
		apfStrict = 11, // 0x800, method,  strictfp: floating-point mode is FP-strict

		apfEnumArray = 12, // 0x1000, flag of enum arrays (Enum[] ENUM$VALUES) and enum switch table (int[] $SWITCH_TABLE$...)
		apfEnum = 14, // 0x4000, enum (class)
		
		dpfBase = 16; // base flag number of deep property flags
	byte// deep properties ( < 32 => (dpOffeset < 32-dpfBase) ), mnemonis in: debug.IClassFileConstMnemonics
		dpfClassLoaded = dpfBase+0, // 16, 0x1'0000, class loaded
		dpfRootClass = dpfBase+1, // 17, 0x2'0000, this class is a root class (loaded by loadRootClass(..) )
	
		dpfReadAccess = dpfBase+2, // 18, 0x4'0000, one or more read accesses to this item
		dpfWriteAccess = dpfBase+3, // 19, 0x8'0000, one or more write accesses to this item

		dpfCall = dpfBase+4, // 20, 0x10'0000, method gets called by the bc instruction invokestatic or invokevirtual
		dpfInterfCall = dpfBase+5, // 21, 0x20'0000, method gets invoked by the bc instruction invokeinterface

		dpfDeclaration = dpfBase+6, // 22, 0x40'0000, is used for declarations of static fields, instance fields or local variables
		dpfNew = dpfBase+7, // 23, 0x80'0000, there might be objects of this type in the running system
		dpfTypeTest = dpfBase+8, // 24, 0x100'0000, there are type tests with this type (instructions: checkcast, instanceof)
		
		dpfConst = dpfBase+8, // 25, 0x200'0000, constant field
		dpfDeprecated = dpfBase+9, // 26, 0x400'0000, deprecated field or method
		dpfSynthetic = dpfBase+10, // 27, 0x800'0000, synthetic field or method
		dpfCommand = dpfBase+11; // 28, 0x1000'0000, parameterless method marked as command

	byte//--- attribute indices, mnemonis in: debug.IClassFileConstMnemonics.attributes
		atxConstantValue = 0,
		atxDeprecated = 1,
		atxSynthetic = 2,
		atxSourceFile = 3,

		atxCode = 4,
		atxLocalVariableTable = 5,
		atxLineNumberTable = 6,
		atxExceptions = 7,

		atxInnerClasses = 8;
}
