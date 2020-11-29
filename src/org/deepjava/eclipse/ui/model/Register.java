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

package org.deepjava.eclipse.ui.model;

public class Register {
	public int value;
	public String name;
	/** 
	 * Kind of Representations:	Hexadecimal = 1, Decimal = 2, Double = 3, Float = 4
	 */
	public int representation;
	
	public Register() {}
	
	public Register(String name, int value, int representation) {
		this.name = name;
		this.value = value;
		this.representation = representation;
	}

}
