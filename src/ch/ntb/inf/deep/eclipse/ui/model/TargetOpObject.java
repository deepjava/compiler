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

package ch.ntb.inf.deep.eclipse.ui.model;

public class TargetOpObject {
	
	public long value;
	
	/**
	 * type of the value
	 */
	public int valueType;
	
	/**
	 * Name of the variable or the register, or the full qualified name of a TargetCMD
	 */
	public String description;
	
	/** 
	 * Kind of Representations:	Binary = 0, Hexadecimal = 1, Decimal = 2, Double = 3
	 */
	public int representation;
	
	/**
	 * Register size in bytes
	 */
	public int registerSize;
	
	/**
	 * Address of the register, memory, variable or method
	 */
	public int addr;
	
	/**
	 * Type of the Register	 
	 */
	public int registerType;
	
	/**
	 * operation opcode
	 * No operation: 0; Register: 1; Variable: 2; Address: 3; TargetCmd: 4
	 */
	public int operation;
	
	/**
	 * indicates when a readVariable operation is chosen. If true, displays the value  
	 */
	public boolean isRead;
	/**
	 * indicates when an TargetCMD operation was executed. If true, displays the address of the method  
	 */
	public boolean cmdSend;
	/**
	 * Error message
	 */
	public String errorMsg;
	/**
	 * Note
	 */
	public String note;
	
	public TargetOpObject(){
		description = "";
	}
	
	public TargetOpObject(int operation, String description){
		this.operation = operation;
		this.description = description;
		this.errorMsg = "";
		representation = 1;
		this.note = "";
	}
	
	public TargetOpObject(int operation, String description, long value, int type, int representation, int registerSize){
		this.operation = operation;
		this.description = description;
		this.value = value;
		this.valueType = type;
		this.representation = representation;
		this.registerSize = registerSize;
		this.errorMsg = "";
		this.note = "";
	}
}
