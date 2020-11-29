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

package org.deepjava.comp.targettest.various;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 25.3.2011, Urs Graf
 * 
 *         Changes:
 */
@MaxErrors(100)
public class StackFrame {
	
	// check if passed parameters are properly ordered
	static short led;
	static void out(boolean a, int b, boolean c) {
		if (c) led |= 1 << b;
		else led &= ~(1 << b);
	}
	
	static void setPosLEDs(boolean state) {
		for (int i = 12; i < 15; i++)
			out(true, i, !state);
	}
	
	@Test
	public static void setPosLEDsTest() {
		setPosLEDs(false);
		Assert.assertEquals("Test1", 0x7000, led);
		setPosLEDs(true);
		Assert.assertEquals("Test2", 0x0000, led);

		CmdTransmitter.sendDone();
	}	

	// checks if index with a is not overwritten by c
	static int loadLocal(int a, int b) {
		int c;
		if (b > 0) {
			return a + 1;
		} else {
			c = 2;
			return a + c;
		}
	}
	
	@Test
	public static void loadLocalTest() {
		Assert.assertEquals("Test1", 11, loadLocal(10, 100));
		setPosLEDs(true);
		Assert.assertEquals("Test2", 12, loadLocal(10, -100));

		CmdTransmitter.sendDone();
	}	
	

	// from the display project
	public void copyBlock (int sl, int sb, int w, int h, int dl, int db, int mode){ 
		int n, k, xStep, yStep, wStart, wStop, hStart, hStop;	 
		if ((w <= 0) || (h <= 0) ) return;
		if (sl >= dl) {xStep = 1; wStart = 0; wStop = w;} else {xStep = - 1; wStart = w-1; wStop = -1;}
		if (sb >= db) {yStep = 1; hStart = 0; hStop = h;} else {yStep = - 1; hStart = h-1; hStop = -1;}
		n = hStart;
		while (n != hStop) {
			k = wStart;
			while (k != wStop) {
				int col = this.getDot(sl + k, sb + n);
				this.setDot(col, dl + k, db + n, mode);
				k = k + xStep;
         	}
			n = n + yStep;
		}
	}

	@SuppressWarnings("unused")
	public void replConstNonsense(int col, int l, int b, int w, int h, int mode){ 
		int x, y;
		int addr = 0;
		if ((w <= 0) || (h <= 0)) return;
		for (x = l; x < (l + w); x+=8){
			for (y = b; y < (b + h); y++){
				addr = (127-y) * 16 + x / 8;
				buf[128] = (byte) 0xff;
			}
		}
	}

	public void replConst (int col, int l, int b, int w, int h, int mode){ 
        int x, y;
        if ((w <= 0) || (h <= 0)) return;
        for (x = l; x < (l + w); x++){
            for (y = b; y < (b + h); y++){
                this.setDot(col, x, y, mode);
            }
        }
  	}

	byte[] buf = new byte[2048];
	static StackFrame obj = new StackFrame();
	
	void setDot (int col, int x, int y, int mode) {
		int addr = (127-y) * 16 + x / 8;
		int bit = 1 << (x % 8);
		switch (mode) {
		case 0:
			if (col == 0) buf[addr] |= bit;	// set bit 
			else buf[addr] &= ~bit;	// clear bit 
			break;
		case 1: 
			if (col == 0) buf[addr] |= bit;	// set bit 
			break;
		case 2: 
			if ((bit & buf[addr]) != 0) { 
				if (col == 0) buf[addr] &= ~bit;	// clear bit 
				else buf[addr] |= bit;	// set bit 
			} else {
				if (col == 0) buf[addr] |= bit;	// clear bit 
				else buf[addr] &= ~bit;	// set bit 
			} 
			break;
		default:
			break;
		}
	}

	int getDot(int x, int y) {
		int addr = (127-y) * 16 + x / 8;
		int bit = 1 << (x % 8);
		return buf[addr] & bit;
	}

	@Test
	public static void replConstNonsenseTest() {
		obj.replConstNonsense(0, 1, 1, 10, 10, 1);
		Assert.assertEquals("Test1", 0, obj.buf[0]);
		Assert.assertEquals("Test2", (byte)0xff, obj.buf[128]);
		Assert.assertEquals("Test3", 0, obj.buf[2047]);	
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void replConstTest() {
		obj.replConst(0, 1, 1, 10, 2, 0);
		Assert.assertEquals("Test1", 0, obj.buf[0]);
		Assert.assertEquals("Test2", (byte)0xfe, obj.buf[2016]);
		Assert.assertEquals("Test3", (byte)0x07, obj.buf[2017]);
		Assert.assertEquals("Test4", 0, obj.buf[2032]);
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void copyBlockTest() {
		StackFrame obj = new StackFrame();
		obj.replConst(0, 8, 125, 8, 2, 1);
		Assert.assertEquals("Test1", 0, obj.buf[0]);
		Assert.assertEquals("Test2", 0, obj.buf[16]);
		Assert.assertEquals("Test3", (byte)0xff, obj.buf[17]);
		Assert.assertEquals("Test4", 0, obj.buf[18]);
		obj.copyBlock(0, 124, 30, 3, 0, 120, 0);
		Assert.assertEquals("Test11", 0, obj.buf[0]);
		Assert.assertEquals("Test12", (byte)0xff, obj.buf[80]);
		Assert.assertEquals("Test13", 0, obj.buf[81]);
		Assert.assertEquals("Test14", (byte)0xff, obj.buf[82]);
		CmdTransmitter.sendDone();
	}	

}
