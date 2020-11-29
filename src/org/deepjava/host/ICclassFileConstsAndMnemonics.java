/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.host;

import org.deepjava.classItems.ICclassFileConsts;

public interface ICclassFileConstsAndMnemonics extends  ICclassFileConsts{

	//--- debug strings for: constant pool tags (cpt)
	String[] cptIdents = {
		"ExtSlot",
		"Utf8",
		"?",
		"Integer", "Float", "Long", "Double",
		"Class", "String", "FieldRef", "MethRef",
		"IntfMethRef", "NameAndType"
	};

	//--- debug strings for: access and property flags (apf) for class, field, method
	String[] apfIdents = {
			"public", 				// 0
			"private", 				// 1
			"protected", 			// 2
			"static", 				// 3
			"final", 				// 4
			"|Csuper|Msynch", 		// 5
			"|Mbridge|Fvolatile",	// 6
			"|Mvarargs|Ftransient", // 7
			"native", 				// 8
			"interface", 			// 9
			"abstract", 			// 10
			"strict", 				// 11
			"synthetic|enumArray", 	// 12
			"annotation", 			// 13
			"enum", 				// 14
			
			//---- deep properties
			"classLoaded", 			// 15 
			"rootClass", 			// 16 
			"declaration", 			// 17 
			"instances", 			// 18 
			"typeTest", 			// 19 
			"|Cmarked|Fconst", 		// 20  
			"|Cextended|FreadAccess", // 21 
			"writeAccess", 			// 22
			"command", 				//23 
			"call",					//24 
			"excHndCall",			//25 
			"excHnd", 				//26 
			"interfaceCall",		//27 
			"new",					//28 
			"unsafe",				//29 
			"sysPrimitive", 		//30 
			"synthetic", 			//31 
	};

	String[] attributes = {// class file attributes: 
			"ConstantValue", // 4.7.2, p119, items: fields
			"Deprecated", // 4.7.10, p132, items: fields, methods
			"Synthetic", // 4.7.6, p127, items: fields, methods
			"SourceFile", // 4.7.7, p128
			
			"Code", // 4.7.3, p120, items: methods
			"LocalVariableTable", // 4.7.9, p130
			"LineNumberTable", // 4.7.8, p129
			"Exceptions", // 4.7.4, p123

			"InnerClasses", // 4.7.5, p125
		};
}
