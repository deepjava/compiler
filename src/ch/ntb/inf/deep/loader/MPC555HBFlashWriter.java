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

package ch.ntb.inf.deep.loader;

import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.Memorysector;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;

public class MPC555HBFlashWriter implements MemoryWriter {
	private Downloader bdi;
	public boolean unlocked = false;
	public MPC555HBFlashWriter(Downloader bdi){
		this.bdi = bdi;
	}
	
	
	@Override
	public int writeByte(int addr, byte data) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int writeSequence(TargetMemorySegment seg) {
		if(!unlocked){
			ErrorReporter.reporter.error(Downloader.errBypassNotUnlocked);
			return 0;
		}
		int i;
		for( i = 0; i < seg.data.length; i++){
			try {
				bdi.setMem(seg.startAddress, 0x05000500, 4);
				bdi.setMem(seg.startAddress + i * 4, seg.data[i], 4);	
				if(i % 200 == 0){
					StdStreams.out.print(".");
				}
			} catch (DownloaderException e) {
				ErrorReporter.reporter.error(Downloader.errProgrammFailed);
				return i;
			}
		}
		return i;
	}

	@Override
	public int writeWord(int addr, int data) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void eraseDevice(Device dev) {
		StdStreams.out.println("erasing device " + dev.getName().toString());
		try {
			bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x55005500, 4);
			bdi.setMem(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00, 4);
			bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x01000100, 4);
			bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x55005500, 4);
			bdi.setMem(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00, 4);
			bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x08000800, 4);
			waitByDataPoll(dev.getbaseAddress(), 0xFFFFFFFF, 30000);
		} catch (DownloaderException e) {
			ErrorReporter.reporter.error(Downloader.errConnectionLost,"while erasing device");
		}
		
	}

	@Override
	public void eraseMarkedSectors(Device dev) {
		Memorysector current = dev.sector;
		while (current != null){
			if(current.used){
				StdStreams.out.println("erasing " + dev.getName().toString() + " sector " + current.getName().toString());
				try {
					bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x55005500, 4);
					bdi.setMem(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00, 4);
					bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x01000100, 4);
					bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x55005500, 4);
					bdi.setMem(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00, 4);
					bdi.setMem(current.getBaseAddress(), 0x0C000C00, 4);
					waitByDataPoll(current.getBaseAddress(), 0xFFFFFFFF, 5000);
				} catch (DownloaderException e) {
					ErrorReporter.reporter.error(Downloader.errConnectionLost,"while erasing sector");
					return;
				}
			}
			current = current.next;
		}
		
	}
	
	private void waitByDataPoll(int addr, int expData, int timeout){
		long endTime = System.currentTimeMillis() + timeout;
		long currentTime;
		int data, count = 0;
		do{
			try {
				data = bdi.getMem(addr, 4);
				count++;
				if(count > 150){
					count = 0;
					StdStreams.out.print(".");
				}
				currentTime = System.currentTimeMillis();
			} catch (DownloaderException e) {
				return;
			}
		} while(data != expData && endTime > currentTime); 
		StdStreams.out.println();
	}
	
	public void unlockBypass(Device dev, boolean unlock){
		if(unlock){//enable fast programming mode
			try {
				bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x55005500, 4);
				bdi.setMem(dev.getbaseAddress()+ 0xAA8, 0xAA00AA00, 4);
				bdi.setMem(dev.getbaseAddress()+ 0x1554, 0x04000400, 4);
				unlocked = true;
			} catch (DownloaderException e) {
			}
		}else{//disable fast programming
			try {
				bdi.setMem(dev.getbaseAddress(), 0x09000900, 4);
				bdi.setMem(dev.getbaseAddress(), 0x0, 4);
				unlocked = false;
			} catch (DownloaderException e) {
			}
		}
	}

}
