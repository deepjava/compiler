package ch.ntb.inf.junitTarget.lib;

import ch.ntb.inf.deep.runtime.mpc555.*;
import ch.ntb.inf.deep.unsafe.US;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Ignore;
import ch.ntb.inf.junitTarget.Test;

public class TaskTest implements IntbMpc555HB {
	static int res;

	@Before
	public static void startTask() {
		res = 1;
		new TaskExt();
		new ActionableImpl(2);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testTask() {
		Assert.assertEquals("Test1", 1, res);
		Assert.assertEquals("Test2", 5, TaskExt.count);
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void testActionable() {
		Assert.assertEquals("Test2", 5, ActionableImpl.count);
		CmdTransmitter.sendDone();
	}	
	
	@Test
	public static void testTaskTime() {
		US.PUT2(TBSCR, 0); 	// stop timer
		long time = Kernel.time();
		int timeMs = Task.time();	
		Assert.assertEquals("Test1", timeMs, time / 1000);
		US.PUT2(TBSCR, 1); 	// restart timer
		CmdTransmitter.sendDone();
	}	
}

class TaskExt extends Task {
	static int count;
	
	public void action() {
		count++;
		if (nofActivations == 5) Task.remove(this);
	}
	
	TaskExt() {
		Task.install(this);
	}
}

class ActionableImpl implements Actionable {
	static int count;
	static Task t;
	
	public void action() {
		count++;
		if (count == 5) Task.remove(t);
	}
	
	public static void init() {
		t = new Task(new ActionableImpl(2));
		Task.install(t);
	}
	
	ActionableImpl(int x) {
		count = x;
		t = new Task(this);
		Task.install(t);
	}
}
