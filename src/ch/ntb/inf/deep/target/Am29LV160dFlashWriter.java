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

package ch.ntb.inf.deep.target;

import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.Memorysector;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;

public class Am29LV160dFlashWriter {
	
	private TargetConnection bdi;
	public boolean unlocked = false;
	
	public Am29LV160dFlashWriter(TargetConnection bdi){
		this.bdi = bdi;
	}
	
	public int writeSequence(TargetMemorySegment seg) {
		if(!unlocked){
			ErrorReporter.reporter.error(TargetConnection.errBypassNotUnlocked);
			return 0;
		}
		int i;
		for( i = 0; i < seg.data.length; i++){
			try {
				bdi.writeWord(seg.startAddress, 0x05000500);
				bdi.writeWord(seg.startAddress + i * 4, seg.data[i]);	
				if(i % 200 == 0){
					StdStreams.out.print(".");
				}
			} catch (TargetConnectionException e) {
				ErrorReporter.reporter.error(TargetConnection.errProgrammFailed);
				return i;
			}
		}
		return i;
	}

	public void eraseDevice(Device dev) {
		StdStreams.out.println("erasing device " + dev.getName().toString());
		try {
			bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x55005500);
			bdi.writeWord(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00);
			bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x01000100);
			bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x55005500);
			bdi.writeWord(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00);
			bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x08000800);
			waitByDataPoll(dev.getbaseAddress(), 0xFFFFFFFF, 30000);
		} catch (TargetConnectionException e) {
			ErrorReporter.reporter.error(TargetConnection.errConnectionLost,"while erasing device");
		}
		
	}

	public void eraseMarkedSectors(Device dev) {
		Memorysector current = dev.sector;
		while (current != null){
			if(current.used){
				StdStreams.out.println("erasing " + dev.getName().toString() + " sector " + current.getName().toString());
				try {
					bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x55005500);
					bdi.writeWord(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00);
					bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x01000100);
					bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x55005500);
					bdi.writeWord(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00);
					bdi.writeWord(current.getBaseAddress(), 0x0C000C00);
					waitByDataPoll(current.getBaseAddress(), 0xFFFFFFFF, 5000);
				} catch (TargetConnectionException e) {
					ErrorReporter.reporter.error(TargetConnection.errConnectionLost,"while erasing sector");
					return;
				}
			}
			current = (Memorysector)current.next;
		}
		
	}
	
	private void waitByDataPoll(int addr, int expData, int timeout) throws TargetConnectionException{
		long endTime = System.currentTimeMillis() + timeout;
		long currentTime;
		int data, count = 0;
		do{
			data = bdi.readWord(addr);
			count++;
			if(count > 150){
				count = 0;
				StdStreams.out.print(".");
			}
			currentTime = System.currentTimeMillis();
		} while(data != expData && endTime > currentTime); 
		StdStreams.out.println();
	}
	
	public void unlockBypass(Device dev, boolean unlock){
		if(unlock) { // enable fast programming mode
			try {
				bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x55005500);
				bdi.writeWord(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00);
				bdi.writeWord(dev.getbaseAddress()+ 0x1554, 0x04000400);
				unlocked = true;
			} catch (TargetConnectionException e) {
				// TODO add error msg here
			}
		}
		else { // disable fast programming
			try {
				bdi.writeWord(dev.getbaseAddress(), 0x09000900);
				bdi.writeWord(dev.getbaseAddress(), 0x0);
				unlocked = false;
			} catch (TargetConnectionException e) {
				// TODO add error msg here
			}
		}
	}

}
