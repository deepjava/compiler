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
	
	public TargetOpObject(){
		description = "";
	}
	
	public TargetOpObject(int operation, String description){
		this.operation = operation;
		this.description = description;
		this.errorMsg = "";
	}
	
	public TargetOpObject(int operation, String description, long value, int type, int representation, int registerSize){
		this.operation = operation;
		this.description = description;
		this.value = value;
		this.valueType = type;
		this.representation = representation;
		this.registerSize = registerSize;
		this.errorMsg = "";
	}
}
