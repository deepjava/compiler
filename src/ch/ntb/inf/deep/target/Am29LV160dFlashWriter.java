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
import ch.ntb.inf.deep.config.MemSector;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class Am29LV160dFlashWriter {
	
	private TargetConnection tc;
	public boolean unlocked = false;
	
	public Am29LV160dFlashWriter(TargetConnection tc) {
		this.tc = tc;
	}
	
	public int writeSequence(TargetMemorySegment seg) {
		if (!unlocked) {
			ErrorReporter.reporter.error(808);
			return 0;
		}
		int i;
		for (i = 0; i < seg.data.length; i++) {
			if (ErrorReporter.reporter.nofErrors <= 0) {
				try {
					tc.writeWord(seg.startAddress, 0x05000500);
					tc.writeWord(seg.startAddress + i * 4, seg.data[i]);	
					if(i % 200 == 0){
						StdStreams.log.print(".");
					}
				} catch (TargetConnectionException e) {
					ErrorReporter.reporter.error(809);
					return i;
				}
			}
		}
		return i;
	}

	public void eraseDevice(Device dev) {
		StdStreams.log.println("erasing device " + dev.name.toString());
		try {
			tc.writeWord(dev.address + 0x1554, 0x55005500);
			tc.writeWord(dev.address + 0xAA8, 0xAA00AA00);
			tc.writeWord(dev.address + 0x1554, 0x01000100);
			tc.writeWord(dev.address + 0x1554, 0x55005500);
			tc.writeWord(dev.address + 0xAA8, 0xAA00AA00);
			tc.writeWord(dev.address + 0x1554, 0x08000800);
			waitByDataPoll(dev.address, 0xFFFFFFFF, 30000);
		} catch (TargetConnectionException e) {
			ErrorReporter.reporter.error(803,"while erasing device");
		}
		
	}

	public void eraseMarkedSectors(Device dev) {
		MemSector current = dev.sector;
		while (current != null){
			if (current.used){
				StdStreams.log.println("erasing " + dev.name.toString() + " sector " + current.name.toString());
				try {
					tc.writeWord(dev.address + 0x1554, 0x55005500);
					tc.writeWord(dev.address + 0xAA8, 0xAA00AA00);
					tc.writeWord(dev.address + 0x1554, 0x01000100);
					tc.writeWord(dev.address + 0x1554, 0x55005500);
					tc.writeWord(dev.address + 0xAA8, 0xAA00AA00);
					tc.writeWord(current.address, 0x0C000C00);
					waitByDataPoll(current.address, 0xFFFFFFFF, 5000);
				} catch (TargetConnectionException e) {
					ErrorReporter.reporter.error(803,"while erasing sector");
					return;
				}
			}
			current = (MemSector)current.next;
		}
		
	}
	
	private void waitByDataPoll(int addr, int expData, int timeout) throws TargetConnectionException {
		long endTime = System.currentTimeMillis() + timeout;
		long currentTime;
		int data, count = 0;
		do {
			data = tc.readWord(addr);
			count++;
			if(count > 150){
				count = 0;
				StdStreams.log.print(".");
			}
			currentTime = System.currentTimeMillis();
		} while(data != expData && endTime > currentTime); 
		StdStreams.log.println();
	}
	
	public void unlockBypass(Device dev, boolean unlock) {
		if (unlock) { // enable fast programming mode
			try {
				tc.writeWord(dev.address + 0x1554, 0x55005500);
				tc.writeWord(dev.address + 0xAA8, 0xAA00AA00);
				tc.writeWord(dev.address + 0x1554, 0x04000400);
				unlocked = true;
			} catch (TargetConnectionException e) {
				ErrorReporter.reporter.error(808);
			}
		}
		else { // disable fast programming
			try {
				tc.writeWord(dev.address, 0x09000900);
				tc.writeWord(dev.address, 0x0);
				unlocked = false;
			} catch (TargetConnectionException e) {
				ErrorReporter.reporter.error(808);
			}
		}
	}

}
