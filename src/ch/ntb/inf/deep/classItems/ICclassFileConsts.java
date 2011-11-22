/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

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

		dpfDeprecated = 13; // deprecated field or method

	byte dpfBase = 15; // base flag number of deep property flags
	byte// flag numbers of deep property flags,  mnemonics in: debug.IClassFileConstMnemonics
		//--- class flags:
		dpfClassLoaded = dpfBase+0,	// class loaded
		dpfRootClass = dpfBase+1,	// this class is a root class (loaded by loadRootClass(..) )
		dpfDeclaration = dpfBase+2,	// class is used for declarations of static fields, instance fields, local variables
		dpfInstances = dpfBase+3,	// there might be objects of this class in the running system
		dpfTypeTest = dpfBase+4,	// there are type tests with this type (instructions: checkcast, instanceof)

		dpfClassMark = dpfBase+5, // mark flag during traverses (dpfConst = dpfClassMark !)
		dpfExtended = dpfBase+6, // class has extensions (one or more)

		//--- field flags:
		dpfConst = dpfBase+5,	// constant field (dpfConst = dpfClassMark !)
		dpfReadAccess = dpfBase+6,	// one or more read accesses to this item
		dpfWriteAccess = dpfBase+7,	// one or more write accesses to this item

		//--- method flags:
		dpfCommand = dpfBase+8,	// method is a command, i.e. this method is invoked by an outside client
		dpfCall = dpfBase+9,	// method gets called by the bc instructions invokestatic or invokevirtual
//		dpfInterfCall = dpfBase+10,	// method gets invoked by the bc instruction invokeinterface ==> moved to "class and method flags"
		dpfExcHndCall = dpfBase+10,	// method gets invoked directly or indirectly by an exception handler method
		dpfExcHnd = dpfBase+11,	// method is an exception handler, i.e. this method is invoked by hardware
		//--- shared flag positions with fields:
		dpfDone = dpfBase+6,	// for class constructors only

		//--- class and method flags:
		//--- meaning as class flag: class has method(s) with this flag (at least one)
		dpfInterfCall = dpfBase+12,	// method gets invoked by the bc instruction invokeinterface,  interface(class) is referenced by invokeinterface
		dpfNew = dpfBase+13, // method flag: method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
		dpfUnsafe = dpfBase+14,	// method is unsafe
		dpfSysPrimitive = dpfBase+15,	// method is a system primitive
		dpfSynthetic = dpfBase+16; // synthetic field or method (items not in source text, or deep in-line methods)

	int apfSetJavaAccAndProperties = (1<<apfEnum)|(1<<apfEnumArray)|(1<<apfStrict)|(1<<apfAbstract)|(1<<apfInterface)
			|(1<<apfNative)	|(1<<apfTransient)|(1<<apfVolatile)|(1<<apfSynchronized) |(1<<apfSuper) |(1<<apfFinal)
			|(1<<apfStatic) |(1<<apfProtected) |(1<<apfPrivate) |(1<<apfPublic);

	int dpfSetClassProperties =(1<<dpfInterfCall)|(1<<dpfExtended)|(1<<dpfClassMark)|(1<<dpfTypeTest)|(1<<dpfInstances)|(1<<dpfDeclaration)|(1<<dpfRootClass)|(1<<dpfClassLoaded)|(1<<dpfSynthetic)|(1<<dpfDeprecated);

	int dpfSetFieldProperties = (1<<dpfConst)|(1<<dpfReadAccess)|(1<<dpfWriteAccess);
	
	int dpfSetMethProperties =
		 (1<<dpfUnsafe)|(1<<dpfNew)|(1<<dpfExcHndCall)|(1<<dpfInterfCall)|(1<<dpfCall)|(1<<dpfExcHnd)|(1<<dpfSysPrimitive)|(1<<dpfCommand)
		|(1<<dpfSynthetic)|(1<<dpfDeprecated);
	int sysMethCodeMask = 0xFFF; // 12 least significant bits, the system method code is defined within the configuration specification
	int dpfSetSysMethProperties = dpfSetMethProperties;

	int dpfSetSysClassProperties = dpfSetClassProperties | dpfSetSysMethProperties;
	int dpfSetProperties = dpfSetClassProperties|dpfSetSysClassProperties | dpfSetMethProperties | dpfSetSysMethProperties | dpfSetFieldProperties;


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
