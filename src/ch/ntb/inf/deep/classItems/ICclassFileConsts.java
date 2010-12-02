package ch.ntb.inf.deep.classItems;

public interface ICclassFileConsts {

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

		dpfDeprecated = 13, // deprecated field or method
		dpfSynthetic = 15; // synthetic field or method (items not in source text, deep inline methods)

	byte dpfBase = 16; // base flag number of deep property flags
	byte// flag numbers of deep property flags,  mnemonics in: debug.IClassFileConstMnemonics
		//--- class flags:
		dpfClassLoaded = dpfBase+0,	// class loaded
		dpfRootClass = dpfBase+1,	// this class is a root class (loaded by loadRootClass(..) )
		dpfDeclaration = dpfBase+2,	// class is used for declarations of static fields, instance fields, local variables
		dpfInstances = dpfBase+3,	// there might be objects of this class in the running system
		dpfTypeTest = dpfBase+4,	// there are type tests with this type (instructions: checkcast, instanceof)

		//--- field flags:
		dpfConst = dpfBase+5,	// constant field
		dpfReadAccess = dpfBase+6,	// one or more read accesses to this item
		dpfWriteAccess = dpfBase+7,	// one or more write accesses to this item

		//--- method flags:
		dpfCommand = dpfBase+8,	// method is a command, i.e. this method is invoked by an outside client
		dpfIsSysPrimitive = dpfBase+9,	// method is a system primitive
		dpfIsExcHnd = dpfBase+10,	// method is an exception handler, i.e. this method is invoked by hardware
		dpfCall = dpfBase+11,	// method gets called by the bc instructions invokestatic or invokevirtual
		dpfInterfCall = dpfBase+12,	// method gets invoked by the bc instruction invokeinterface
		dpfExcHndCall = dpfBase+13,	// method gets invoked directly or indirectly by an exception handler method
		dpfNew = dpfBase+14,	// method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
		dpfUnsafe = dpfBase+15;	// method is unsafe


	int// deep system primitive identifiers (0xFFFF'FCMM: F= Flags, C= system class number, MM-method number
		dscUS = 0x000, // deep system class: deep/UNSAFE/US,  dscUS = system class 0
		dspiPUT1 = dscUS+0x01,
		dspiPUT2 = dscUS+0x02,
		dspiPUT4 = dscUS+0x03,
		dspiPUT8 = dscUS+0x04,
		dspiGET1 = dscUS+0x05,
		dspiGET2 = dscUS+0x06,
		dspiGET4 = dscUS+0x07,
		dspiGET8 = dscUS+0x08,
		dspiGETBIT = dscUS+0x09,
		
		dspiASM  = dscUS+0x0A;

	int
		dscLL = 0x100, // deep system class: deep/lowLevel/LL,  dscLL = system class 1
		dspiMostSign1BitNr = dscLL+0x01,
		dspiLeastSign1BitNr = dscLL+0x02,
		dspiGetBit = dscLL+0x03,
		dspiIsPowerOf2 = dscLL+0x04,
		dspiNextPowerOf2 = dscLL+0x05;

	int
		dscHeap = 0x200, // deep system class: deep/runtime/Heap,  dscHeap = system class 2
		dspiNewObject = dscHeap+0x01, // bc 187
		dspiNewPrimitiveArray = dscHeap+0x02, // bc 188
		dspiNewReferenceArray = dscHeap+0x03, // bc 189
		dspiNewMultiDimArray = dscHeap+0x04; // bc 197

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
