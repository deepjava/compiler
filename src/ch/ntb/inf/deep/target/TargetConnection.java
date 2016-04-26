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

package ch.ntb.inf.deep.target;

import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;

public abstract class TargetConnection {
		
	public static final byte stateRunning = 0x01;
	public static final byte stateStopped = 0x02;
	public static final byte stateDebug = 0x03;
	public static final byte stateUnknown = -1; // 0xff

	protected static final int defaultValue = 0;

	public abstract void openConnection() throws TargetConnectionException;
	public abstract void setOptions(HString opts);
	public abstract void closeConnection();
	public abstract boolean isConnected();

	public abstract int getTargetState() throws TargetConnectionException;
	public abstract void startTarget(int adddress) throws TargetConnectionException;
	public abstract void stopTarget() throws TargetConnectionException;
	public abstract void resetTarget() throws TargetConnectionException;
	
	public abstract void setRegisterValue(String regName, long value) throws TargetConnectionException;	
	public abstract void setRegisterValue(Register reg, long value) throws TargetConnectionException;
	public abstract long getRegisterValue(String regName) throws TargetConnectionException;
	public abstract long getRegisterValue(Register reg) throws TargetConnectionException;
	
	public abstract byte readByte(int address) throws TargetConnectionException;
	public abstract short readHalfWord(int address) throws TargetConnectionException;
	public abstract int readWord(int address) throws TargetConnectionException;
	public abstract void writeByte(int address, byte data) throws TargetConnectionException;
	public abstract void writeHalfWord(int address, short data) throws TargetConnectionException;
	public abstract void writeWord(int address, int data) throws TargetConnectionException;
	
	public abstract void writeTMS(TargetMemorySegment tms) throws TargetConnectionException;
	public abstract void downloadImageFile(String filename) throws TargetConnectionException;
	
	public abstract void setBreakPoint(int address) throws TargetConnectionException;
	public abstract void removeBreakPoint(int address) throws TargetConnectionException;
	public abstract void confirmBreakPoint(int address) throws TargetConnectionException;
	
}
