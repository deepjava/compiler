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

package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Ignore;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 12.03.2013
 * 
 * @author Urs Graf
 * 
 * Tests for enums
 */

@MaxErrors(100)
public class EnumTest {

	@Test
	public static void testEnum1() {
		Assert.assertEquals("test1", "BLACK", Color.BLACK.name());
		Assert.assertEquals("test2", "RED", Color.RED.toString());
		Assert.assertEquals("test3", "WHITE", Color.valueOf("WHITE").name());

		Assert.assertEquals("test10", 0, Color.WHITE.ordinal());
		Assert.assertEquals("test11", 1, Color.BLACK.ordinal());
		Assert.assertEquals("test12", 2, Color.RED.ordinal());
		
		CmdTransmitter.sendDone();		
	}
	
	@Ignore
	@Test
	public static void testEnumSwitch() {
		int res = 0;
		Color state = Color.BLACK;
		switch (state) {
		case WHITE: res = 1000; break;
		case BLACK: res = 2000; break;
		case RED: res = 3000; break;
		}
		Assert.assertEquals("test1", 2000, res);
		
		state = Color.RED;
		switch (state) {
		case WHITE: res = 1000; break;
		case BLACK: res = 2000; break;
		case RED: res = 3000; break;
		}
		Assert.assertEquals("test2", 3000, res);
		
		Color state1 = Color.BLACK;
		switch (state1) {
		case WHITE: res = 1000; break;
		case BLACK: res = 2000; break;
		case RED: res = 3000; break;
		}
		Assert.assertEquals("test3", 2000, res);
		
		CmdTransmitter.sendDone();		
	}
		
}

enum Color {WHITE, BLACK, RED}