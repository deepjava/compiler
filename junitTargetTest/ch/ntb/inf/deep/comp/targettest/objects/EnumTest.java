package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.deep.comp.targettest.objects.helper.ClassA;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleA.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleB.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleC.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleD.*;

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
		Assert.assertEquals("test3", 3000, res);
		
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

enum Color { WHITE, BLACK, RED;}