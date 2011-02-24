package ch.ntb.inf.deep.eclipse.launcher;

import org.eclipse.core.resources.IMarkerDelta;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

/**
 * This class is only needed to send IDebugEvent.Terminated
 * @author millischer
 *
 */

public class DummyDebugTarget extends DebugElement implements IDebugTarget {
	
	// containing launch object
	private ILaunch fLaunch;

	
	public DummyDebugTarget(ILaunch launch) {
		super(null);
		fLaunch = launch;
	}

	@Override
	public String getName() throws DebugException {
		return "deep";
	}

	@Override
	public IProcess getProcess() {
		return null;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return new IThread[0];
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return false;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		return false;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public String getModelIdentifier() {
		return null;
	}

	@Override
	public boolean canTerminate() {
		return !isTerminated();
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public void terminate() throws DebugException {
		fireTerminateEvent();
	}

	@Override
	public boolean canResume() {
		return false;
	}

	@Override
	public boolean canSuspend() {
		return false;
	}

	@Override
	public boolean isSuspended() {
		return false;
	}

	@Override
	public void resume() throws DebugException {
	}

	@Override
	public void suspend() throws DebugException {
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	@Override
	public boolean canDisconnect() {
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
	}

	@Override
	public boolean isDisconnected() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)	throws DebugException {
		return null;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

}
